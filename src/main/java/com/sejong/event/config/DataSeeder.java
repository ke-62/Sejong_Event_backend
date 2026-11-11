package com.sejong.event.config;

import com.sejong.event.attendance.domain.AttendanceLog;
import com.sejong.event.attendance.domain.AttendanceStatus;
import com.sejong.event.attendance.repository.AttendanceLogRepository;
import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.repository.BoothRepository;
import com.sejong.event.event.domain.Event;
import com.sejong.event.event.repository.EventRepository;
import com.sejong.event.festival.domain.FestivalSetting;
import com.sejong.event.festival.repository.FestivalSettingRepository;
import com.sejong.event.user.domain.Role;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import com.sejong.event.waiting.domain.WaitingEntry;
import com.sejong.event.waiting.repository.WaitingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 로컬 개발 전용 예시 데이터. DB가 비어있을 때만(계정 0건) 1회 시딩한다.
 * 계정/행사/부스/대기열/입장 로그를 화면별로 바로 확인할 수 있는 조합으로 채운다.
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BoothRepository boothRepository;
    private final WaitingRepository waitingRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final FestivalSettingRepository festivalSettingRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String[] WAITING_NAMES = {
            "김민준", "이서연", "박도윤", "최지우", "정하은",
            "강시우", "조수아", "윤예준", "임서윤", "한지호",
            "오유나", "신도현", "장예은", "권민서", "홍준서",
            "배아린", "송지안", "문건우",
    };

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] 기존 데이터가 있어 시딩을 건너뜁니다.");
            return;
        }
        log.info("[DataSeeder] 로컬 예시 데이터 시딩 시작");

        seedFestivalSetting();
        seedUsers();
        Event totalAssembly = seedEvents();
        Booth busyBooth = seedBooths();
        seedWaiting(busyBooth);
        seedAttendance(totalAssembly);

        log.info("[DataSeeder] 완료 — admin/admin1234 로 로그인해 확인하세요.");
    }

    private void seedFestivalSetting() {
        FestivalSetting setting = FestivalSetting.defaultSetting();
        setting.setActive(true); // 데모 편의상 축제 기간 ON
        festivalSettingRepository.save(setting);
    }

    private void seedUsers() {
        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234"))
                .plainPassword("admin1234")
                .name("개발팀")
                .organization("개발팀")
                .role(Role.SUPERUSER)
                .build());

        userRepository.save(User.builder()
                .username("csclub")
                .password(passwordEncoder.encode("csclub1234"))
                .plainPassword("csclub1234")
                .name("김운영")
                .organization("컴퓨터공학과 학생회")
                .role(Role.ORGANIZER)
                .build());

        userRepository.save(User.builder()
                .username("studentunion")
                .password(passwordEncoder.encode("union1234"))
                .plainPassword("union1234")
                .name("박총학")
                .organization("총학생회")
                .role(Role.ORGANIZER)
                .build());
    }

    private Event seedEvents() {
        Event totalAssembly = eventRepository.save(Event.builder()
                .name("2026 전체학생총회")
                .organization("총학생회")
                .location("대양홀")
                .startAt(LocalDateTime.now().plusDays(3).withHour(14).withMinute(0))
                .description("정족수 충족 시 안건이 상정됩니다.")
                .targetCount(300)
                .build());

        eventRepository.save(Event.builder()
                .name("컴퓨터공학과 새내기 배움터")
                .organization("컴퓨터공학과 학생회")
                .location("공학관 대강당")
                .startAt(LocalDateTime.now().plusDays(10).withHour(10).withMinute(0))
                .description("신입생 대상 학과 오리엔테이션입니다.")
                .targetCount(150)
                .build());

        return totalAssembly;
    }

    private Booth seedBooths() {
        // 승인됨 — 혼잡(waiting > 15)
        Booth busyBooth = boothRepository.save(Booth.builder()
                .name("컴공 야시장")
                .category("주점")
                .location("A-01")
                .emoji("🍢")
                .organization("컴퓨터공학과 학생회")
                .description("떡볶이·오뎅·튀김을 판매합니다.")
                .createdIndex(0)
                .build());
        busyBooth.approve();
        busyBooth.setWaiting(18);
        boothRepository.save(busyBooth);

        // 승인됨 — 보통(6~15)
        Booth midBooth = boothRepository.save(Booth.builder()
                .name("총학 포차")
                .category("주점")
                .location("A-05")
                .emoji("🍺")
                .organization("총학생회")
                .description("생맥주와 안주를 판매합니다.")
                .createdIndex(1)
                .build());
        midBooth.approve();
        midBooth.setWaiting(9);
        boothRepository.save(midBooth);

        // 승인됨 — 여유(≤5)
        Booth calmBooth1 = boothRepository.save(Booth.builder()
                .name("타로카페")
                .category("체험")
                .location("B-02")
                .emoji("🔮")
                .organization("컴퓨터공학과 학생회")
                .description("타로 상담을 무료로 진행합니다.")
                .createdIndex(2)
                .build());
        calmBooth1.approve();
        calmBooth1.setWaiting(3);
        boothRepository.save(calmBooth1);

        Booth calmBooth2 = boothRepository.save(Booth.builder()
                .name("페이스페인팅")
                .category("체험")
                .location("B-07")
                .emoji("🎨")
                .organization("총학생회")
                .description("페이스페인팅 및 네일아트 체험 부스입니다.")
                .createdIndex(3)
                .build());
        calmBooth2.approve();
        boothRepository.save(calmBooth2);

        // 승인 대기 — 관리자 콘솔 "부스신청 관리" 탭 데모용
        boothRepository.save(Booth.builder()
                .name("댄스동아리 무대 부스")
                .category("체험")
                .location("C-03")
                .emoji("💃")
                .organization("댄스동아리")
                .description("소품 사진 촬영 부스입니다. 검토 부탁드립니다.")
                .createdIndex(4)
                .build());

        // 거부됨 — 재승인/삭제 데모용
        Booth rejected = boothRepository.save(Booth.builder()
                .name("임시 굿즈 판매대")
                .category("체험")
                .location("미정")
                .emoji("🎁")
                .organization("미승인 동아리")
                .description("위치 미정으로 반려되었습니다.")
                .createdIndex(5)
                .build());
        rejected.reject();
        boothRepository.save(rejected);

        return busyBooth;
    }

    private void seedWaiting(Booth booth) {
        int order = 1;
        for (String name : WAITING_NAMES) {
            waitingRepository.save(WaitingEntry.builder()
                    .booth(booth)
                    .order(order)
                    .name(name)
                    .phone("010-%04d-%04d".formatted(order * 111 % 10000, order * 777 % 10000))
                    .partySize((order % 4) + 1)
                    .build());
            order++;
        }
    }

    private void seedAttendance(Event event) {
        String[][] students = {
                {"23011679", "이규형", "컴퓨터공학과"},
                {"23015234", "김하윤", "전자정보통신공학과"},
                {"22098123", "박서준", "경영학과"},
                {"24012456", "최수민", "국어국문학과"},
                {"23033789", "정예린", "컴퓨터공학과"},
                {"22045612", "강도윤", "산업공학과"},
                {"24067890", "윤지호", "화학과"},
                {"23078234", "임채원", "역사학과"},
                {"22011345", "한서아", "물리학과"},
                {"24029876", "오민재", "영어영문학과"},
        };
        int ticket = 1;
        for (String[] s : students) {
            attendanceLogRepository.save(AttendanceLog.builder()
                    .event(event)
                    .studentId(s[0])
                    .name(s[1])
                    .department(s[2])
                    .ticketNumber(ticket++)
                    .gate("정문")
                    .status(AttendanceStatus.SUCCESS)
                    .build());
            event.incrementAttendance();
        }
        // 중복 입장 시도 1건 — 대시보드 중복 카운트 데모용
        attendanceLogRepository.save(AttendanceLog.builder()
                .event(event)
                .studentId("23011679-dup")
                .name("이규형")
                .department("컴퓨터공학과")
                .ticketNumber(ticket)
                .gate("정문")
                .status(AttendanceStatus.DUPLICATE)
                .build());

        eventRepository.save(event);
    }
}
