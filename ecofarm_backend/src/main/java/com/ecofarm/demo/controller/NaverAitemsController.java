package com.ecofarm.demo.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecofarm.demo.dto.AitemsDTO;
import com.ecofarm.demo.dto.ApartmentDTO;
import org.apache.commons.lang3.SystemProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/aitems")
public class NaverAitemsController {
    /* AiTEMS에 대한 설명은 아래 주소 참고
       https://api.ncloud-docs.com/docs/ai-application-service-aitems    */
    private static final String API_URL = "https://aitems.apigw.ntruss.com";

    /* 로그인 후 아래 URL 로 방문하면 확인 가능
       마이페이지 - 계정관리 - 인증키 관리
       https://www.ncloud.com/mypage/manage/authkey    */
    @Value("${ncp.access-key}")
    private String ACCESS_KEY;
    @Value("${ncp.secret-key}")
    private String SECRET_KEY;

    /*  GET 방식 호출일때는 아래처럼, POST는 method=post로 변경
        http://localhost:8080/napi?method=get

        기본 조회 후 serviceId 파라미터를 추가하여 다른 API 호출 수행
        http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb

        학습 목록 조회 GET http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb/trains
         -- http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/trains
        학습 결과 조회 GET https://aitems.apigw.ntruss.com/api/v1/services/{serviceId}/trains/{trainType}/versions/{version}
            * 학습유형 trainType: personalRecommend, relatedItem, pop 중에서 선택
            * 학습버전 versions: (예: zsnlub0z0ei)
            * 호출 예: http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb/trains/personalRecommend/versions/zsnlub0z0ei
        --인기항목추천 : http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/trains/personalRecommend/versions/pa29h7b822d

        학습 결과 테스트 GET https://aitems.apigw.ntruss.com/api/v1/services/{serviceId}/infers/lookup?type={type}&targetId={targetId}
            * 학습유형 type: personalRecommend, relatedItem, pop 중에서 선택
            * 조회대상ID targetId:
            * 호출 예: http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb/infers/lookup?type=personalRecommend&targetId=user_2
                HTML특수문자 변환: http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb%2Finfers%2Flookup%3Ftype%3DpersonalRecommend%26targetId%3Duser_2
                http://localhost:8080/napi?method=get&serviceId=lp73r7pbdvb/infers/lookup%3Ftype%3DpersonalRecommend%26targetId%3Duser_2
     */

    /*
    --학습목록 : http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/trains
    --학습결과조회 : http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/trains/pop/versions/z8ulmqva4t8
    --개인화추천   : http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/infers/lookup%3Ftype%3DpersonalRecommend%26targetId%3Duser_22
                   http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/infers/lookup%3Ftype%3DpersonalRecommend%26targetId%3Duser_22
    --개인화 >> 소비자 추천 물품
    -- 인기 항목 추천 >>
    --인기항목추천 : http://localhost:8080/api/aitems/napi?method=get&serviceId=8jthvpwt3yp/infers/lookup%3Ftype%3Dpop%26targetId%3D40
     */

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> naverAitems(@RequestBody AitemsDTO aitemsDTO) {

        // DTO에서 데이터 추출
        String productGroup = aitemsDTO.getProductGroup();
        String ageGroup = aitemsDTO.getAgeGroup();
        List<String> diseases = aitemsDTO.getDiseases();
        List<ResponseEntity<String>> responses = new ArrayList<>();

        if(productGroup != null && !productGroup.isEmpty()) {
            String type = "relatedItem";
            String serviceId = "zcwon4ujjwx";
            String apiPath = String.format("/api/v1/services/%s/infers/lookup?type=%s&targetId=%s",
                    serviceId, type, productGroup);
            String url = API_URL + apiPath;

            HttpMethod httpMethod = HttpMethod.GET;

            RestTemplate restTemplate = new RestTemplate();
            long timestamp = Instant.now().toEpochMilli();
            String signature = createSignature(timestamp, apiPath, httpMethod.name());

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
            headers.set("x-ncp-iam-access-key", ACCESS_KEY);
            headers.set("x-ncp-apigw-signature-v2", signature);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            System.out.println("연관 물품");
            System.out.println(response.getBody());

            responses.add(response);

//            return ResponseEntity.status(response.getStatusCode())
//                    .headers(response.getHeaders())
//                    .body(response.getBody());
        }

        if(ageGroup != null && !ageGroup.isEmpty()) {
            String type = "pop";
            String serviceId = "fffi5prl5m1";
            String apiPath = String.format("/api/v1/services/%s/infers/lookup?type=%s&targetId=%s",
                    serviceId, type, ageGroup);
            String url = API_URL + apiPath;

            HttpMethod httpMethod = HttpMethod.GET;

            RestTemplate restTemplate = new RestTemplate();
            long timestamp = Instant.now().toEpochMilli();
            String signature = createSignature(timestamp, apiPath, httpMethod.name());

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
            headers.set("x-ncp-iam-access-key", ACCESS_KEY);
            headers.set("x-ncp-apigw-signature-v2", signature);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

            System.out.println("연령별 인기 항목");
            System.out.println(response.getBody());

            responses.add(response);
        }

        if(diseases != null && !diseases.isEmpty()) {
            System.out.println("관심질환");
            System.out.println(diseases);

            for (String disease : diseases) {
                String type = "pop";
                String serviceId = "12sv8z2cs96";
                try {
                    String encodedDisease = URLEncoder.encode(disease, StandardCharsets.UTF_8);
                    String apiPath = String.format("/api/v1/services/%s/infers/lookup?type=%s&targetId=%s",
                            serviceId, type, encodedDisease);
                    String url = API_URL + apiPath;
                    System.out.println("관심질환 : " + disease);
                    System.out.println(url);
                    HttpMethod httpMethod = HttpMethod.GET;

                    RestTemplate restTemplate = new RestTemplate();
                    long timestamp = Instant.now().toEpochMilli();
                    String signature = createSignature(timestamp, apiPath, httpMethod.name());

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
                    headers.set("x-ncp-iam-access-key", ACCESS_KEY);
                    headers.set("x-ncp-apigw-signature-v2", signature);
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> entity = new HttpEntity<>(null, headers);
                    ResponseEntity<String> response = restTemplate.exchange(url, httpMethod, entity, String.class);

                    System.out.println("질환별 인기 항목");
                    System.out.println(response.getBody());

                    responses.add(response);
                } catch (Exception e) {
                    System.out.println(disease + " encode error : " + e.getMessage());
                }
            }
        }

        return combineResponses(responses);
    }

