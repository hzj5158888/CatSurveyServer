package com.codecat.catsurvey.commcon.Enum.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum QuestionTypeEnum {
    RADIO("单选"),
    CHECKBOX("多选"),
    TEXT("文本");

    private String name;
}
