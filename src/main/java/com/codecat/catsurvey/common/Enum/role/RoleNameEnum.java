package com.codecat.catsurvey.common.Enum.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RoleNameEnum {
    USER("User", "用户"),
    SUPER_ADMIN("SuperAdmin", "超级管理员"),
    USER_ADMIN("UserAdmin", "用户管理员"),
    TEMPLATE_ADMIN("TemplateAdmin", "模板管理员"),
    SURVEY_ADMIN("SurveyAdmin", "问卷管理员"),
    CUSTOMER_ADMIN("ServiceAdmin", "客服管理员"),
    DATA_ANALYSE("DataAnalyse", "统计分析员");

    private String name;
    private String chnName;

    public static String getChnName(String name) {
        for (RoleNameEnum roleNameEnum : RoleNameEnum.values()) {
            if (roleNameEnum.getName().equals(name))
                return roleNameEnum.getChnName();
        }

        return null;
    }

    public static String getName(String chnName) {
        for (RoleNameEnum roleNameEnum : RoleNameEnum.values()) {
            if (roleNameEnum.getChnName().equals(chnName))
                return roleNameEnum.getName();
        }

        return null;
    }
}
