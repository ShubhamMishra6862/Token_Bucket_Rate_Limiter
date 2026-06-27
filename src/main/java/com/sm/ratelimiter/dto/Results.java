package com.sm.ratelimiter.dto;

import lombok.Data;

@Data
public class Results {
    private Integer Successful_Requests;
    private Integer Failed_Requests;
    private Long Initial_Tokens;
    private Long Final_Tokens;
}
