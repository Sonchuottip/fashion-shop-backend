package com.example.fashionshopbackend.dto.review;

import lombok.Data;

@Data
public class ReportRequest {
    private Integer userId;
    private Long reviewId;
    private String reason;

}
