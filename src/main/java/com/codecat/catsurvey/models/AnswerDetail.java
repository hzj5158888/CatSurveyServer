package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.function.question.QuestionIdExists;
import com.codecat.catsurvey.common.valid.function.response.ResponseIdExists;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "answer_detail", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class AnswerDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "responseId不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @ResponseIdExists(message = "答卷不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "response_id", nullable = false)
    private Integer responseId;

    @NotNull(message = "questionId不能为空")
    @QuestionIdExists(message = "问题不存在", groups = {
            validationTime.FullAdd.class,
            validationTime.FullUpdate.class,
            validationTime.Add.class,
            validationTime.Update.class}
    )
    @Column(name = "question_id", nullable = false)
    private Integer questionId;

    @Column(name = "option_id")
    private Integer optionId;

    @NotNull(message = "答案不能为空")
    @Column(name = "json_answer", nullable = false)
    private Object jsonAnswer;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = Response.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "response_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Response response;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = Question.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "question_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Question question;
}
