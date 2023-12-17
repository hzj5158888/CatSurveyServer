package com.codecat.catsurvey.common.Enum.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum QuestionTypeEnum {
    RADIO("单选"),
    CHECKBOX("多选"),
    TEXTAREA("文本"),
    TEXT("单行文本");

    private String name;
}
