package io.github.leewoo97;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HttpTestController {

    // GET - 쿼리 파라미터
    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(required = false) String name) {
        return Map.of("message", "Hello, " + (name != null ? name : "World") + "!");
    }

    // GET - Path Variable
    @GetMapping("/users/{id}")
    public Map<String, Object> getUser(@PathVariable Long id) {
        return Map.of("id", id, "name", "User " + id, "email", "user" + id + "@example.com");
    }

    // GET - 목록 조회
    @GetMapping("/users")
    public List<Map<String, Object>> listUsers(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return List.of(
            Map.of("id", 1, "name", "Alice"),
            Map.of("id", 2, "name", "Bob"),
            Map.of("id", 3, "name", "Charlie")
        );
    }

    // POST - JSON body
    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body) {
        return Map.of("id", 42, "name", body.getOrDefault("name", ""), "created", true);
    }

    // PUT - Path Variable + JSON body
    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return Map.of("id", id, "name", body.getOrDefault("name", ""), "updated", true);
    }

    // DELETE - Path Variable
    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        return Map.of("id", id, "deleted", true);
    }

    // GET - 대용량 데이터 (10,000건)
    @GetMapping("/bulk")
    public List<Map<String, Object>> bulkData(@RequestParam(defaultValue = "10000") int count) {
        List<Map<String, Object>> result = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i);
            item.put("name", "Item-" + i);
            item.put("value", i * 3.14);
            item.put("active", i % 2 == 0);
            item.put("category", "category-" + (i % 10));
            result.add(item);
        }
        return result;
    }
}
