package com.swiggysystem.assignmentservice.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RiderCandidate {
    private String riderId;
    private double distanceKm;
    private double score;
}