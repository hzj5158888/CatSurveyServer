package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.function.survey.SurveyIdExists;
import com.codecat.catsurvey.common.valid.function.user.UserIdExists;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "response", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "问卷ID不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @SurveyIdExists(message = "问卷ID不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "survey_id", nullable = false)
    private Integer surveyId;

    @UserIdExists(message = "用户ID不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @CreatedDate
    @Column(name = "submit_date", nullable = false)
    private Date submitDate;

    @JSONField(serialize = false)
    @ToString.Exclude
    @ManyToOne(targetEntity = Survey.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "survey_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Survey survey;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "response_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<AnswerDetail> answerDetailList = new ArrayList<>();
}
