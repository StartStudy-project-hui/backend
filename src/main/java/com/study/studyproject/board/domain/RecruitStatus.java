package com.study.studyproject.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum RecruitStatus {

    RECRUITING("모집중"),
    COMPLETED("모집완료");

    private final String description;
}