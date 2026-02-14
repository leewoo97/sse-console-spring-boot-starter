package io.github.leewoo97;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.stereotype.Controller;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * SSE Console 메타 정보 컨트롤러
 * 
 * 프로젝트 내 모든 SSE 엔드포인트를 자동으로 탐색하여 
 * sse-console.html 페이지에서 사용할 수 있도록 API 목록을 제공합니다.
 */
@RestController
@RequestMapping("/sse-console")
public class SseConsoleMetaController {

    // 메서드 파라미터 이름을 추출하기 위한 유틸리티
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * SSE 엔드포인트 목록 조회 API
     * 
     * 프로젝트 내 모든 컨트롤러를 스캔하여 text/event-stream을 produces하는
     * SSE 엔드포인트만 필터링하여 반환합니다.
     * 
     * @return SSE 엔드포인트 목록 (경로, HTTP 메서드, 파라미터 정보 포함)
     */
    @GetMapping("/api-list")
    public List<Map<String, Object>> apiList() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        
        // Spring에 등록된 모든 핸들러 메서드 정보를 가져옴
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handler = entry.getValue();
            Class<?> beanType = handler.getBeanType();
            
            // RestController 또는 Controller 어노테이션이 있는 클래스만 필터링
            if (!AnnotatedElementUtils.hasAnnotation(beanType, RestController.class) && !AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)) {
                continue;
            }

            // produces에 text/event-stream이 있는지 확인 (SSE 엔드포인트만 선택)
            Set<String> produces = entry.getKey().getProducesCondition().getProducibleMediaTypes().stream().map(MediaType::toString).collect(java.util.stream.Collectors.toSet());
            if (produces.stream().noneMatch(p -> p.contains("text/event-stream"))) {
                continue;
            }

            // HTTP method (GET, POST 등) 추출
            Set<RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
            
            // URL path 패턴 추출
            Set<String> patterns = new LinkedHashSet<>();
            PathPatternsRequestCondition pathPatternsCondition = entry.getKey().getPathPatternsCondition();
            if (pathPatternsCondition != null) {
                patterns.addAll(pathPatternsCondition.getPatternValues());
            }
            if (entry.getKey().getPatternsCondition() != null) {
                patterns.addAll(entry.getKey().getPatternsCondition().getPatterns());
            }
            
            String method = methods.isEmpty() ? "GET" : methods.iterator().next().name();
            String path = patterns.isEmpty() ? "" : patterns.iterator().next();
            if (path.isBlank()) {
                continue;
            }

            // 파라미터 정보 추출 (타입, 필수 여부, 이름 등)
            List<Map<String, Object>> params = new ArrayList<>();
            Method javaMethod = handler.getMethod();
            String[] discoveredNames = PARAMETER_NAME_DISCOVERER.getParameterNames(javaMethod);
            Parameter[] javaParams = javaMethod.getParameters();
            
            for (int i = 0; i < javaParams.length; i++) {
                Parameter param = javaParams[i];
                RequestParam req = param.getAnnotation(RequestParam.class);
                
                // @RequestParam 어노테이션이 있는 파라미터만 처리
                if (req != null) {
                    Map<String, Object> paramInfo = new HashMap<>();

                    // @RequestParam의 name 또는 value 속성에서 파라미터 이름 추출
                    String requestParamName = null;
                    if (req.name() != null && !req.name().isBlank()) requestParamName = req.name();
                    else if (req.value() != null && !req.value().isBlank()) requestParamName = req.value();

                    // 컴파일 시 남아있는 파라미터 이름 추출 (Java 8+ -parameters 옵션 필요)
                    String discovered = (discoveredNames != null && i < discoveredNames.length) ? discoveredNames[i] : null;
                    if (discovered != null && (discovered.equals("arg" + i) || discovered.isBlank())) {
                        discovered = null;
                    }

                    // 최종 파라미터 이름 결정 (우선순위: @RequestParam > 컴파일된 이름 > 리플렉션 이름)
                    String effectiveName = requestParamName != null ? requestParamName : (discovered != null ? discovered : param.getName());
                    
                    paramInfo.put("name", effectiveName);
                    paramInfo.put("type", param.getType().getSimpleName().toLowerCase());
                    paramInfo.put("required", req.required());
                    paramInfo.put("desc", "");
                    params.add(paramInfo);
                }
            }

            // 엔드포인트 정보를 맵으로 구성
            Map<String, Object> ep = new HashMap<>();
            ep.put("path", path);
            ep.put("method", method);
            ep.put("parameters", params);
            ep.put("tag", beanType.getSimpleName()); // 컨트롤러 클래스 이름
            ep.put("summary", handler.getMethod().getName()); // 메서드 이름
            endpoints.add(ep);
        }
        return endpoints;
    }
}
