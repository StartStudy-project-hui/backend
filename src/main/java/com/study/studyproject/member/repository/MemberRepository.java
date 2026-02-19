package com.study.studyproject.member.repository;


import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.domain.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByNickname(String nickname);

    @Query("select m.email from Member m where m.nickname = :nickname")
    Optional<String> findEmailByNickname(@Param("nickname")  String nickname);
    @Query("select m.email from Member m where m.id = :id")
    Optional<String> findEmailById(@Param("id") Long id);


    boolean existsByNickname(String nickname);

    Optional<Member> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
