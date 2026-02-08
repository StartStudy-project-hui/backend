package com.study.studyproject.ad.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.study.studyproject.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Builder
public class AdContractHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "ad_history_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Ad ad;

    @Column(updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime startDate;

    @Column(updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;

    @Column(updatable = false,nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime change_at;


    @Enumerated(value = EnumType.STRING)
    private AdStatus status;

    @Enumerated(value = EnumType.STRING)
    private AdStatus changeType;

    @Column(nullable = true)
    private String changeReason;







}
