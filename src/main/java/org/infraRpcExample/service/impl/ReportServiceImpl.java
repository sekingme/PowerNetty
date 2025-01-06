package org.infraRpcExample.service.impl;

import org.infraRpcExample.service.ReportService;
import org.springframework.stereotype.Service;

/**
 * @author sekingme
 */
@Service
public class ReportServiceImpl implements ReportService {
    @Override
    public String getReport(String input, int number) {
        // 模拟业务逻辑
        return "{ \"input\": " + input + "||||" + number + ", \"content\": \"Report Content\"}";
    }
}
