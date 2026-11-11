package com.sejong.event.booth.dto;

import com.sejong.event.support.TestRequests;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBoothRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("지도 좌표가 없으면 부스 신청을 거부한다")
    void requiresCoordinates() {
        CreateBoothRequest request = TestRequests.createBooth("감성주점", "주점");
        ReflectionTestUtils.setField(request, "latitude", null);
        ReflectionTestUtils.setField(request, "longitude", null);

        Set<ConstraintViolation<CreateBoothRequest>> violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("latitude", "longitude");
    }

    @Test
    @DisplayName("위치 설명은 비워도 신청할 수 있다")
    void locationIsOptional() {
        CreateBoothRequest request = TestRequests.createBooth("감성주점", "주점");
        ReflectionTestUtils.setField(request, "location", null);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    @DisplayName("캠퍼스 범위를 벗어난 좌표는 거부한다")
    void rejectsOutOfCampus() {
        CreateBoothRequest request = TestRequests.createBoothAt("감성주점", 37.60, 127.0743);

        assertThat(validator.validate(request)).extracting(v -> v.getPropertyPath().toString())
                .containsExactly("latitude");
    }
}
