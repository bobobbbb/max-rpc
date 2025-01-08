package com.max.maxrpc;
import com.max.maxrpc.config.RegistryConfig;
import com.max.maxrpc.config.RpcConfig;
import com.max.maxrpc.constant.RpcConstant;
import com.max.maxrpc.registry.Registry;
import com.max.maxrpc.registry.RegistryFactory;
import com.max.maxrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC 框架应用
 * 相当于 holder，存放了项目全局用到的变量。双检锁单例模式实现
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化，支持传入自定义配置
     *
     * @param newRpcConfig
     */
    /**
     * 框架初始化，支持传入自定义配置
     *
     * @param newRpcConfig
     */
    public static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("rpc init, config = {}", newRpcConfig.toString());
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}", registryConfig);

        //注册shutdown hook方法
//        为什么用 registry::destroy 而不是 registry.destroy()？
//        方法引用 vs 方法调用：
//        registry.destroy() 是直接调用 registry 对象的 destroy() 方法。它是一个 方法调用，会立即执行方法。
//        registry::destroy 是一个 方法引用，它并不立即调用方法，而是指向 registry 对象的 destroy() 方法，并可以在合适的时机（例如在线程启动时）由其他代码执行。方法引用是对方法的一种传递方式，而不是立即执行。
        Runtime.getRuntime().addShutdownHook(new Thread(registry::destroy));
    }

    /**
     * 初始化
     */
    public static void init() {
        RpcConfig newRpcConfig;
        try {
            //读取application.properties文件中前缀为RpcConstant.DEFAULT_CONFIG_PREFIX的值赋值给当前的config文件
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 配置加载失败，使用默认值
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置
     *
     * @return
     */
    public static RpcConfig getRpcConfig() {
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
