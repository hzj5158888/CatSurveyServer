package com.codecat.catsurvey.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private Integer userId;
    private String userName;
    private List<String> role;
    private List<String> permission;
    private String accessToken;
}
