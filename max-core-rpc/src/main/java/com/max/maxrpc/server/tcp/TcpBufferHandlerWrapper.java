package com.max.maxrpc.server.tcp;

import com.max.maxrpc.protocol.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式（使用 recordParser 对原有的 buffer 处理能力进行增强）
 */
//调用 TcpBufferHandlerWrapper 类的方法的顺序大致如下：
//
//1. 创建 TcpBufferHandlerWrapper 实例
//首先，创建 TcpBufferHandlerWrapper 类的实例，
// 并传入一个外部的 Handler<Buffer>（即 bufferHandler）。
// 这个 bufferHandler 将在解析完整的消息后被调用。
//TcpBufferHandlerWrapper wrapper = new TcpBufferHandlerWrapper(externalBufferHandler);
//2. 调用 handle 方法
//接下来，当新的数据（Buffer）到达时，handle(Buffer buffer) 方法会被调用。
// 在这一步，TcpBufferHandlerWrapper 会将接收到的 Buffer 数据交给 RecordParser 进行解析。
//wrapper.handle(buffer);
//3. RecordParser 处理数据
//在 handle 方法内部，调用了 recordParser.handle(buffer)，将数据传递给 RecordParser 进行解析。
// RecordParser 会根据其配置的解析规则（如固定长度的头部和变长的体部）逐步解析数据。
//4. setOutput 中的 handle 方法被触发
//RecordParser 在解析数据时，会逐步触发 setOutput 中的 handle 方法。
// 这个 handle 方法负责处理解析的每一部分数据（例如，解析消息头和消息体），并在完成时将数据交给外部的 bufferHandler 进行进一步处理。
public class TcpBufferHandlerWrapper implements Handler<Buffer> {
    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }
//    您可以理解为，parser.setOutput 会在数据流被逐步读取时，持续调用 handle 方法。
//    每当解析到新的数据时，handle 会将这部分数据进行处理（比如拼接消息头和消息体）。
//    一旦完成一条完整的消息后，handle 就会交给外部的 bufferHandler 进行处理。这样就实现了数据的逐步解析和处理。
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        // 构造 parser
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 一次完整的读取（头 + 体）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size) {
                    // 读取消息体长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 写入头信息到结果
                    resultBuffer.appendBuffer(buffer);
                } else {
                    // 写入体信息到结果
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整 Buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        return parser;
    }
}
