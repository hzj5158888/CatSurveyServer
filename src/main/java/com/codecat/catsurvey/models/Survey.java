package com.codecat.catsurvey.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.common.valid.function.survey.SurveyStatusExists;
import com.codecat.catsurvey.common.valid.function.user.UserIdExists;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "survey", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @UserIdExists(message = "userId不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotBlank(message = "标题不能为空")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", length = -1)
    private String description;

    @CreatedDate
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @NotBlank(message = "状态不能为空")
    @SurveyStatusExists(message = "status非法")
    @Column(name = "status", nullable = false, length = 20)
    private String status; // 默认为草稿

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = User.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @OrderBy("iOrder asc")
    @OneToMany(targetEntity = Question.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "survey_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Question> questionList = new ArrayList<>();

    @OrderBy("submitDate desc")
    @OneToMany(targetEntity = Response.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "survey_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Response> responseList = new ArrayList<>();
}
