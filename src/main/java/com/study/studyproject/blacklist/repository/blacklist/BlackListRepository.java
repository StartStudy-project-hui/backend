package com.study.studyproject.blacklist.repository.blacklist;

import com.study.studyproject.blacklist.domain.BlackList;
import com.study.studyproject.blacklist.domain.BlackType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlackListRepository extends JpaRepository<BlackList, Long>,BlackListRepositoryCustom {


    Optional<BlackList> findByHashValueAndType(String hash, BlackType type);

    boolean existsByHashValue(String hashValue);

    Optional<BlackList> findByHashValue(String hash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BlackList b WHERE b.hashValue = :hash")
    Optional<BlackList> findByHashValueWithLock(@Param("hash") String hash);
}
