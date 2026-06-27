package com.sm.ratelimiter.service;

import com.sm.ratelimiter.dto.Results;
import com.sm.ratelimiter.dto.TestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TestService {

    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private RedisTokenBucketService bucketService;

    public Mono<ResponseEntity<?>> testApi(int numberOfRequest,int refillToken,String clientId) {
        Map<String, String> requestsResult=new LinkedHashMap<>();
        RestTemplate restTemplate=new RestTemplate();
        String url="http://localhost:8081/api/health";

        bucketService.setRefillToken(refillToken);

        int successCount=0,failureCount=0;

        for(int i=1;i<=numberOfRequest;i++){
                try {
                    ResponseEntity<?> response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            null,
                            Object.class);

                    requestsResult.put("✅ Request " + i,
                            response.getStatusCode().toString());
                    successCount++;

                } catch (HttpClientErrorException.TooManyRequests ex) {

                    requestsResult.put("❌ Request " + i, "429 Too Many Requests");
                    failureCount++;

                } catch (Exception ex) {

                    requestsResult.put("❗ Request " + i, ex.getMessage());
                }

        }

        Results results=new Results();
        results.setSuccessful_Requests(successCount);
        results.setFailed_Requests(failureCount);
        results.setInitial_Tokens(rateLimiterService.getCapacity(clientId));
        results.setFinal_Tokens(rateLimiterService.getAvailableTokens(clientId));


        TestResponse testResponse=new TestResponse();
        testResponse.setRequestsResult(requestsResult);
        testResponse.setResults(results);

        return Mono.just(ResponseEntity.ok(testResponse));
    }
}
