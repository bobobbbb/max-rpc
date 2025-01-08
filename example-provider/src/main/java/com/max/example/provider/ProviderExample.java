package com.max.example.provider;
import com.max.example.common.service.UserService;
import com.max.maxrpc.RpcApplication;
import com.max.maxrpc.config.RegistryConfig;
import com.max.maxrpc.config.RpcConfig;
import com.max.maxrpc.model.ServiceMetaInfo;
import com.max.maxrpc.registry.LocalRegistry;
import com.max.maxrpc.registry.Registry;
import com.max.maxrpc.registry.RegistryFactory;
import com.max.maxrpc.server.HttpServer;
import com.max.maxrpc.server.VertxHttpServer;

/**
 * 服务提供者示例
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        // 注册服务
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        //在这里使用loadspi加载custom中的etcdregistry类，并在这里取到；
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            //调用的是etcd的注册方法 将这个service的信息注册进去etcd；
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
