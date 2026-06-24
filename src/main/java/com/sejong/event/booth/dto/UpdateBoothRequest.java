package com.sejong.event.booth.dto;

import lombok.Getter;

@Getter
public class UpdateBoothRequest {

    private String name;
    private String description;
    private String location;
    private String menu;
}
