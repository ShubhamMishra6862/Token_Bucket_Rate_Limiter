package com.sm.ratelimiter.service;

import com.sm.ratelimiter.config.RateLimiterProperties;
import com.sm.ratelimiter.dto.Results;
import com.sm.ratelimiter.dto.TestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;

@Profile("prod")
@Service
public class ProdTestExecutor implements TestApi{

    @Autowired
    private RateLimiterService rateLimiterService;
    @Autowired
    private RedisTokenBucketService bucketService;
    @Autowired
    private RateLimiterProperties config;

    @Override
    public Mono<ResponseEntity<?>> execute(int numberOfRequest,int refillToken,String clientId) {
        if(numberOfRequest>50){
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Input: numberOfRequest<=50"));
        }
        Map<String, String> requestsResult=new LinkedHashMap<>();

        bucketService.setRefillToken(refillToken);

        int successCount=0,failureCount=0;

        for(int i=1;i<=numberOfRequest;i++){
            boolean allowed = rateLimiterService.isAllowed(clientId);

            if(allowed) {
                successCount++;
                requestsResult.put("✅ Request " + i, "200 OK");
            } else {
                failureCount++;
                requestsResult.put("❌ Request " + i, "429 Too Many Requests");
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
