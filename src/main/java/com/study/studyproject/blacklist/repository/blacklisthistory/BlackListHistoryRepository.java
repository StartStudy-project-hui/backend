package com.study.studyproject.blacklist.repository.blacklisthistory;

import com.study.studyproject.blacklist.domain.BlackListHistory;
import com.study.studyproject.blacklist.domain.BlacklistAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlackListHistoryRepository extends JpaRepository<BlackListHistory, Long> , BlackListHistoryRepositoryCustom {

    long countByHashValueAndAction(String hash, BlacklistAction blacklistAction);


}
