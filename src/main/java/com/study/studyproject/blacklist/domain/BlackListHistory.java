package com.study.studyproject.blacklist.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class BlackListHistory   {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blacklist_history_id")
    private Long id;
    @CreatedBy
    private String createBy;

    private String hashValue;
    private String reason;


    @Enumerated(EnumType.STRING)
    private BlackType type;

    @Enumerated(EnumType.STRING)
    private BlacklistStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blacklist_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private BlackList blacklist;


    @Enumerated(EnumType.STRING)
    private BlacklistAction action;

    private LocalDateTime createAt = LocalDateTime.now();


    @Builder(access =  AccessLevel.PRIVATE)
    public BlackListHistory(String hashValue, BlackType type, String reason, BlacklistStatus status, BlacklistAction action, LocalDateTime actionAt) {
        this.hashValue = hashValue;
        this.type = type;
        this.reason = reason;
        this.status = status;
        this.action = action;
        this.createAt = actionAt;
    }


    public static BlackListHistory create(BlacklistAction action, BlackList blacklist, String hashValue , BlackType type, String reason, BlacklistStatus status ) {
        BlackListHistory blackListHistory = BlackListHistory.builder()
                .action(action)
                .reason(reason)
                .type(type)
                .hashValue(hashValue)
                .status(status)
                .actionAt(LocalDateTime.now())
                .build();
        if (blacklist != null) {
            blacklist.addHistory(blackListHistory);
        }
        return blackListHistory;
    }


    public void addBlacklist(BlackList blackList) {
        this.blacklist = blackList;
    }
}
