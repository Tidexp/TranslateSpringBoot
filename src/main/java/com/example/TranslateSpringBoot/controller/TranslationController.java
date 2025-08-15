package com.example.TranslateSpringBoot.controller;

// Import các class cần thiết từ Spring và Java
import org.springframework.beans.factory.annotation.Value;      // Dùng để inject giá trị từ application.properties
import org.springframework.http.*;                           // Dùng cho HttpHeaders, MediaType, HttpEntity, ResponseEntity, HttpStatus
import org.springframework.web.bind.annotation.*;           // Dùng cho @RestController, @RequestMapping, @PostMapping, @RequestParam
import org.springframework.web.client.RestTemplate;         // Dùng để gửi HTTP request (POST/GET) đến API bên ngoài

import org.springframework.http.HttpHeaders;
import java.util.*;                                         // Dùng cho List, Map, Collections

// Khai báo class controller
@RestController  // Đánh dấu đây là REST controller, trả JSON trực tiếp, không render view (html hiển thị)
@RequestMapping("/api") // Tất cả endpoint trong class này sẽ có tiền tố /api
public class TranslationController {
    // Lấy giá trị từ biến môi trường (Environment Variables) trên Render
    @Value("${TRANSLATOR_KEY}")
    private String apiKey;               // Microsoft Translator API key

    @Value("${TRANSLATOR_REGION}")
    private String region;               // Region của subscription

    @Value("${TRANSLATOR_ENDPOINT}")
    private String endpoint;             // Endpoint API

    // Định nghĩa endpoint POST /api/translate
    @PostMapping("/translate")
    public ResponseEntity<?> translateText(
            @RequestParam String text,   // Text cần dịch
            @RequestParam String to      // Ngôn ngữ đích (ví dụ: "vi" cho tiếng Việt)
    ) {
        try {
            // Tạo URL đầy đủ cho API Microsoft Translator
            String url = endpoint + "/translate?api-version=3.0&to=" + to;

            // Tạo headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);       // Content-Type = application/json
            headers.set("Ocp-Apim-Subscription-Key", apiKey);        // Key API
            headers.set("Ocp-Apim-Subscription-Region", region);     // Region của key

            // Tạo body JSON theo format mà Microsoft Translator yêu cầu
            // Body là List<Map> vì API nhận mảng JSON với mỗi phần tử có key "Text"
            List<Map<String, String>> body = Collections.singletonList(
                    Collections.singletonMap("Text", text)
            );

            // Gói headers + body thành HttpEntity để gửi đi
            HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(body, headers);

            // Tạo RestTemplate để gửi HTTP request
            RestTemplate restTemplate = new RestTemplate();

            // Gửi POST request tới API và nhận response dạng Object
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);

            // Trả thẳng response body về client với HTTP 200 OK
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            // Nếu có lỗi, trả HTTP 500 kèm thông báo lỗi dạng JSON
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
