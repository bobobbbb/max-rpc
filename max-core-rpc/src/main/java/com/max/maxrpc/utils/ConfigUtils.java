package com.max.maxrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 *
 * 该工具类用于加载 `.properties` 配置文件并将其中的配置项映射到 Java 对象中。
 * 提供了两种方式来加载配置：
 * 1. 加载普通的配置文件
 * 2. 加载支持区分环境的配置文件（例如：`application-dev.properties`, `application-prod.properties`）
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     *
     * 该方法是 `loadConfig` 方法的简化版本，仅支持根据传入的 `prefix` 加载配置，不区分环境。
     *
     * @param tClass 目标配置类的 Class 对象，表示将配置文件中的内容映射到哪个类
     * @param prefix 配置项的前缀，指定从配置文件中加载哪些配置项
     * @param <T> 配置类的类型
     * @return 返回映射后的配置类对象，类型为 `T`
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        // 调用重载方法，使用空字符串作为环境参数，表示不区分环境
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     *
     * 该方法加载指定环境下的配置文件（如：`application-prod.properties`），
     * 并将配置项（以指定的 `prefix` 开头）映射到指定的 Java 对象。
     *
     * @param tClass 目标配置类的 Class 对象，表示将配置文件中的内容映射到哪个类
     * @param prefix 配置项的前缀，指定从配置文件中加载哪些配置项
     * @param environment 配置环境（例如：`dev`、`prod`），决定加载 `application-<environment>.properties` 文件
     * @param <T> 配置类的类型
     * @return 返回映射后的配置类对象，类型为 `T`
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        // 创建一个 StringBuilder，用来构建配置文件的路径
        StringBuilder configFileBuilder = new StringBuilder("application");

        // 如果环境不为空，拼接环境名到文件名中（例如：`application-prod.properties`）
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }

        // 拼接配置文件的后缀名 ".properties"
        configFileBuilder.append(".properties");

        // 根据构建的文件名加载配置文件
        Props props = new Props(configFileBuilder.toString());

        // 使用前缀 `prefix`，将配置文件中的内容映射到目标类 `tClass` 中，并返回该对象
        return props.toBean(tClass, prefix);
    }
}
