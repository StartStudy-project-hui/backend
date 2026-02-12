package com.study.studyproject.ad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Banner {

    @Id @GeneratedValue
    private Long id;

    private String title;
    private String imageUrl;
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    private BannerPosition position;

    private long viewCount;
    private long clickCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ad_id")
    private Ad ad;




    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseClickCount() {
        this.clickCount++;
    }

}
