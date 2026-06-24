package com.sejong.event.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class IssueTicketRequest {

    @NotBlank(message = "학번은 필수입니다.")
    private String studentId;

    private String studentName;
}
