package com.max.maxrpc.springboot.starter.bootstrap;
import com.max.maxrpc.RpcApplication;
import com.max.maxrpc.config.RpcConfig;
import com.max.maxrpc.server.tcp.VertxTcpServer;
import com.max.maxrpc.springboot.starter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import javax.swing.*;

/**
 * Rpc 框架启动
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">程序员鱼皮的编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
//registerBeanDefinitions 方法是在 Spring 容器的初始化过程中被调用的
//一但扫描到@EnableRpc的注解后，就通过@import注解来注册这RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class三个为bean
//而registerBeanDefinitions就是一旦被初始化后就立刻执行的，因此不用像其他两个consumer和provider一样需要监听每一个类加载为bean的时候，有没有注解@RpcReference和@RpcService了
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化时执行，初始化 RPC 框架
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    //动态地获取被注解的类的信息（needServer），并根据这些信息（needServer)选择是否起用服务器
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取 EnableRpc 注解的属性值
        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())
                .get("needServer");

        // RPC 框架初始化（配置和注册中心）
        RpcApplication.init();

        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 启动服务器
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        } else {
            log.info("不启动 server");
        }

    }
}
