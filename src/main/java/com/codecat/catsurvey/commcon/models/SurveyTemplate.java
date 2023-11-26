package com.codecat.catsurvey.commcon.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.commcon.valid.function.survey.SurveyStatusExists;
import com.codecat.catsurvey.commcon.valid.function.user.UserIdExists;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "survey_template", schema = "codecat")
@EntityListeners(AuditingEntityListener.class)
public class SurveyTemplate {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "标题不能为空")
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = -1)
    private String description;

    @CreatedDate
    @Column(name = "create_date", nullable = false)
    private Date createDate;

    @UserIdExists(message = "userId不存在", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @SurveyStatusExists(message = "问卷状态非法")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = User.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    @OrderBy("iOrder asc")
    @OneToMany(targetEntity = QuestionTemplate.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "template_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Question> questionTemplateList = new ArrayList<>();
}
