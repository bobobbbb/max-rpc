package com.max.maxrpc.server;

import io.vertx.core.Vertx;

public class VertxHttpServer implements HttpServer{
    @Override
    public void doStart(int port) {
        //创建vertx
        Vertx vertx=Vertx.vertx();
        //创建服务器
        io.vertx.core.http.HttpServer server=vertx.createHttpServer();
        //监听端口并处理
        server.requestHandler(new HttpServerHandler());
//        server.requestHandler(request->{
//            //处理http请求
//            System.out.println("Received request" +""+ request.method()+""+request.uri());
//            //发送http响应
//            request.response()
//                    .putHeader("content-type","taxt/plain")
//                    .end("Hello from Vert.x HTTP server!");
//        });
        //启动http服务并监听指定端口
        server.listen(port,result->{
            if(result.succeeded()){
                System.out.println("Server is now listening on port"+port);
            }else{
                System.out.println("Failed to start port"+result.cause());
            }
        });
    }
}
