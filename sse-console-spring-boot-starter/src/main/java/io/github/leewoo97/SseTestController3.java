package io.github.leewoo97;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

@RestController
public class SseTestController3 {
    // 랜덤 숫자 스트림
    @GetMapping(value = "/sse/random", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void randomNumbers(@RequestParam(defaultValue = "5") int count, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            int value = random.nextInt(100);
            writer.write("event: random\n");
            writer.write("data: {\\\"value\\\":" + value + "}\n\n");
            writer.flush();
            Thread.sleep(800);
        }
        writer.write("event: response\n");
        writer.write("data: {\\\"done\\\":true}\n\n");
        writer.flush();
    }

    // 알파벳 스트림
    @GetMapping(value = "/sse/alpha", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void alphaStream(@RequestParam(defaultValue = "A") String start, @RequestParam(defaultValue = "5") int count, HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        char c = start.charAt(0);
        for (int i = 0; i < count; i++) {
            writer.write("event: alpha\n");
            writer.write("data: {\\\"char\\\":\\\"" + (char)(c + i) + "\\\"}\n\n");
            writer.flush();
            Thread.sleep(600);
        }
        writer.write("event: response\n");
        writer.write("data: {\\\"done\\\":true}\n\n");
        writer.flush();
    }

    // 느린 메시지 스트림
    @GetMapping(value = "/sse/slow", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void slowStream(HttpServletResponse response) throws IOException, InterruptedException {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        PrintWriter writer = response.getWriter();
        String[] msgs = {"첫번째", "두번째", "세번째", "네번째", "다섯번째"};
        for (String msg : msgs) {
            writer.write("event: slow\n");
            writer.write("data: {\\\"msg\\\":\\\"" + msg + "\\\"}\n\n");
            writer.flush();
            Thread.sleep(2000);
        }
        writer.write("event: response\n");
        writer.write("data: {\\\"done\\\":true}\n\n");
        writer.flush();
    }
}
