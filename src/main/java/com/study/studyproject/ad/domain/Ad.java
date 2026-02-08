package com.study.studyproject.ad.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@NoArgsConstructor
public class Ad extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "ad_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "board_id")
    private List<Board> board = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AdStatus status;

    @Column(name = "start_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime  startDate;

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
}
