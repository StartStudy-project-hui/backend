package com.study.studyproject;

import com.study.studyproject.blacklist.domain.*;
import com.study.studyproject.blacklist.repository.blacklist.BlackListRepository;
import com.study.studyproject.blacklist.repository.blacklisthistory.BlackListHistoryRepository;
import com.study.studyproject.board.domain.Board;
import com.study.studyproject.board.domain.Category;
import com.study.studyproject.board.domain.ConnectionType;
import com.study.studyproject.board.domain.OfflineLocation;
import com.study.studyproject.board.repository.BoardRepository;
import com.study.studyproject.global.hash.HashUtil;
import com.study.studyproject.auth.domain.Role;
import com.study.studyproject.member.domain.Member;
import com.study.studyproject.member.repository.MemberRepository;
import com.study.studyproject.postlike.domain.PostLike;
import com.study.studyproject.postlike.repository.PostLikeRepository;
import com.study.studyproject.reply.domain.Reply;
import com.study.studyproject.reply.repository.ReplyRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//(33.450701, 126.570667)
@Component
@Profile({"local","prod","dev"})
public class InitData {

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ReplyRepository replyRepository;


    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    BlackListRepository blackListRepository;

    @Autowired
    BlackListHistoryRepository blackListHistoryRepository;




    @PostConstruct
    void init() {

        if (memberRepository.count() > 0) {
            return;
        }

        String encode1 = passwordEncoder.encode("Y@3r9o$7k");
        String encode2 = passwordEncoder.encode("Y@3r9o$7aaak");
        String encode3 = passwordEncoder.encode("Y@3r9o$7kff");
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
                .password(encode2)
                .build();
        memberRepository.save(admin);

        Member member = memberRepository.findById(1L).get();
        Member user2 = Member.builder()
                .role(Role.ROLE_USER)
                .username("김지우")
                .email("huj@naver.com")
                .nickname("huj")
                .password(encode3)
                .build();

        memberRepository.save(user2);

        Member[] arr1 = {
              user, admin, user2
        };

//        createBlackList(user.getEmail(), "피싱", BlacklistStatus.ACTIVE);
//        createBlackList(admin.getEmail(), "스팸", BlacklistStatus.PERMANENT);
//        createBlackList(user2.getEmail(), "욕설", BlacklistStatus.ACTIVE);

        Category[] arr = {
                Category.CS, Category.ETC, Category.CODING_TEST
        };


        for (int i = 1; i <= 15; i++) {
            int val = (int) (Math.random() * 3);
            Board build = Board.builder()
                    .content("같이 " + arr[val] + " 같이해요")
                    .category(arr[val])
                    .title("같이 하실 " + arr[val] + " 하실 분?")
                    .member(member)
                    .connectionType(ConnectionType.ONLINE)
                    .build();

            boardRepository.save(build);
        }

        String[] regin = {"서울", "인천", "경기"};
        for (int i = 1; i <= 15; i++) {
            int val = (int) (Math.random() * 3);
            Board build = Board.builder()
                    .content(regin[val] + "에서 " + arr[val] + "같이해요")
                    .category(arr[val])
                    .connectionType(ConnectionType.OFFLINE)
                    .offlineLocation(new OfflineLocation(33.450701,126.570667))
                    .title(regin[val] + "에서 같이 " + arr[val] + "하실 분? ")
                    .member(member)
                    .build();
            boardRepository.save(build);
        }

        Board board = boardRepository.findById(25L).get();
        Reply reply = null;
        for (int i = 0; i < 3; i++) {
            reply = Reply.builder()
                    .board(board)
                    .member(member)
                    .content("답글" + (i + 1))
                    .build();

            Reply replyer;
            for (int j = 0; j < 2; j++) {
                replyer = getReply(user, reply, board, "대댓글" + j);
                replyer.updateParent(reply);
                replyRepository.save(reply);
                replyRepository.save(replyer);
            }


        }

        for (int i = 0; i < 3; i++) {
            Board getBoard = boardRepository.findById((long) (i + 4)).get();
            PostLike postLike = PostLike.create(member, getBoard);
            postLikeRepository.save(postLike);
        }

        for (int i = 0; i < 3; i++) {
            Board getBoard = boardRepository.findById((long) (i + 1)).get();
            PostLike postLike = PostLike.create(user2, getBoard);
            postLikeRepository.save(postLike);
        }


        


    }

    private void createBlackList(String email,
                                String reason,
                                BlacklistStatus status) {

        String hashValue = HashUtil.sha256(email);

        // 이미 존재하면 생성 안 함 (unique 안전)
        if (blackListRepository.existsByHashValue(hashValue)) {
            return;
        }

        BlackList blackList = BlackList.create(
                hashValue,
                reason
        );

        if (status == BlacklistStatus.PERMANENT) {
            blackList.setDuration(0, 3); // 영구 정지 처리
        }

        BlackListHistory history =
                BlackListHistory.save(
                        BlacklistAction.REGISTER,
                        blackList,
                        hashValue,
                        BlackType.EMAIL,
                        reason,
                        status
                );

        blackList.addHistory(history);

        blackListRepository.save(blackList);
    }

    private static Reply getReply(Member memberOne, Reply reply, Board board, String content) {
        return Reply.builder()
                .member(memberOne)
                .content(content)
                .parent(reply)
                .board(board)
                .build();
    }

}
