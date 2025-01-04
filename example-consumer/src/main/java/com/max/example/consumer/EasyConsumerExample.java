package com.max.example.consumer;
import com.max.example.common.model.User;
import com.max.example.common.service.UserService;
import com.max.maxrpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 动态代理
//        UserService userService=new UserServiceProxy();
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("max");
        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
