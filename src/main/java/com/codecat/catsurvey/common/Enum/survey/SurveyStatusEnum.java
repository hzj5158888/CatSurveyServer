package com.codecat.catsurvey.common.Enum.survey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SurveyStatusEnum {
    DRAFT("草稿"),
    CARRYOUT("进行中"),
    CLOSED("已结束"),
    TEMPLATE("模板");

    private String name;
}
