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

    @PostMapping("/translate")
    public ResponseEntity<?> translateText(
            @RequestBody Map<String, String> body // Nhận JSON body
    ) {
        try {
            String text = body.get("text");  // lấy text
            String to = body.get("to");      // lấy ngôn ngữ đích
            String from = body.get("from");  // có thể null
    
            // Tạo URL cho Microsoft Translator
            String url = endpoint + "/translate?api-version=3.0&to=" + to + "&includeAlignment=true";
    
            if (from != null && !from.isEmpty()) {
                url += "&from=" + from;
            }
    
            // Tạo headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", apiKey);
            headers.set("Ocp-Apim-Subscription-Region", region);
    
            // Body JSON cho Microsoft API
            List<Map<String, String>> msBody = Collections.singletonList(
                    Collections.singletonMap("Text", text)
            );
    
            HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(msBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Object> response = restTemplate.postForEntity(url, entity, Object.class);
    
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
