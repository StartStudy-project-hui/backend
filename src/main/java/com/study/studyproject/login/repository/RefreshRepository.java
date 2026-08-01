package com.study.studyproject.login.repository;


import com.study.studyproject.login.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;


import java.util.Optional;

public interface RefreshRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByAccessToken(String accessToken);

}
