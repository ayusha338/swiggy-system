package com.swiggysystem.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiderCandidateResponse {
    private String riderId;
    private double distanceKm;
    private double score;
}