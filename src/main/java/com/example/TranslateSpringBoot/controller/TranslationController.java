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
    public ResponseEntity<?> translateText(@RequestBody Map<String, String> body) {
        try {
            String text = body.get("text");
            String to = body.get("to");
            String from = body.get("from");
    
            String url = endpoint + "/translate?api-version=3.0&to=" + to + "&includeAlignment=true";
            if (from != null && !from.isEmpty()) url += "&from=" + from;
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", apiKey);
            headers.set("Ocp-Apim-Subscription-Region", region);
    
            List<Map<String, String>> msBody = Collections.singletonList(Collections.singletonMap("Text", text));
            HttpEntity<List<Map<String, String>>> entity = new HttpEntity<>(msBody, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.POST, entity, (Class<List<Map<String, Object>>>) (Class<?>) List.class);
    
            List<Map<String, Object>> bodyRes = response.getBody();
            Map<String, Object> translations = (Map<String, Object>) ((List) bodyRes.get(0).get("translations")).get(0);
    
            String translatedText = (String) translations.get("text");
            String detectedLang = bodyRes.get(0).containsKey("detectedLanguage") ? (String) ((Map)bodyRes.get(0).get("detectedLanguage")).get("language") : null;
    
            Object alignmentObj = translations.get("alignment");
            String alignment = null;
            
            if (alignmentObj instanceof Map) {
                Map<?, ?> alignmentMap = (Map<?, ?>) alignmentObj;
                alignment = (String) alignmentMap.get("proj"); // lấy chuỗi proj
            }

            Map<String, Object> result = new HashMap<>();
            result.put("translatedText", translatedText);
            result.put("detectedLanguage", detectedLang);
            result.put("alignment", alignment);  // ✅ thêm dòng này
            
            return ResponseEntity.ok(result);
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
