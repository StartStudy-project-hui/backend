package com.study.studyproject.test;

import com.study.studyproject.global.hash.HashUtil;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    private static final String FILE_PATH = "다운로드/dummy_data.sql";

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    // 이번 실행에서 만든 SQL 텍스트를 파일에 적기 위한 writer
    private BufferedWriter sqlWriter;

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
    void 더미데이터_전체생성() throws IOException {
        long startTime = System.currentTimeMillis();

        Path path = Path.of(FILE_PATH);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            this.sqlWriter = writer;

            insertMembers();
            insertBoards();
            insertReplies();
            insertPostLikes();
            insertBlackLists();
            insertBlackListHistories();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("=========================================");
        System.out.println("더미 데이터 생성 완료");
        System.out.println("SQL 파일 저장 위치: " + path.toAbsolutePath());
        System.out.println("총 소요 시간: " + (endTime - startTime) / 1000.0 + "초");
        System.out.println("=========================================");
    }

    // ===================== member =====================

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

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < MEMBER_COUNT; i++) {
            int index = i + 1;

            String emailUsername = fakerEn.internet()
                    .username()
                    .replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase();

            if (emailUsername.isBlank()) {
                emailUsername = "user";
            }

            String email = emailUsername + index + "@naver.com";

            /*
             * password 컬럼이 NOT NULL 이므로 소셜 회원도 null이면 안 됨.
             * 실제 로그인 검증까지 생각하면 BCrypt 형식 값을 넣는 것이 안전함.
             */
            String password = "$2a$10$rYm7K9PxXvBgQmQnqYpJbOe9Pi4jHnQnZg5tvC6kOqGrfFrZxZp4y";

            String username = getBulkName(index);
            String nickname = fakerKo.funnyName().name().replace(" ", "") + index;
            String role = index % 20 == 0 ? "ROLE_ADMIN" : "ROLE_USER";

            String socialId = null;
            String socialType = null;

            boolean isKakaoMember = index % 10 == 3;
            boolean isNaverMember = index % 10 == 5;

            if (isKakaoMember) {
                socialId = "kakao-" + index;
                socialType = "KAKAO";
            } else if (isNaverMember) {
                socialId = "naver-" + index;
                socialType = "NAVER";
            }

            Timestamp createdDate = now();
            Timestamp lastModifiedDate = createdDate;

            batchArgs.add(new Object[]{
                    email, password, username, nickname, role,
                    socialId, socialType, createdDate, lastModifiedDate
            });

            writeSql(
                    "insert into member (email, password, username, nickname, role, social_id, social_type, created_date, last_modified_date) values (%s, %s, %s, %s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlStr(email), sqlStr(password), sqlStr(username), sqlStr(nickname), sqlStr(role),
                                    sqlStr(socialId), sqlStr(socialType), sqlTimestamp(createdDate), sqlTimestamp(lastModifiedDate)
                            )
            );
        }

        executeBatched(sql, batchArgs);

        System.out.println("member 생성 완료: " + MEMBER_COUNT + "건");
    }

    // ===================== board =====================

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
                    recruit_status,
                    title
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < BOARD_COUNT; i++) {
            Long memberId = pick(memberIds);
            double x = randomCoordinate();
            double y = randomCoordinate();
            Timestamp createdDate = now();
            Timestamp lastModifiedDate = createdDate;
            long viewCount = random.nextLong(0, 1000);
            String category = randomCategory();
            String connectionType = randomConnectionType();
            String content = fakerKo.lorem().sentence();
            String recruitStatus = randomRecruit();
            String title = fakerKo.book().title();

            batchArgs.add(new Object[]{
                    false, x, y, createdDate, lastModifiedDate, memberId,
                    viewCount, category, connectionType, content, recruitStatus, title
            });

            writeSql(
                    "insert into board (is_deleted, x, y, created_date, last_modified_date, member_id, view_count, category, connection_type, content, recruit_status, title) values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlBool(false), sqlNum(x), sqlNum(y), sqlTimestamp(createdDate), sqlTimestamp(lastModifiedDate),
                                    sqlNum(memberId), sqlNum(viewCount), sqlStr(category), sqlStr(connectionType),
                                    sqlStr(content), sqlStr(recruitStatus), sqlStr(title)
                            )
            );
        }

        executeBatched(sql, batchArgs);

        System.out.println("board 생성 완료: " + BOARD_COUNT + "건");
    }

    // ===================== reply =====================

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

    private static final String REPLY_INSERT_SQL = """
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

    private void insertParentReplies(List<Long> memberIds, List<Long> boardIds, int count) {
        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Long memberId = pick(memberIds);
            Long boardId = pick(boardIds);
            Timestamp createdDate = now();
            Timestamp lastModifiedDate = createdDate;
            String content = fakerKo.lorem().sentence();
            String nickname = fakerKo.name().username();

            batchArgs.add(new Object[]{
                    false, boardId, createdDate, lastModifiedDate, memberId, null, content, nickname
            });

            writeSql(
                    "insert into reply (is_deleted, board_id, created_date, last_modified_date, member_id, parent_id, content, nickname) values (%s, %s, %s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlBool(false), sqlNum(boardId), sqlTimestamp(createdDate), sqlTimestamp(lastModifiedDate),
                                    sqlNum(memberId), "NULL", sqlStr(content), sqlStr(nickname)
                            )
            );
        }

        executeBatched(REPLY_INSERT_SQL, batchArgs);

        System.out.println("일반 댓글 생성 완료: " + count + "건");
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
        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Long memberId = pick(memberIds);
            ReplyParentInfo parentReply = pick(parentReplies);
            Timestamp createdDate = now();
            Timestamp lastModifiedDate = createdDate;
            String content = fakerKo.lorem().sentence();
            String nickname = fakerKo.name().username();

            batchArgs.add(new Object[]{
                    false, parentReply.boardId(), createdDate, lastModifiedDate, memberId,
                    parentReply.replyId(), content, nickname
            });

            writeSql(
                    "insert into reply (is_deleted, board_id, created_date, last_modified_date, member_id, parent_id, content, nickname) values (%s, %s, %s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlBool(false), sqlNum(parentReply.boardId()), sqlTimestamp(createdDate), sqlTimestamp(lastModifiedDate),
                                    sqlNum(memberId), sqlNum(parentReply.replyId()), sqlStr(content), sqlStr(nickname)
                            )
            );
        }

        executeBatched(REPLY_INSERT_SQL, batchArgs);

        System.out.println("대댓글 생성 완료: " + count + "건");
    }

    private record ReplyParentInfo(
            Long replyId,
            Long boardId
    ) {
    }

    // ===================== post_like =====================

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

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 0; i < POST_LIKE_COUNT; i++) {
            Long memberId = pick(memberIds);
            Long boardId = pick(boardIds);
            Timestamp createdDate = now();
            Timestamp lastModifiedDate = createdDate;

            batchArgs.add(new Object[]{boardId, createdDate, lastModifiedDate, memberId});

            writeSql(
                    "insert into post_like (board_id, created_date, last_modified_date, member_id) values (%s, %s, %s, %s);"
                            .formatted(sqlNum(boardId), sqlTimestamp(createdDate), sqlTimestamp(lastModifiedDate), sqlNum(memberId))
            );
        }

        executeBatched(sql, batchArgs);

        System.out.println("post_like 생성 완료: " + POST_LIKE_COUNT + "건");
    }

    // ===================== black_list =====================

    private record BlackListRecord(
            String hashValue,
            String reason,
            String status,
            String type,
            Timestamp createdAt,
            Timestamp expireAt
    ) {
    }

    // black_list_history에서 재사용하기 위해 이번 실행에서 생성한 값들을 보관
    private List<BlackListRecord> generatedBlackLists = new ArrayList<>();

    private void insertBlackLists() {
        List<String> memberEmails = findMemberEmails();

        if (memberEmails.isEmpty()) {
            throw new IllegalStateException("블랙리스트에 넣을 회원 이메일 데이터가 없습니다.");
        }

        // 1. memberEmails에서 중복 없이 고유한 해시값 목록을 미리 생성
        // (요청한 BLACKLIST_COUNT 개수와 실제 이메일 총개수 중 작은 값만큼 추출)
        List<String> uniqueHashValues = memberEmails.stream()
                .distinct()
                .limit(BLACKLIST_COUNT)
                .map(HashUtil::sha256)
                .toList();

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

        List<Object[]> batchArgs = new ArrayList<>();
        generatedBlackLists = new ArrayList<>();

        for (String hashValue : uniqueHashValues) {
            Timestamp createdAt = now();
            Timestamp expireAt = randomExpireAt();
            String reason = randomReason();
            String status = randomBlackListStatus();
            String type = randomBlackListType();

            batchArgs.add(new Object[]{createdAt, expireAt, hashValue, reason, status, type});
            generatedBlackLists.add(new BlackListRecord(hashValue, reason, status, type, createdAt, expireAt));

            writeSql(
                    "insert into black_list (created_at, expire_at, hash_value, reason, status, type) values (%s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlTimestamp(createdAt), sqlTimestamp(expireAt), sqlStr(hashValue),
                                    sqlStr(reason), sqlStr(status), sqlStr(type)
                            )
            );
        }

        executeBatched(sql, batchArgs);

        System.out.println("black_list 생성 완료: " + uniqueHashValues.size() + "건");
    }

    // 이메일 목록을 조회하는 도우미 메서드
    private List<String> findMemberEmails() {
        return jdbcTemplate.queryForList(
                "select email from member",
                String.class
        );
    }

    // ===================== black_list_history =====================

    private void insertBlackListHistories() {
        List<Long> blackListIds = findIds("black_list", "blacklist_id");

        if (blackListIds.size() != generatedBlackLists.size()) {
            System.out.println(
                    "경고: black_list ID 개수(" + blackListIds.size()
                            + ")와 생성된 레코드 개수(" + generatedBlackLists.size()
                            + ")가 다릅니다. SQL 파일 내용이 실제 DB와 다를 수 있습니다."
            );
        }

        // DB 실행은 기존과 동일하게 insert...select 방식 유지 (FK 무결성 보장)
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

        List<Object[]> batchArgs = new ArrayList<>();

        int count = Math.min(blackListIds.size(), generatedBlackLists.size());

        for (int i = 0; i < count; i++) {
            Long blackListId = blackListIds.get(i);
            BlackListRecord record = generatedBlackLists.get(i);

            batchArgs.add(new Object[]{"REGISTER", "SYSTEM", blackListId});

            // insert...select 대신, 실제 값을 그대로 채운 리터럴 insert문으로 파일에 기록
            writeSql(
                    "insert into black_list_history (blacklist_id, create_at, action, create_by, hash_value, reason, status, type) values (%s, %s, %s, %s, %s, %s, %s, %s);"
                            .formatted(
                                    sqlNum(blackListId), sqlTimestamp(record.createdAt()), sqlStr("REGISTER"), sqlStr("SYSTEM"),
                                    sqlStr(record.hashValue()), sqlStr(record.reason()), sqlStr(record.status()), sqlStr(record.type())
                            )
            );
        }

        executeBatched(sql, batchArgs);

        System.out.println("black_list_history 생성 완료: " + count + "건");
    }

    // ===================== 공통 유틸 =====================

    private List<Long> findIds(String tableName, String idColumnName) {
        return jdbcTemplate.queryForList(
                "select " + idColumnName + " from " + tableName,
                Long.class
        );
    }

    private void executeBatched(String sql, List<Object[]> batchArgs) {
        for (int start = 0; start < batchArgs.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, batchArgs.size());
            jdbcTemplate.batchUpdate(sql, batchArgs.subList(start, end));
        }
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
        return pick(List.of("ETC", "CS", "ALL", "CODING_TEST", "PROJECT"));
    }

    private String randomConnectionType() {
        return pick(List.of("OFFLINE", "ONLINE"));
    }

    private String randomRecruit() {
        return pick(List.of("RECRUITING", "COMPLETED"));
    }

    private String randomBlackListType() {
        return pick(List.of("EMAIL"));
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

    // ===================== SQL 텍스트 생성 유틸 =====================

    private void writeSql(String sql) {
        try {
            sqlWriter.write(sql);
            sqlWriter.newLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String sqlStr(String value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + value.replace("'", "''") + "'";
    }

    private String sqlNum(Number value) {
        if (value == null) {
            return "NULL";
        }
        return value.toString();
    }

    private String sqlBool(boolean value) {
        // DB가 MySQL(tinyint) 계열이면 0/1, PostgreSQL이면 true/false로 바꿔서 사용
        return value ? "1" : "0";
    }

    private String sqlTimestamp(Timestamp value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + value.toLocalDateTime().format(TS_FORMATTER) + "'";
    }
}