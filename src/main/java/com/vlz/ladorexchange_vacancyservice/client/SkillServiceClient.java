package com.vlz.ladorexchange_vacancyservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "${spring.clients.skill-service.name}",
        url = "${spring.clients.skill-service.url}"
)
public interface SkillServiceClient {

    @GetMapping("/api/skills/names/by-ids")
    List<String> findSkillNamesByIds(@RequestParam("ids") List<Long> ids);

    @GetMapping("/api/skills/map/by-ids")
    Map<Long, String> findSkillMapByIds(@RequestParam("ids") List<Long> ids);
}