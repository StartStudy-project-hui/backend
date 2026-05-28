package com.study.studyproject.board.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;

@Embeddable
@Getter
public class OfflineLocation {

    private double x;
    private double y;


    protected OfflineLocation() {
    }


    @Builder
    public OfflineLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static OfflineLocation create(double getX, double getY) {
        return OfflineLocation.builder()
                .x(getX)
                .y(getY)
                .build();
    }



}
