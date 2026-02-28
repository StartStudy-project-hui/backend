package com.study.studyproject.main.service;

import com.study.studyproject.main.dto.ListResponseDto;
import com.study.studyproject.main.dto.MainRequestDto;
import com.study.studyproject.main.repository.MainQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ListService {

    private final MainQueryRepository mainQueryRepository;

    //조회
    public Page<ListResponseDto> list(String title, MainRequestDto boardListPage, Pageable pageable) {
        return mainQueryRepository.getBoardListPage(title,boardListPage, pageable);
    }

}
