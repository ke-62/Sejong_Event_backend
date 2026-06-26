package com.sejong.event.attendance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManualTicketResponse {

    private Integer ticketNumber;
    private String studentId;
    private String name;
}
