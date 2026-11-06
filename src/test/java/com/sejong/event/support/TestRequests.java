package com.sejong.event.support;

import com.sejong.event.account.dto.CreateAccountRequest;
import com.sejong.event.attendance.dto.ManualTicketRequest;
import com.sejong.event.attendance.dto.ScanRequest;
import com.sejong.event.auth.dto.LoginRequest;
import com.sejong.event.booth.dto.CreateBoothRequest;
import com.sejong.event.booth.dto.UpdateBoothRequest;
import com.sejong.event.booth.dto.UpdateBoothStatusRequest;
import com.sejong.event.event.dto.CreateEventRequest;
import com.sejong.event.event.dto.UpdateEventRequest;
import com.sejong.event.festival.dto.UpdateFestivalStatusRequest;
import com.sejong.event.waiting.dto.WaitingRegisterRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 요청 DTO는 @Getter 만 있어 세터·빌더가 없다.
 * 테스트 입력은 리플렉션으로 필드를 채워 만든다.
 */
public final class TestRequests {

    private TestRequests() {
    }

    public static LoginRequest login(String username, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "username", username);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    public static ScanRequest scan(String studentId, String name, String qrValue) {
        ScanRequest request = new ScanRequest();
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "qrValue", qrValue);
        return request;
    }

    public static ManualTicketRequest manualTicket(String studentId, String name) {
        ManualTicketRequest request = new ManualTicketRequest();
        ReflectionTestUtils.setField(request, "studentId", studentId);
        ReflectionTestUtils.setField(request, "name", name);
        return request;
    }

    public static CreateEventRequest createEvent(String name, String location, Integer targetCount) {
        CreateEventRequest request = new CreateEventRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "location", location);
        ReflectionTestUtils.setField(request, "targetCount", targetCount);
        return request;
    }

    public static UpdateEventRequest updateEvent(String name) {
        UpdateEventRequest request = new UpdateEventRequest();
        ReflectionTestUtils.setField(request, "name", name);
        return request;
    }

    public static CreateBoothRequest createBooth(String name, String category) {
        CreateBoothRequest request = new CreateBoothRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "category", category);
        ReflectionTestUtils.setField(request, "location", "잔디마당");
        return request;
    }

    public static UpdateBoothRequest updateBooth(String name) {
        UpdateBoothRequest request = new UpdateBoothRequest();
        ReflectionTestUtils.setField(request, "name", name);
        return request;
    }

    public static CreateBoothRequest createBoothAt(String name, double latitude, double longitude) {
        CreateBoothRequest request = createBooth(name, "주점");
        ReflectionTestUtils.setField(request, "latitude", latitude);
        ReflectionTestUtils.setField(request, "longitude", longitude);
        return request;
    }

    public static UpdateBoothRequest updateBoothCoordinates(double latitude, double longitude) {
        UpdateBoothRequest request = new UpdateBoothRequest();
        ReflectionTestUtils.setField(request, "latitude", latitude);
        ReflectionTestUtils.setField(request, "longitude", longitude);
        return request;
    }

    public static UpdateBoothStatusRequest boothStatus(boolean isOpen) {
        UpdateBoothStatusRequest request = new UpdateBoothStatusRequest();
        ReflectionTestUtils.setField(request, "isOpen", isOpen);
        return request;
    }

    public static WaitingRegisterRequest waiting(String name, String phone, Integer partySize) {
        WaitingRegisterRequest request = new WaitingRegisterRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "phone", phone);
        ReflectionTestUtils.setField(request, "partySize", partySize);
        return request;
    }

    public static CreateAccountRequest createAccount(String username, String password) {
        CreateAccountRequest request = new CreateAccountRequest();
        ReflectionTestUtils.setField(request, "username", username);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "name", "운영진");
        ReflectionTestUtils.setField(request, "organization", "학생회");
        return request;
    }

    public static UpdateFestivalStatusRequest festivalStatus(boolean isActive) {
        UpdateFestivalStatusRequest request = new UpdateFestivalStatusRequest();
        ReflectionTestUtils.setField(request, "isActive", isActive);
        return request;
    }
}
