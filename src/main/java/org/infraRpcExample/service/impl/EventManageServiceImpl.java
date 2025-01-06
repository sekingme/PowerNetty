package org.infraRpcExample.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infraRpcExample.service.EventManageService;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: Sekingme
 * @description:
 * @create: 2024-10-22 14:21
 **/

@RestController
@Slf4j
@RequiredArgsConstructor
public class EventManageServiceImpl implements EventManageService {
    @Override
    public List<String> getEventListByAppId(Long appId, String username) {
        return null;
    }
}
