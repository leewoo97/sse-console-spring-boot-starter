package io.github.leewoo97;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class SseTestController {
    @GetMapping(value = "/sse/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void sseTest(@RequestParam String userId, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 1; i <= 5; i++) {
            writer.write("event: message\n");
            writer.write("data: {\"msg\":\"Hello, " + userId + ", count: " + i + "\"}\n\n");
            writer.flush();
            Thread.sleep(1000);
        }
        writer.write("event: response\n");
        writer.write("data: {\"done\":true}\n\n");
        writer.flush();
    }
}
