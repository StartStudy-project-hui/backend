package com.study.studyproject.board.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    ETC("기타"),
    CS("CS"),
    ALL("전체"),
    CODING_TEST("코테"),
    PROJECT("프로젝트");

    private final String description;
}