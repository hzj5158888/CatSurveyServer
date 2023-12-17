package com.codecat.catsurvey.common.Enum.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum PermissionNameEnum {
    USER_MANAGE("UserManage", "用户管理权"),
    TEMPLATE_MANAGE("TemplateManage", "模板管理权"),
    SURVEY_MANAGE("SurveyManage", "问卷管理权"),
    CUSTOMER_MANAGE("CustomerManage", "客服管理权"),
    DATA_MANAGE("DataManage", "数据分析权");

    private String name;
    private String chnName;

    public String getChnName(String name) {
        for (PermissionNameEnum roleNameEnum : PermissionNameEnum.values()) {
            if (roleNameEnum.getName().equals(name))
                return roleNameEnum.getChnName();
        }

        return null;
    }

    public String getName(String chnName) {
        for (PermissionNameEnum roleNameEnum : PermissionNameEnum.values()) {
            if (roleNameEnum.getChnName().equals(chnName))
                return roleNameEnum.getName();
        }

        return null;
    }
}
