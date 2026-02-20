package com.study.studyproject.blacklist.repository.blacklist;

import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList, Long>,BlackListRepositoryCustom {


    Optional<BlackList> findByHashValueAndType(String hash, BlackType type);

    boolean existsByHashValue(String hashValue);

    Optional<BlackList> findByHashValue(String hash);
}
