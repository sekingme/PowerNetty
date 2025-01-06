package org.remoteRpcExample.service.impl;

import org.remoteRpcExample.service.ReportService;

/**
 * @author sekingme
 */
public class ReportServiceImpl implements ReportService {
    @Override
    public String getReport(String input, int number) {
        // 模拟业务逻辑
        return "{ \"input\": " + input + "||||" + number + ", \"content\": \"Report Content\"}";
    }
}
