package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.function.question.QuestionIdExists;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ioption", schema = "codecat")
public class Option implements Comparable<Option> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "questionId不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @QuestionIdExists(message = "questionId非法", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @NotBlank(message = "描述不能为空")
    @Column(name = "content", nullable = false, length = -1)
    private String content;

    @Column(name = "i_order", nullable = false)
    private Integer iOrder;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = Question.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Question question;

    @Override
    public int compareTo(Option other) {
        if (!questionId.equals(other.getQuestionId()))
            return questionId > other.getQuestionId() ? 1 : -1;
        if (iOrder.equals(other.getIOrder()))
            return 0;

        return iOrder > other.iOrder ? 1 : -1;
    }
}
