package com.max.maxrpc.config;

import com.max.maxrpc.fault.retry.RetryStrategyKeys;
import com.max.maxrpc.loadbalancer.LoadBalancerKeys;
import com.max.maxrpc.serializer.SerializerKeys;
import lombok.Data;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {
    private RegistryConfig registryConfig=new RegistryConfig();
    /**
     * 名称
     */
    private String name = "max-rpc";
    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;


    private boolean mock=false;

    private String serializer= SerializerKeys.JDK;

    private String loadBalancer= LoadBalancerKeys.RANDOM;

    private String retryStrategy= RetryStrategyKeys.NO;

}
