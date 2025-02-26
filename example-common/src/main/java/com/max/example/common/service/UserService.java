package com.max.example.common.service;

import com.max.example.common.model.User;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);

    default int getNumber(){
        return 0;
    }
}
