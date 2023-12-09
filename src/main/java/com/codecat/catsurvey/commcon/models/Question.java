package com.codecat.catsurvey.commcon.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.commcon.valid.function.question.QuestionTypeExists;
import com.codecat.catsurvey.commcon.valid.function.survey.SurveyIdExists;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class Question implements Comparable<Question> {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "surveyId不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @SurveyIdExists(message = "surveyId不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "survey_id", nullable = false)
    private Integer surveyId;

    @NotBlank(message = "content不能为空")
    @Column(name = "content", nullable = false, length = -1)
    private String content;

    @NotBlank(message = "问题类型不能为空白")
    @QuestionTypeExists(message = "问题类型非法")
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "i_order", nullable = false)
    private Integer iOrder;

    @NotNull(message = "请选择是否必答")
    @Range(min = 0, max = 1, message = "isRequired的取值为0或1")
    @Column(name = "is_required", nullable = false)
    private byte isRequired = 1;

    @JSONField(serialize = false)
    @ToString.Exclude
    @ManyToOne(targetEntity = Survey.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "survey_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Survey survey;

    @OrderBy("iOrder asc")
    @OneToMany(targetEntity = Option.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Option> optionList = new ArrayList<>();

    @OneToMany(targetEntity = AnswerDetail.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<AnswerDetail> answerDetailList = new ArrayList<>();

    @Override
    public int compareTo(Question other) {
        if (!surveyId.equals(other.getSurveyId()))
            return surveyId > other.getSurveyId() ? 1 : -1;
        if (iOrder.equals(other.getIOrder()))
            return 0;

        return iOrder > other.iOrder ? 1 : -1;
    }
}
