package com.study.studyproject.ad.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ad extends BaseTimeEntity {


    @Id  @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ad_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "ad")
    private List<AdContractHistory> adContractHistories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AdStatus status;

    @OneToMany(mappedBy = "ad")
    private List<Banner> banners = new ArrayList<>();

    @Column(name = "start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime  startDate; //시갖

    @Column(name = "end_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime endDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ad ad = (Ad) o;
        return Objects.equals(id, ad.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Builder
    public Ad(String name, List<AdContractHistory> adContractHistories, AdStatus status, List<Banner> banners, LocalDateTime startDate, LocalDateTime endDate) {
        this.name = name;
        this.adContractHistories = adContractHistories;
        this.status = status;
        this.banners = banners;
        this.startDate = startDate;
        this.endDate = endDate;
    }



    public void addHistory(AdContractHistory history) {
        adContractHistories.add(history);
        history.assignAd(this);
    }




}