    @PreAuthorize("hasRole('CONSUMER') or hasRole('SELLER') or hasRole('ADMIN')")
    @RequestMapping(value = "/napi", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> naverCloudApi(
            @RequestParam String method,
            @RequestParam(required = false, defaultValue = "") String serviceId,
            @RequestBody(required = false) String body) {

        HttpMethod httpMethod;
        if ("get".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.GET;
        } else if ("post".equalsIgnoreCase(method)) {
            httpMethod = HttpMethod.POST;
        } else {
            return ResponseEntity.badRequest().body("Invalid method. Use 'get' or 'post'.");
        }

        String apiPath = "/api/v1/services" + ("".equals(serviceId) ? "" : "/" + serviceId);
        String fullUrl = API_URL + apiPath;

        System.out.println(API_URL);
        System.out.println(apiPath);

        RestTemplate restTemplate = new RestTemplate();
        long timestamp = Instant.now().toEpochMilli();
        String signature = createSignature(timestamp, apiPath, httpMethod.name());

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-ncp-apigw-timestamp", String.valueOf(timestamp));
        headers.set("x-ncp-iam-access-key", ACCESS_KEY);
        headers.set("x-ncp-apigw-signature-v2", signature);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(fullUrl, httpMethod, entity, String.class);

        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }

    private String createSignature(long timestamp, String url, String method) {
        String space = " ";
        String newLine = "\n";

        String message = new StringBuilder()
                .append(method)
                .append(space)
                .append(url)
                .append(newLine)
                .append(timestamp)
                .append(newLine)
                .append(ACCESS_KEY)
                .toString();

        try {
            SecretKeySpec signingKey = new SecretKeySpec(SECRET_KEY.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    public ResponseEntity<String> combineResponses(List<ResponseEntity<String>> responses) {

        if (responses == null || responses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Integer> weightMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (int i = 0; i < responses.size(); i++) {
            ResponseEntity<String> response = responses.get(i);
            try {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode valuesNode = jsonNode.get("values");

                int listLength = valuesNode.size(); // 현재 리스트의 길이

                for (int j = 0; j < valuesNode.size(); j++) {
                    String value = valuesNode.get(j).asText();
                    // 가중치 부여 (listLength - j)로 가중치 설정
                    weightMap.put(value, weightMap.getOrDefault(value, 0) + (listLength - j));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 가중치에 따라 정렬
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(weightMap.entrySet());
        sortedEntries.sort((entry1, entry2) -> {
            int compare = entry2.getValue().compareTo(entry1.getValue()); // 내림차순 정렬
            if (compare == 0) {
                // 가중치가 같은 경우, 원래 순서 유지
                return weightMap.keySet().stream().toList().indexOf(entry1.getKey()) - weightMap.keySet().stream().toList().indexOf(entry2.getKey());
            }
            return compare;
        });

        // 결과 JSON 생성
        List<String> mergedValues = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            mergedValues.add(entry.getKey());
        }

        // 최종 JSON 구조 생성
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("field", "ITEM_ID");
        resultMap.put("values", mergedValues);

        System.out.println("combined");
        System.out.println(resultMap);

        try {
            String resultJson = objectMapper.writeValueAsString(resultMap);
            return ResponseEntity.ok(resultJson);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating JSON response");
        }
    }

}
