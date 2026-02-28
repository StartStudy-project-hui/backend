package com.study.studyproject;

import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.OfflineLocation;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.Hash.HashUtil;
import com.study.studyproject.login.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile({"local", "prod", "dev"})
@RequiredArgsConstructor
public class InitData {

    private final InitService initService;

    @PostConstruct
    void init() {
        initService.init();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {

        private final BoardRepository boardRepository;
        private final MemberRepository memberRepository;
        private final ReplyRepository replyRepository;
        private final PostLikeRepository postLikeRepository;
        private final PasswordEncoder passwordEncoder;
        private final BlackListRepository blackListRepository;
        private final BlackListHistoryRepository blackListHistoryRepository;

        public void init() {
            Member user = Member.builder()
                    .role(Role.ROLE_USER)
                    .username("김하임")
                    .email("kimSky@naver.com")
                    .nickname("kimSky")
                    .password(passwordEncoder.encode("Y@3r9o$7k"))
                    .build();
            memberRepository.save(user);

            Member admin = Member.builder()
                    .role(Role.ROLE_ADMIN)
                    .username("김일우")
                    .email("admin@naver.com")
                    .nickname("admin")
                    .password(passwordEncoder.encode("Y@3r9o$7aaak"))
                    .build();
            memberRepository.save(admin);

            Member user2 = Member.builder()
                    .role(Role.ROLE_USER)
                    .username("김지우")
                    .email("huj@naver.com")
                    .nickname("huj")
                    .password(passwordEncoder.encode("Y@3r9o$7kff"))
                    .build();
            memberRepository.save(user2);

            createBlackList(user.getEmail(), "피싱", BlacklistStatus.ACTIVE);
            createBlackList(admin.getEmail(), "스팸", BlacklistStatus.PERMANENT);
            createBlackList(user2.getEmail(), "욕설", BlacklistStatus.ACTIVE);

            Member member = memberRepository.findById(1L).get();
            Category[] categories = {Category.CS, Category.기타, Category.코테};
            String[] regions = {"서울", "인천", "경기"};

            for (int i = 1; i <= 15; i++) {
                int val = (int) (Math.random() * 3);
                Board board = Board.builder()
                        .content("같이 " + categories[val] + " 같이해요")
                        .category(categories[val])
                        .title("같이 하실 " + categories[val] + " 하실 분?")
                        .member(member)
                        .connectionType(ConnectionType.ONLINE)
                        .build();
                boardRepository.save(board);
            }

            for (int i = 1; i <= 15; i++) {
                int val = (int) (Math.random() * 3);
                Board board = Board.builder()
                        .content(regions[val] + "에서 " + categories[val] + "같이해요")
                        .category(categories[val])
                        .connectionType(ConnectionType.OFFLINE)
                        .offlineLocation(new OfflineLocation(33.450701, 126.570667))
                        .title(regions[val] + "에서 같이 " + categories[val] + "하실 분? ")
                        .member(member)
                        .build();
                boardRepository.save(board);
            }

            Board board = boardRepository.findById(25L).get();
            for (int i = 0; i < 3; i++) {
                Reply reply = Reply.createReply("답글" + (i + 1), board, member, null);
                replyRepository.save(reply);
                for (int j = 0; j < 2; j++) {
                    Reply child = Reply.createReply("대댓글" + j, board, user, reply);
                    replyRepository.save(child);
                }
            }

            for (int i = 0; i < 3; i++) {
                Board getBoard = boardRepository.findById((long) (i + 4)).get();
                postLikeRepository.save(PostLike.create(member, getBoard));
            }

            for (int i = 0; i < 3; i++) {
                Board getBoard = boardRepository.findById((long) (i + 1)).get();
                postLikeRepository.save(PostLike.create(user2, getBoard));
            }
        }

        private void createBlackList(String email, String reason, BlacklistStatus status) {
            String hashValue = HashUtil.sha256(email);

            if (blackListRepository.existsByHashValue(hashValue)) {
                return;
            }

            BlackList blackList = BlackList.create(BlackType.EMAIL, hashValue, reason);

            if (status == BlacklistStatus.PERMANENT) {
                blackList.setDuration(0, 3);
            }

            BlackListHistory history = BlackListHistory.create(
                    BlacklistAction.REGISTER, blackList, hashValue, BlackType.EMAIL, reason, status
            );

            blackList.addHistory(history);
            blackListRepository.save(blackList);
        }
    }
}