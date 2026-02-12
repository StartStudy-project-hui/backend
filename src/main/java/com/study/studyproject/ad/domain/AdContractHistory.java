package com.study.studyproject.ad.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.study.studyproject.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ad_contract_history")
public class AdContractHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue
    @Column(name = "ad_history_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Ad ad;


    @Embedded
    private ContractPeriod period;

    @Column(updatable = false, nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime changedAt;


    @Enumerated(value = EnumType.STRING)
    private AdStatus status;

    @Enumerated(value = EnumType.STRING)
    private AdChangeType changeType;

    @Column(nullable = true)
    private String changeReason;

    @Builder(access = AccessLevel.PRIVATE)
    public AdContractHistory(
            Ad ad,
            ContractPeriod period,
            LocalDateTime changedAt,
            AdStatus status,
            AdChangeType changeType,
            String changeReason
    ) {
        this.ad = ad;
        this.period = period;
        this.changedAt = changedAt;
        this.status = status;
        this.changeType = changeType;
        this.changeReason = changeReason;
    }


    public static AdContractHistory create(Ad ad,
                                           ContractPeriod period,
                                           LocalDateTime changedAt,
                                           AdStatus status,
                                           AdChangeType changeType,
                                           String changeReason
    ) {

        AdContractHistory adContractHistory = AdContractHistory.builder()
                .changedAt(changedAt)
                .status(status)
                .period(period)
                .changeType(changeType)
                .changeReason(changeReason)
                .build();

        adContractHistory.assignAd(ad);
        return adContractHistory;
    }

    public void assignAd(Ad ad) {
        this.ad = ad;
        ad.getAdContractHistories().add(this);
    }


}
