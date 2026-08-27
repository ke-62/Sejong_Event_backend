package com.sejong.event.waiting.service;

import com.sejong.event.booth.domain.Booth;
import com.sejong.event.booth.service.BoothService;
import com.sejong.event.common.exception.CustomException;
import com.sejong.event.common.exception.ErrorCode;
import com.sejong.event.support.TestFixtures;
import com.sejong.event.support.TestRequests;
import com.sejong.event.waiting.domain.WaitingEntry;
import com.sejong.event.waiting.domain.WaitingStatus;
import com.sejong.event.waiting.dto.NextWaitingResponse;
import com.sejong.event.waiting.dto.WaitingRegisterResponse;
import com.sejong.event.waiting.dto.WaitingStatusResponse;
import com.sejong.event.waiting.repository.WaitingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {

    @Mock
    private WaitingRepository waitingRepository;
    @Mock
    private BoothService boothService;
    @InjectMocks
    private WaitingService waitingService;

    @Test
    @DisplayName("웨이팅 등록 시 순번·앞선 팀 수·예상 시간을 계산하고 부스 대기 인원을 브로드캐스트한다")
    void register() {
        Booth booth = TestFixtures.booth("동아리 A");
        given(boothService.findBooth(3L)).willReturn(booth);
        given(waitingRepository.countByBoothId(3L)).willReturn(2);
        given(waitingRepository.save(any(WaitingEntry.class))).willAnswer(invocation -> invocation.getArgument(0));
        given(waitingRepository.countByBoothIdAndStatus(3L, WaitingStatus.WAITING)).willReturn(3);

        WaitingRegisterResponse response =
                waitingService.register(3L, TestRequests.waiting("홍길동", "01012345678", 2));

        assertThat(response.getOrder()).isEqualTo(3);            // countByBoothId(2) + 1
        assertThat(response.getWaitingAhead()).isEqualTo(2);     // countByStatus(3) - 1
        assertThat(response.getEstimatedMinutes()).isEqualTo(3.0); // 2 * 1.5
        assertThat(response.getStatus()).isEqualTo("WAITING");
        assertThat(booth.getWaiting()).isEqualTo(3);
        then(boothService).should().broadcastBoothWaiting(eq(3L), any(Booth.class));
    }

    @Test
    @DisplayName("다음 팀 호출 시 첫 대기 팀을 CALLED 로 바꾸고 남은 인원을 반환한다")
    void callNext() {
        Booth booth = TestFixtures.booth("동아리 A");
        WaitingEntry first = TestFixtures.waitingEntry(booth, 10L, 1, WaitingStatus.WAITING);
        given(waitingRepository.findFirstByBoothIdAndStatusOrderByOrderAsc(3L, WaitingStatus.WAITING))
                .willReturn(Optional.of(first));
        given(boothService.findBooth(3L)).willReturn(booth);
        given(waitingRepository.countByBoothIdAndStatus(3L, WaitingStatus.WAITING)).willReturn(4);

        NextWaitingResponse response = waitingService.callNext(3L);

        assertThat(first.getStatus()).isEqualTo(WaitingStatus.CALLED);
        assertThat(response.getCalledId()).isEqualTo(10L);
        assertThat(response.getRemainingCount()).isEqualTo(4);
        then(boothService).should().broadcastBoothWaiting(eq(3L), any(Booth.class));
    }

    @Test
    @DisplayName("호출할 대기 팀이 없으면 WAITING_NOT_FOUND")
    void callNextWithoutWaiting() {
        given(waitingRepository.findFirstByBoothIdAndStatusOrderByOrderAsc(3L, WaitingStatus.WAITING))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.callNext(3L))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.WAITING_NOT_FOUND));
    }

    @Test
    @DisplayName("다른 부스의 웨이팅을 취소하려 하면 BOOTH_ACCESS_DENIED")
    void cancelWrongBooth() {
        Booth booth = TestFixtures.booth("동아리 A"); // id = 3
        WaitingEntry entry = TestFixtures.waitingEntry(booth, 10L, 1, WaitingStatus.WAITING);
        given(waitingRepository.findById(10L)).willReturn(Optional.of(entry));

        assertThatThrownBy(() -> waitingService.cancel(99L, 10L))
                .isInstanceOfSatisfying(CustomException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.BOOTH_ACCESS_DENIED));
    }

    @Test
    @DisplayName("웨이팅 취소 시 상태를 CANCELLED 로 바꾸고 대기 인원을 브로드캐스트한다")
    void cancel() {
        Booth booth = TestFixtures.booth("동아리 A");
        WaitingEntry entry = TestFixtures.waitingEntry(booth, 10L, 1, WaitingStatus.WAITING);
        given(waitingRepository.findById(10L)).willReturn(Optional.of(entry));
        given(boothService.findBooth(3L)).willReturn(booth);
        given(waitingRepository.countByBoothIdAndStatus(3L, WaitingStatus.WAITING)).willReturn(0);

        waitingService.cancel(3L, 10L);

        assertThat(entry.getStatus()).isEqualTo(WaitingStatus.CANCELLED);
        assertThat(booth.getWaiting()).isZero();
        then(boothService).should().broadcastBoothWaiting(eq(3L), any(Booth.class));
    }

    @Test
    @DisplayName("내 웨이팅 상태 조회는 앞선 팀 수와 예상 시간을 돌려준다")
    void getStatus() {
        Booth booth = TestFixtures.booth("동아리 A");
        WaitingEntry entry = TestFixtures.waitingEntry(booth, 10L, 5, WaitingStatus.WAITING);
        given(waitingRepository.findById(10L)).willReturn(Optional.of(entry));
        given(waitingRepository.findFirstByBoothIdAndStatusOrderByOrderDesc(3L, WaitingStatus.CALLED))
                .willReturn(Optional.empty());
        given(waitingRepository.countByBoothIdAndStatusAndOrderLessThan(3L, WaitingStatus.WAITING, 5))
                .willReturn(3);

        WaitingStatusResponse response = waitingService.getStatus(3L, 10L);

        assertThat(response.getMyOrder()).isEqualTo(5);
        assertThat(response.getCalledOrder()).isZero();
        assertThat(response.getAhead()).isEqualTo(3);
        assertThat(response.getEstimatedMinutes()).isEqualTo(4.5); // 3 * 1.5
    }

    @Test
    @DisplayName("대기열 공개 조회는 순번·이름·인원만 반환한다")
    void getWaitingQueue() {
        Booth booth = TestFixtures.booth("동아리 A");
        given(waitingRepository.findByBoothIdAndStatusOrderByOrderAsc(3L, WaitingStatus.WAITING))
                .willReturn(List.of(
                        TestFixtures.waitingEntry(booth, 1L, 1, WaitingStatus.WAITING),
                        TestFixtures.waitingEntry(booth, 2L, 2, WaitingStatus.WAITING)));

        assertThat(waitingService.getWaitingQueue(3L)).hasSize(2);
    }
}
