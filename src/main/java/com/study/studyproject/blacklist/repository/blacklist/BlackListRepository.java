package com.study.studyproject.blacklist.repository.blacklist;

import com.study.studyproject.blacklist.domain.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlackList, Long>,BlackListRepositoryCustom {


}
