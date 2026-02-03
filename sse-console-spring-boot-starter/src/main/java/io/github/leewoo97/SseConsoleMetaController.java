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

@RestController
@RequestMapping("/sse-console")
public class SseConsoleMetaController {

    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/api-list")
    public List<Map<String, Object>> apiList() {
        List<Map<String, Object>> endpoints = new ArrayList<>();
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handler = entry.getValue();
            Class<?> beanType = handler.getBeanType();
            // RestController 또는 Controller만
            if (!AnnotatedElementUtils.hasAnnotation(beanType, RestController.class) && !AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)) {
                continue;
            }

            // produces에 text/event-stream이 있는지 확인
            Set<String> produces = entry.getKey().getProducesCondition().getProducibleMediaTypes().stream().map(MediaType::toString).collect(java.util.stream.Collectors.toSet());
            if (produces.stream().noneMatch(p -> p.contains("text/event-stream"))) {
                continue;
            }

            // HTTP method, path 추출
            Set<RequestMethod> methods = entry.getKey().getMethodsCondition().getMethods();
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

            // 파라미터 정보 추출
            List<Map<String, Object>> params = new ArrayList<>();
            Method javaMethod = handler.getMethod();
            String[] discoveredNames = PARAMETER_NAME_DISCOVERER.getParameterNames(javaMethod);
            Parameter[] javaParams = javaMethod.getParameters();
            for (int i = 0; i < javaParams.length; i++) {
                Parameter param = javaParams[i];
                RequestParam req = param.getAnnotation(RequestParam.class);
                if (req != null) {
                    Map<String, Object> paramInfo = new HashMap<>();

                    String requestParamName = null;
                    if (req.name() != null && !req.name().isBlank()) requestParamName = req.name();
                    else if (req.value() != null && !req.value().isBlank()) requestParamName = req.value();

                    String discovered = (discoveredNames != null && i < discoveredNames.length) ? discoveredNames[i] : null;
                    if (discovered != null && (discovered.equals("arg" + i) || discovered.isBlank())) {
                        discovered = null;
                    }

                    String effectiveName = requestParamName != null ? requestParamName : (discovered != null ? discovered : param.getName());
                    paramInfo.put("name", effectiveName);
                    paramInfo.put("type", param.getType().getSimpleName().toLowerCase());
                    paramInfo.put("required", req.required());
                    paramInfo.put("desc", "");
                    params.add(paramInfo);
                }
            }

            Map<String, Object> ep = new HashMap<>();
            ep.put("path", path);
            ep.put("method", method);
            ep.put("parameters", params);
            ep.put("tag", beanType.getSimpleName());
            ep.put("summary", handler.getMethod().getName());
            endpoints.add(ep);
        }
        return endpoints;
    }
}
