package io.github.leewoo97;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@RestController
public class SseTestController2 {
    // 단순 카운트 업 이벤트
    @GetMapping(value = "/sse/counter", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void counter(@RequestParam(defaultValue = "3") int count, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 1; i <= count; i++) {
            writer.write("event: count\n");
            writer.write("data: {\\\"value\\\":" + i + "}\n\n");
            writer.flush();
            Thread.sleep(700);
        }
        writer.write("event: response\n");
        writer.write("data: {\\\"done\\\":true}\n\n");
        writer.flush();
    }

    // 에러 이벤트 예시
    @GetMapping(value = "/sse/error-demo", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void errorDemo(@RequestParam(defaultValue = "false") boolean fail, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        if (fail) {
            writer.write("event: error\n");
            writer.write("data: {\\\"error\\\":\\\"Something went wrong!\\\"}\n\n");
            writer.flush();
            return;
        }
        writer.write("event: message\n");
        writer.write("data: {\\\"msg\\\":\\\"No error!\\\"}\n\n");
        writer.flush();
    }

    // 실시간 시간 전송
    @GetMapping(value = "/sse/time", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void timeStream(@RequestParam(defaultValue = "5") int times, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        for (int i = 0; i < times; i++) {
            writer.write("event: time\n");
            writer.write("data: {\\\"now\\\":\\\"" + java.time.LocalTime.now() + "\\\"}\n\n");
            writer.flush();
            Thread.sleep(1000);
        }
        writer.write("event: response\n");
        writer.write("data: {\\\"done\\\":true}\n\n");
        writer.flush();
    }
}
