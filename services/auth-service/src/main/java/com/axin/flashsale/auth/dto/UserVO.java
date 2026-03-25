package com.axin.flashsale.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String roles;
    private Boolean enabled;
    private LocalDateTime createTime;
}
