package com.sejong.event.ticket.dto;

import com.sejong.event.ticket.domain.Ticket;
import com.sejong.event.ticket.domain.TicketStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TicketResponse {

    private Long id;
    private Long eventId;
    private String eventTitle;
    private String studentId;
    private String studentName;
    private String ticketNumber;
    private String qrToken;
    private TicketStatus status;
    private Boolean isEntered;
    private LocalDateTime issuedAt;

    public static TicketResponse from(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .eventId(ticket.getEvent().getId())
                .eventTitle(ticket.getEvent().getTitle())
                .studentId(ticket.getStudentId())
                .studentName(ticket.getStudentName())
                .ticketNumber(ticket.getTicketNumber())
                .qrToken(ticket.getQrToken())
                .status(ticket.getStatus())
                .isEntered(ticket.getIsEntered())
                .issuedAt(ticket.getIssuedAt())
                .build();
    }
}
