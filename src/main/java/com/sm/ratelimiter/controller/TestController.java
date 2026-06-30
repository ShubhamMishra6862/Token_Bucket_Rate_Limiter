package com.sm.ratelimiter.controller;

import com.sm.ratelimiter.service.ProdTestExecutor;
import com.sm.ratelimiter.service.TestApi;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Tag(name = "Ratelimiter Test")
@Slf4j
@RestController
@RequestMapping("/api")
public class TestController {

    @Autowired
    private StatusController statusController;
    @Autowired
    private TestApi testApi;

    @GetMapping
    public Mono<ResponseEntity<?>> test(
            @RequestParam(defaultValue = "10") int numberOfRequest,
            @RequestParam(defaultValue = "5") int refillToken,
            ServerWebExchange exchange) {

        try {
            String clientId = statusController.getClientId(exchange);
            return testApi.execute(numberOfRequest, refillToken, clientId);
        } catch (Exception e) {
            log.error("Error occurred: {}", e.getMessage(), e);
            return Mono.just(
                    ResponseEntity.badRequest()
                            .body("Error occurred:"+e.getMessage()));
        }
    }
}