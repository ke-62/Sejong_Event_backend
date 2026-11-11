package com.sejong.event.booth.service;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.domain.BoothStatus;
import com.sejong.event.booth.dto.BoothResponse;
import com.sejong.event.booth.repository.BoothRepository;
import java.util.List;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import com.sejong.event.user.domain.User;
import com.sejong.event.user.repository.UserRepository;
import com.sejong.event.waiting.repository.WaitingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BoothServiceTest {

    @Mock
    private BoothRepository boothRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private WaitingRepository waitingRepository;
    @InjectMocks
    private BoothService boothService;

    @Test
    @DisplayName("부스 신청은 소속 기관을 붙여 pending 으로 저장된다")
    void createBoothIsPending() {
        User user = TestFixtures.organizer("중앙동아리 A");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(boothRepository.count()).willReturn(0L);
        given(boothRepository.save(any(Booth.class))).willAnswer(invocation -> invocation.getArgument(0));

        BoothResponse response = boothService.createBooth(1L, TestRequests.createBooth("감성주점", "주점"));

        assertThat(response.getOrganization()).isEqualTo("중앙동아리 A");
        assertThat(response.getStatus()).isEqualTo("pending");
    }

    @Test
    @DisplayName("공개 목록은 승인된 부스를 대기 인원 내림차순으로 반환한다")
    void getApprovedBooths() {
        given(boothRepository.findByStatusOrderByWaitingDesc(BoothStatus.APPROVED))
                .willReturn(List.of(TestFixtures.booth("동아리 A")));

        assertThat(boothService.getBooths(null, null)).hasSize(1);
        then(boothRepository).should().findByStatusOrderByWaitingDesc(BoothStatus.APPROVED);
    }

    @Test
    @DisplayName("status=all 이면 승인 대기 포함 전체 부스를 반환한다")
    void getAllBooths() {
        given(boothRepository.findAllByOrderByStatusAscIdDesc())
                .willReturn(List.of(TestFixtures.booth("A"), TestFixtures.booth("B")));

        assertThat(boothService.getBooths(null, "all")).hasSize(2);
    }

    @Test
    @DisplayName("category 파라미터가 있으면 카테고리로 거른다")
    void getBoothsByCategory() {
        given(boothRepository.findByCategoryAndStatusOrderByWaitingDesc("주점", BoothStatus.APPROVED))
                .willReturn(List.of(TestFixtures.booth("동아리 A")));

        assertThat(boothService.getBooths("주점", null)).hasSize(1);
    }

    @Test
    @DisplayName("부스 상세 조회는 예상 대기시간(avgMinutes)을 포함한다")
    void getBoothDetail() {
        given(boothRepository.findById(3L)).willReturn(java.util.Optional.of(TestFixtures.booth("동아리 A")));

        BoothResponse response = boothService.getBooth(3L);

        assertThat(response.getAvgMinutes()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("슈퍼유저는 부스 신청을 거부할 수 있다")
    void rejectBooth() {
        Booth booth = TestFixtures.booth("동아리 A");
        given(boothRepository.findById(3L)).willReturn(java.util.Optional.of(booth));

        boothService.rejectBooth(3L);

        assertThat(booth.getStatus()).isEqualTo(BoothStatus.REJECTED);
    }

    @Test
    @DisplayName("부스 신청 시 지도 좌표를 함께 저장한다")
    void createBoothWithCoordinates() {
        User user = TestFixtures.organizer("중앙동아리 A");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(boothRepository.count()).willReturn(0L);
        given(boothRepository.save(any(Booth.class))).willAnswer(invocation -> invocation.getArgument(0));

        BoothResponse response = boothService.createBooth(1L,
                TestRequests.createBoothAt("감성주점", 37.5503, 127.0742));

        assertThat(response.getLatitude()).isEqualTo(37.5503);
        assertThat(response.getLongitude()).isEqualTo(127.0742);
    }

    @Test
    @DisplayName("부스 수정 시 좌표를 보내면 갱신하고, 보내지 않으면 기존 좌표를 유지한다")
    void updateBoothCoordinates() {
        User user = TestFixtures.organizer("동아리 A");
        Booth booth = TestFixtures.booth("동아리 A");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(boothRepository.findById(3L)).willReturn(Optional.of(booth));

        boothService.updateBooth(1L, 3L, TestRequests.updateBoothCoordinates(37.5501, 127.0739));
        boothService.updateBooth(1L, 3L, TestRequests.updateBooth("새 이름"));

        assertThat(booth.getLatitude()).isEqualTo(37.5501);
        assertThat(booth.getLongitude()).isEqualTo(127.0739);
        assertThat(booth.getName()).isEqualTo("새 이름");
    }

    @Test
    @DisplayName("다른 기관 부스 수정은 BOOTH_ACCESS_DENIED")
    void updateOtherOrganizationBooth() {
        User user = TestFixtures.organizer("동아리 A");
        Booth other = TestFixtures.booth("동아리 B");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(boothRepository.findById(3L)).willReturn(Optional.of(other));

        assertThatThrownBy(() -> boothService.updateBooth(1L, 3L, TestRequests.updateBooth("새 이름")))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BOOTH_ACCESS_DENIED));
    }

    @Test
    @DisplayName("슈퍼유저는 부스를 승인할 수 있다")
    void approveBooth() {
        Booth booth = TestFixtures.booth("동아리 A");
        given(boothRepository.findById(3L)).willReturn(Optional.of(booth));

        Map<String, Object> response = boothService.approveBooth(3L);

        assertThat(booth.getStatus()).isEqualTo(BoothStatus.APPROVED);
        assertThat(response).containsEntry("status", "approved");
    }

    @Test
    @DisplayName("부스 삭제 시 웨이팅을 먼저 제거한다")
    void deleteBoothClearsWaiting() {
        Booth booth = TestFixtures.booth("동아리 A");
        given(userRepository.findById(9L)).willReturn(Optional.of(TestFixtures.superuser()));
        given(boothRepository.findById(3L)).willReturn(Optional.of(booth));

        boothService.deleteBooth(9L, 3L);

        then(waitingRepository).should().deleteByBoothId(3L);
        then(boothRepository).should().delete(booth);
    }

    @Test
    @DisplayName("영업 상태 변경 시 부스 토픽으로 브로드캐스트한다")
    void updateBoothStatusBroadcasts() {
        User user = TestFixtures.organizer("동아리 A");
        Booth booth = TestFixtures.booth("동아리 A");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(boothRepository.findById(3L)).willReturn(Optional.of(booth));

        boothService.updateBoothStatus(1L, 3L, TestRequests.boothStatus(false));

        assertThat(booth.getIsOpen()).isFalse();
        then(messagingTemplate).should().convertAndSend(eq("/topic/booths/3"), any(Object.class));
    }
}
