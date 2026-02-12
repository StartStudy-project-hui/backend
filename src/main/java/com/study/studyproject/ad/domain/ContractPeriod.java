package com.study.studyproject.ad.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 *
 * 계약 시작일과 종료일
 */
@Embeddable
@Getter
public class ContractPeriod {

    @Column(updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDate;

    @Column(updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;


    @Builder
    public ContractPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static ContractPeriod from(LocalDateTime startDate, LocalDateTime endDate) {
        return ContractPeriod.builder().
                startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
