package com.study.studyproject.test;

import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SpringBootTest
class DummyDataInsertTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Faker fakerKo = new Faker(Locale.KOREA);
    private final Faker fakerEn = new Faker(Locale.ENGLISH);
    private final Random random = new Random();

    // !!!!!!!!!!!!!!내가 세팅 필요: 원하는 더미 데이터 개수로 변경
    private static final int MEMBER_COUNT = 60;
    private static final int BOARD_COUNT = 50;
    private static final int REPLY_COUNT = 50;
    private static final int POST_LIKE_COUNT = 50;
    private static final int BLACKLIST_COUNT = 50;

    // !!!!!!!!!!!!!!내가 세팅 필요: 한 번에 insert할 배치 크기
    private static final int BATCH_SIZE = 5_000;

    private static final List<String> LAST_NAMES = List.of(
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "전", "홍",
            "유", "고", "문", "양", "손", "배", "백", "허", "노", "심",
            "곽", "하", "성", "차", "주", "우", "구"
    );

    private static final List<String> FIRST_NAMES = List.of(
            "민준", "서연", "서준", "서현", "도윤", "수아", "예준", "하은", "시우", "지민",
            "지호", "지우", "하준", "윤서", "준우", "지아", "현우", "채원", "유준", "다은",
            "우진", "민서", "건우", "우주", "지훈", "서우", "진우", "윤아", "재윤", "채은",
            "선우", "하윤", "준서", "지윤", "정우", "소율", "승우", "지안", "유진", "시하",
            "민재", "소윤", "유찬", "서윤", "동현", "수빈", "시윤", "하린", "민성", "예린",
            "연우", "시은", "은우", "다인", "현준", "지유", "승현", "유나", "지원", "민수",
            "영희", "철수", "정민", "수민", "재혁", "은지", "태현", "현지", "수현", "성민",
            "혜원", "민우", "서진", "도현", "민지", "승민", "지현", "재현", "예진", "효준",
            "미영", "선영", "진호", "정호", "영호", "동우", "동희", "태웅", "성호", "지환"
    );

    @Test
    void 더미데이터_전체생성() {
        long startTime = System.currentTimeMillis();

        insertMembers();
        insertBoards();
        insertReplies();
        insertPostLikes();
        insertBlackLists();
        insertBlackListHistories();

        long endTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("더미 데이터 생성 완료");
        System.out.println("총 소요 시간: " + (endTime - startTime) / 1000.0 + "초");
        System.out.println("=========================================");
    }

    private void insertMembers() {
        String sql = """
            insert into member (
                email,
                password,
                username,
                nickname,
                role,
                social_id,
                social_type,
                created_date,
                last_modified_date
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                int index = i + 1;

                String emailUsername = fakerEn.internet()
                        .username()
                        .replaceAll("[^a-zA-Z0-9]", "")
                        .toLowerCase();

                if (emailUsername.isBlank()) {
                    emailUsername = "user";
                }

                String email = emailUsername + index + "@naver.com";
//                String password = "$2a$10$rYm7K9PxXvB" + fakerKo.random().hex(20);
                String password = "$2a$10$rYm7K9PxXvB";
                String username = getBulkName(index);
                String nickname = fakerKo.funnyName().name().replace(" ", "") + index;
                String role = index % 20 == 0 ? "ROLE_ADMIN" : "ROLE_USER";


                String socialId;
                String socialType;


                boolean isKakaoMember = index % 10 == 3;
                boolean isNaverMember = index % 10 == 5;
                if (isKakaoMember) {
                    password = null;
                    socialId = "kakao-" + index;
                    socialType = "KAKAO";
                } else if (isNaverMember) {
                    password = null;
                    socialId = "naver-" + index;
                    socialType = "NAVER";
                } else {
                    socialId = null;
                    socialType = null;
                }


                ps.setString(1, email);
                ps.setString(2, password);
                ps.setString(3, username);
                ps.setString(4, nickname);
                ps.setString(5, role);
                ps.setString(6, socialId);
                ps.setString(7, socialType);
                ps.setTimestamp(8, now());
                ps.setTimestamp(9, now());

            }

            @Override
            public int getBatchSize() {
                return MEMBER_COUNT;
            }
        });

        System.out.println("member 생성 완료: " + MEMBER_COUNT + "건");
    }

    private void insertBoards() {
        List<Long> memberIds = findIds("member", "member_id");

        String sql = """
                insert into board (
                    is_deleted,
                    x,
                    y,
                    created_date,
                    last_modified_date,
                    member_id,
                    view_count,
                    category,
                    connection_type,
                    content,
                    recruit,
                    title
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (int start = 0; start < BOARD_COUNT; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, BOARD_COUNT - start);

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Long memberId = pick(memberIds);

                    ps.setBoolean(1, false);
                    ps.setDouble(2, randomCoordinate());
                    ps.setDouble(3, randomCoordinate());
                    ps.setTimestamp(4, now());
                    ps.setTimestamp(5, now());
                    ps.setLong(6, memberId);
                    ps.setLong(7, random.nextLong(0, 1000));
                    ps.setString(8, randomCategory());
                    ps.setString(9, randomConnectionType());
                    ps.setString(10, fakerKo.lorem().sentence());
                    ps.setString(11, randomRecruit());
                    ps.setString(12, fakerKo.book().title());
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            System.out.println("board 생성 진행: " + Math.min(start + BATCH_SIZE, BOARD_COUNT) + "건");
        }

        System.out.println("board 생성 완료: " + BOARD_COUNT + "건");
    }

    private void insertReplies() {
        List<Long> memberIds = findIds("member", "member_id");
        List<Long> boardIds = findIds("board", "board_id");

        int parentReplyCount = (int) (REPLY_COUNT * 0.6);
        int childReplyCount = REPLY_COUNT - parentReplyCount;

        System.out.println("일반 댓글 생성 예정: " + parentReplyCount);
        System.out.println("대댓글 생성 예정: " + childReplyCount);

        insertParentReplies(memberIds, boardIds, parentReplyCount);

        List<ReplyParentInfo> parentReplies = findParentReplies();

        System.out.println("부모 댓글 조회 개수: " + parentReplies.size());

        if (parentReplies.isEmpty()) {
            throw new IllegalStateException("부모 댓글이 없어서 대댓글을 만들 수 없습니다.");
        }

        insertChildReplies(memberIds, parentReplies, childReplyCount);

        System.out.println("reply 생성 완료: " + REPLY_COUNT + "건");
    }

    private void insertParentReplies(List<Long> memberIds, List<Long> boardIds, int count) {
        String sql = """
            insert into reply (
                is_deleted,
                board_id,
                created_date,
                last_modified_date,
                member_id,
                parent_id,
                content,
                nickname
            ) values (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        for (int start = 0; start < count; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, count - start);

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Long memberId = pick(memberIds);
                    Long boardId = pick(boardIds);

                    ps.setBoolean(1, false);
                    ps.setLong(2, boardId);
                    ps.setTimestamp(3, now());
                    ps.setTimestamp(4, now());
                    ps.setLong(5, memberId);

                    // 일반 댓글
                    ps.setNull(6, java.sql.Types.BIGINT);

                    ps.setString(7, fakerKo.lorem().sentence());
                    ps.setString(8, fakerKo.name().username());
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            System.out.println("일반 댓글 생성 진행: " + Math.min(start + BATCH_SIZE, count) + "건");
        }
    }

    private List<ReplyParentInfo> findParentReplies() {
        return jdbcTemplate.query("""
            select
                reply_id,
                board_id
            from reply
            where parent_id is null
            """,
                (rs, rowNum) -> new ReplyParentInfo(
                        rs.getLong("reply_id"),
                        rs.getLong("board_id")
                )
        );
    }

    private void insertChildReplies(List<Long> memberIds, List<ReplyParentInfo> parentReplies, int count) {
        String sql = """
            insert into reply (
                is_deleted,
                board_id,
                created_date,
                last_modified_date,
                member_id,
                parent_id,
                content,
                nickname
            ) values (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        for (int start = 0; start < count; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, count - start);

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Long memberId = pick(memberIds);
                    ReplyParentInfo parentReply = pick(parentReplies);

                    ps.setBoolean(1, false);

                    // 부모 댓글과 같은 게시글
                    ps.setLong(2, parentReply.boardId());

                    ps.setTimestamp(3, now());
                    ps.setTimestamp(4, now());
                    ps.setLong(5, memberId);

                    // 대댓글 핵심
                    ps.setLong(6, parentReply.replyId());

                    ps.setString(7, fakerKo.lorem().sentence());
                    ps.setString(8, fakerKo.name().username());
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            System.out.println("대댓글 생성 진행: " + Math.min(start + BATCH_SIZE, count) + "건");
        }
    }

    private record ReplyParentInfo(
            Long replyId,
            Long boardId
    ) {
    }
    private void insertPostLikes() {
        List<Long> memberIds = findIds("member", "member_id");
        List<Long> boardIds = findIds("board", "board_id");

        String sql = """
                insert into post_like (
                    board_id,
                    created_date,
                    last_modified_date,
                    member_id
                ) values (?, ?, ?, ?)
                """;

        for (int start = 0; start < POST_LIKE_COUNT; start += BATCH_SIZE) {
            int currentBatchSize = Math.min(BATCH_SIZE, POST_LIKE_COUNT - start);

            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                    Long memberId = pick(memberIds);
                    Long boardId = pick(boardIds);

                    ps.setLong(1, boardId);
                    ps.setTimestamp(2, now());
                    ps.setTimestamp(3, now());
                    ps.setLong(4, memberId);
                }

                @Override
                public int getBatchSize() {
                    return currentBatchSize;
                }
            });

            System.out.println("post_like 생성 진행: " + Math.min(start + BATCH_SIZE, POST_LIKE_COUNT) + "건");
        }

        System.out.println("post_like 생성 완료: " + POST_LIKE_COUNT + "건");
    }

    private void insertBlackLists() {
        String sql = """
                insert into black_list (
                    created_at,
                    expire_at,
                    hash_value,
                    reason,
                    status,
                    type
                ) values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                int index = i + 1;

                String type = randomBlackListType();
                String hashValue = type.toLowerCase() + "-hash-" + index;

                ps.setTimestamp(1, now());
                ps.setTimestamp(2, randomExpireAt());
                ps.setString(3, hashValue);
                ps.setString(4, randomReason());
                ps.setString(5, randomBlackListStatus());
                ps.setString(6, type);
            }

            @Override
            public int getBatchSize() {
                return BLACKLIST_COUNT;
            }
        });

        System.out.println("black_list 생성 완료: " + BLACKLIST_COUNT + "건");
    }

    private void insertBlackListHistories() {
        List<Long> blackListIds = findIds("black_list", "blacklist_id");

        String sql = """
                insert into black_list_history (
                    blacklist_id,
                    create_at,
                    action,
                    create_by,
                    hash_value,
                    reason,
                    status,
                    type
                )
                select
                    blacklist_id,
                    created_at,
                    ?,
                    ?,
                    hash_value,
                    reason,
                    status,
                    type
                from black_list
                where blacklist_id = ?
                """;

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                Long blackListId = blackListIds.get(i);

                ps.setString(1, "REGISTER");
                ps.setString(2, "SYSTEM");
                ps.setLong(3, blackListId);
            }

            @Override
            public int getBatchSize() {
                return blackListIds.size();
            }
        });

        System.out.println("black_list_history 생성 완료: " + blackListIds.size() + "건");
    }

    private List<Long> findIds(String tableName, String idColumnName) {
        return jdbcTemplate.queryForList(
                "select " + idColumnName + " from " + tableName,
                Long.class
        );
    }

    private Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    private Timestamp randomExpireAt() {
        return Timestamp.valueOf(LocalDateTime.now().plusDays(random.nextInt(1, 90)));
    }

    private double randomCoordinate() {
        return random.nextDouble(0, 100);
    }

    private String randomCategory() {
        return pick(List.of("기타", "CS", "전체", "코테", "프로젝트"));
    }

    private String randomConnectionType() {
        return pick(List.of("OFFLINE", "ONLINE"));
    }

    private String randomRecruit() {
        return pick(List.of("모집중", "모집완료"));
    }

    private String randomBlackListType() {
        return pick(List.of("IP", "EMAIL"));
    }

    private String randomBlackListStatus() {
        return pick(List.of("PERMANENT", "ACTIVE", "EXPIRED"));
    }

    private String randomReason() {
        return pick(List.of(
                "스팸 행위",
                "비정상 접근",
                "욕설 신고",
                "악성 사용자",
                "관리자 차단"
        ));
    }

    private static String getBulkName(int index) {
        int lastNameIndex = index % LAST_NAMES.size();
        int firstNameIndex = (index + index / LAST_NAMES.size()) % FIRST_NAMES.size();

        return LAST_NAMES.get(lastNameIndex) + FIRST_NAMES.get(firstNameIndex);
    }

    private <T> T pick(List<T> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("선택할 값 목록이 비어 있습니다.");
        }

        return values.get(random.nextInt(values.size()));
    }
}