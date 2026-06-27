package com.sm.ratelimiter.dto;

import lombok.Data;

import java.util.Map;

@Data
public class TestResponse {
    private Map<String, String> requestsResult;
    private Results results;
}
