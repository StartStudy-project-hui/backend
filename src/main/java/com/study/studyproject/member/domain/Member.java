package com.study.studyproject.member.domain;

import com.study.studyproject.board.domain.Board;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.global.config.BaseTimeEntity;
import com.study.studyproject.login.dto.SignRequest;
import com.study.studyproject.member.dto.MemberUpdateResDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor
@ToString(of = {"id", "email", "password", "nickname", "username", "role"})
@Table(name = "Member",indexes = @Index(name = "username_idx", columnList = "username"))
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    private String email; // 아이디 
    private String password; //비밀번호

    private String username;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;
    private String socialId;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> board = new ArrayList<>();


    @Builder
    public Member(String email, String password, String username, String nickname, Role role, SocialType socialType, String socialId) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.nickname = nickname;
        this.role = role;
        this.socialType = socialType;
        this.socialId = socialId;
    }






    public static Member toEntity(SignRequest signRequest, String encodePwd) {
        return Member.builder().role(Role.ROLE_USER)
                .username(signRequest.getName())
                .nickname(signRequest.getNickname())
                .password(encodePwd)
                .email(signRequest.getEmail()).build();
    }


    public Member updateMemberInfo(MemberUpdateResDto dto) {
        this.username = dto.getUsername();
        this.nickname = dto.getNickname();
        return this;
    }
}
