package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ioption_template", schema = "codecat")
public class OptionTemplate implements Comparable<OptionTemplate> {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "questionTemplateId不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "question_template_id", nullable = false)
    private Integer questionTemplateId;

    @NotBlank(message = "content不能为空")
    @Column(name = "content", nullable = false, length = -1)
    private String content;

    @Column(name = "i_order", nullable = false)
    private Integer iOrder;

    @ToString.Exclude
    @JsonIgnore
    @ManyToOne(targetEntity = QuestionTemplate.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "question_template_id", referencedColumnName = "id", insertable = false, updatable = false)
    private QuestionTemplate questionTemplate;

    @Override
    public int compareTo(OptionTemplate other) {
        if (!questionTemplateId.equals(other.getQuestionTemplateId()))
            return questionTemplateId > other.getQuestionTemplateId() ? 1 : -1;
        if (iOrder.equals(other.getIOrder()))
            return 0;

        return iOrder > other.iOrder ? 1 : -1;
    }
}
