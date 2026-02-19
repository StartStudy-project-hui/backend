package com.study.studyproject.blacklist.service;

import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackType;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import com.study.studyproject.blacklist.dto.request.BlackListCreateRequestDto;
import com.study.studyproject.blacklist.dto.request.BlackListUpdateRequestDto;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.global.GlobalResultDto;
import com.study.studyproject.global.Hash.HashUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import static com.study.studyproject.blacklist.domain.BlackType.EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlackListImplUnitTest {

    @Mock
    private BlackListRepository blacklistRepository;

    @InjectMocks
    private BlackListServiceImpl blackListService;

    @Mock
    private BlackListHistoryRepository blackListHistoryRepository;


    @Test
    @DisplayName("블랙리스트 등록 시 정상적으로 저장된다")
    void register_shouldSaveBlacklist() throws NoSuchFieldException {
        // given
        BlackListCreateRequestDto dto = new BlackListCreateRequestDto();
        dto.setRawValue("user@example.com");
        dto.setReason("spam");
        dto.setType(BlackType.EMAIL);

        String hash = HashUtil.sha256(dto.getRawValue());
        BlackList saved = BlackList.create(dto.getType(), dto.getRawValue(), dto.getReason());

        when(blacklistRepository.save(any(BlackList.class))).thenReturn(saved);

        // when
        GlobalResultDto register = blackListService.register(dto);

        // then
        assertThat(register.getStatusCode()).isEqualTo(200);

        // repository 호출 검증
        verify(blacklistRepository).save(any(BlackList.class));
        verify(blackListHistoryRepository).countByHashValueAndAction(anyString(), any());

    }


    @Test
    @DisplayName("블랙리스트 수정 시 사유(reason)가 변경된다")
    void update_shouldChangeReason() {
        BlackList blacklist = BlackList.create(EMAIL, "update@example.com", "spam");

        // findById 호출 시 Mock 객체 반환
        when(blacklistRepository.findById(blacklist.getId())).thenReturn(java.util.Optional.of(blacklist));

        BlackListUpdateRequestDto updateDto = new BlackListUpdateRequestDto("phishing");
        blackListService.update(blacklist.getId(), updateDto);

        assertThat(blacklist.getReason()).isEqualTo("phishing");
    }

    @Test
    @DisplayName("블랙리스트 삭제 시 delete 호출됨")
    void delete_shouldCallDelete() {
        BlackList blacklist = BlackList.create(EMAIL, "delete@example.com", "spam");

        when(blacklistRepository.findById(blacklist.getId())).thenReturn(java.util.Optional.of(blacklist));

        GlobalResultDto delete = blackListService.delete(blacklist.getId());

        assertThat(delete.getStatusCode()).isEqualTo(200);
    }

}
