package com.sm.ratelimiter.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public  interface TestApi {
    Mono<ResponseEntity<?>> execute(
            int numberOfRequest,
            int refillToken,
            String clientId
    );
}
