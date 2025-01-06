package org.infraRpcExample.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RequestMapping("/eventManage")
public interface EventManageService {
    @GetMapping("/eventListByAppId")
    List<String> getEventListByAppId(@Valid @NotNull @RequestParam Long appId,
                                                          @Valid @Nullable @RequestParam String username);
}
