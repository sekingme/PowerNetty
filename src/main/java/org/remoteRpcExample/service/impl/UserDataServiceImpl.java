package org.remoteRpcExample.service.impl;

import org.remoteRpcExample.service.UserDataService;

public class UserDataServiceImpl implements UserDataService {
    @Override
    public String getUserData(String userId) {
        // 模拟业务逻辑
        return "{ \"userId\": " + userId + ", \"name\": \"John Doe\" }";
    }
}
