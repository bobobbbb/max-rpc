package com.max.maxrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import com.max.maxrpc.RpcApplication;
import com.max.maxrpc.config.RpcConfig;
import com.max.maxrpc.constant.RpcConstant;
import com.max.maxrpc.loadbalancer.LoadBalancer;
import com.max.maxrpc.loadbalancer.LoadBalancerFactory;
import com.max.maxrpc.model.RpcRequest;
import com.max.maxrpc.model.RpcResponse;
import com.max.maxrpc.model.ServiceMetaInfo;
import com.max.maxrpc.registry.Registry;
import com.max.maxrpc.registry.RegistryFactory;
import com.max.maxrpc.serializer.Serializer;
import com.max.maxrpc.serializer.SerializerFactory;
import com.max.maxrpc.server.tcp.VertxTcpClient;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务代理（JDK 动态代理）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     *
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        try {
            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
//            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);
            //用负载均衡了
            LoadBalancer loadBalancer= LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());
            Map<String,Object> request=new HashMap<>();
            request.put("methodName",rpcRequest.getMethodName());
            ServiceMetaInfo selectedServiceMetaInfo=loadBalancer.select(request,serviceMetaInfoList);
            // 发送 TCP 请求
            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            return rpcResponse.getData();
        } catch (Exception e) {
            throw new RuntimeException("调用失败");
        }
    }
}
