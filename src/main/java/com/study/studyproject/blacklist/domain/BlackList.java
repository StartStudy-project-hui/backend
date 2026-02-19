package com.study.studyproject.blacklist.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlackList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blacklist_id")
    private Long id;

    // EMAIL, PHONE
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlackType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    BlacklistStatus status;

    @OneToMany(mappedBy = "blacklist")
    private List<BlackListHistory> histories = new ArrayList<>();

    // 해시된 값 -
    @Column(nullable = false)
    private String hashValue;

    @NotNull(message = "값을 입력해주세요")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime expireAt;

    @Builder(access = AccessLevel.PRIVATE)
    public BlackList(BlackType type, BlacklistStatus status, String hashValue, String reason, LocalDateTime createdAt) {
        this.type = type;
        this.status = status;
        this.hashValue = hashValue;
        this.reason = reason;
        this.createdAt = createdAt;
    }



    public static BlackList create(BlackType type, String hashValue, String reason) {
        return BlackList.builder()
                .type(type)
                .hashValue(hashValue)
                .reason(reason)
                .status(BlacklistStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }


    public boolean isNew() {
        return this.id == null;
    }

    public boolean existsInDB() {
        return !isNew();
    }

    // 히스토리 연결 메서드
    public void addHistory(BlackListHistory history) {
        histories.add(history);
        history.addBlacklist(this);
    }



    public void updateReason(String reason) {
        this.reason = reason;
    }

    public BlacklistStatus setDuration(int months, long violationCount) {
        if (violationCount >= 3 || months <= 0) { // 누적 4회 이상 또는 기간 0개월이면 영구정지
            this.expireAt = null;
            this.status = BlacklistStatus.PERMANENT;
        } else { // 일반 기간 정지
            this.expireAt = createdAt.plusMonths(months);
            this.status = BlacklistStatus.ACTIVE;
        }
        return this.status;
    }


}
