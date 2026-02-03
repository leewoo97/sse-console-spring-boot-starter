package io.github.leewoo97;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;

@RestController
@RequestMapping("/sse-console")
public class SseConsoleMetaController {
    @GetMapping("/api-list")
    public List<Map<String, Object>> apiList() {
        // 예시: 실제로는 리플렉션 등으로 자동 추출 필요
        List<Map<String, Object>> endpoints = new ArrayList<>();
        Map<String, Object> ep = new HashMap<>();
        ep.put("path", "/sse/test");
        ep.put("method", "GET");
        ep.put("parameters", Arrays.asList(
            Map.of("name", "userId", "type", "string", "required", true, "desc", "유저 ID")
        ));
        ep.put("tag", "테스트");
        ep.put("summary", "SSE 테스트 엔드포인트");
        endpoints.add(ep);
        return endpoints;
    }
}
