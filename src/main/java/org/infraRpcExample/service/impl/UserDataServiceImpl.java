package org.infraRpcExample.service.impl;

import org.infraRpcExample.service.TestRrturn;
import org.infraRpcExample.service.UserDataService;

public class UserDataServiceImpl implements UserDataService {
    @Override
    public TestRrturn getUserData(String userId) {
        // 模拟业务逻辑
        TestRrturn testRrturn = new TestRrturn();
        testRrturn.setA(userId);
        return testRrturn;
    }
}
