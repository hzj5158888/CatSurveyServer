package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.valid.function.survey.SurveyIdExists;
import com.codecat.catsurvey.common.valid.function.survey.SurveyStatusExists;
import com.codecat.catsurvey.common.valid.function.user.UserIdExists;
import com.codecat.catsurvey.common.valid.group.validationTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class Template {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "survey_id", nullable = false)
    private Integer surveyId;

    @CreatedDate
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @NotNull(message = "问卷不能为空", groups = {validationTime.FullAdd.class})
    @ManyToOne(targetEntity = Survey.class, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "survey_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Survey survey;
}