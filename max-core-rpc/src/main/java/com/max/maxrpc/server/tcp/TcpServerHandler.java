package com.max.maxrpc.server.tcp;
import com.max.maxrpc.model.RpcRequest;
import com.max.maxrpc.model.RpcResponse;
import com.max.maxrpc.protocol.ProtocolMessage;
import com.max.maxrpc.protocol.ProtocolMessageTypeEnum;
import com.max.maxrpc.registry.LocalRegistry;
import com.max.maxrpc.server.tcp.ProtocolMessageDecoder;
import com.max.maxrpc.server.tcp.ProtocolMessageEncoder;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

public class TcpServerHandler implements Handler<NetSocket> {
//    读取客户端发送的请求：当 netSocket.handler(tcpBufferHandlerWrapper) 被调用时，
//    netSocket 会从客户端接收数据，交给 TcpBufferHandlerWrapper 进行处理。
//    将响应数据发送给客户端：在处理完请求后，使用 netSocket.write(encode) 将响应数据发送回客户端。
    @Override
    public void handle(NetSocket netSocket) {
        // 处理连接
        TcpBufferHandlerWrapper tcpBufferHandlerWrapper=new TcpBufferHandlerWrapper(buffer -> {
            // 接受请求，解码
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            } catch (IOException e) {
                throw new RuntimeException("协议消息解码错误");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求
            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            try {
                // 获取要调用的服务实现类，通过反射调用
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 发送响应，编码
            ProtocolMessage.Header header = protocolMessage.getHeader();
            header.setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header, rpcResponse);
            try {
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                netSocket.write(encode);
            } catch (IOException e) {
                throw new RuntimeException("协议消息编码错误");
            }
        });
        netSocket.handler(tcpBufferHandlerWrapper);
    }
}
