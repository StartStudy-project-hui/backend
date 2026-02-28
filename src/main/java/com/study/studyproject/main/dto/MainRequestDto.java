package com.study.studyproject.main.dto;

import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MainRequestDto {

    Category category;

    int order; // 0 -> 최신순

    ConnectionType connectionType;

    @Builder
    public MainRequestDto(Category category, Integer order, ConnectionType connectionType) {
        this.category = category;
        this.order = order;
        this.connectionType = connectionType;
    }
}
