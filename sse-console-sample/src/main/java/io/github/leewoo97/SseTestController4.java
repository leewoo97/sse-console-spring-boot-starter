package io.github.leewoo97;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class SseTestController4 {
    // JSON 오브젝트 스트림
    @GetMapping(value = "/sse/json", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void jsonStream(@RequestParam(defaultValue = "3") int count, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 1; i <= count; i++) {
            writer.write("event: json\n");
            writer.write("data: {\"id\":" + i + ",\"name\":\"user" + i + "\"}\n\n");
            writer.flush();
            Thread.sleep(900);
        }
        writer.write("event: response\n");
        writer.write("data: {\"done\":true}\n\n");
        writer.flush();
    }

    // 빠른 메시지 스트림
    @GetMapping(value = "/sse/fast", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void fastStream(@RequestParam(defaultValue = "5") int count, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 1; i <= count; i++) {
            writer.write("event: fast\n");
            writer.write("data: {\"msg\":\"빠른 메시지 " + i + "\"}\n\n");
            writer.flush();
            Thread.sleep(200);
        }
        writer.write("event: response\n");
        writer.write("data: {\"done\":true}\n\n");
        writer.flush();
    }

    // 커스텀 이벤트 이름
    @GetMapping(value = "/sse/custom-event", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void customEvent(@RequestParam(defaultValue = "eventX") String event, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 1; i <= 3; i++) {
            writer.write("event: " + event + "\n");
            writer.write("data: {\"index\":" + i + "}\n\n");
            writer.flush();
            Thread.sleep(1000);
        }
        writer.write("event: response\n");
        writer.write("data: {\"done\":true}\n\n");
        writer.flush();
    }
}
