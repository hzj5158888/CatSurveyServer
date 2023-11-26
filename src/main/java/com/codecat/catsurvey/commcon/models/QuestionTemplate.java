package com.codecat.catsurvey.commcon.models;

import com.alibaba.fastjson2.annotation.JSONField;
import com.codecat.catsurvey.commcon.valid.function.question.QuestionTypeExists;
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
import org.hibernate.validator.constraints.Range;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_template", schema = "codecat")
public class QuestionTemplate implements Comparable<QuestionTemplate> {
    @Id
    @Null(message = "id为只读", groups = {validationTime.FullAdd.class, validationTime.Add.class})
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull(message = "问题模板不能为空", groups = {validationTime.FullAdd.class, validationTime.FullUpdate.class})
    @Column(name = "template_id", nullable = false)
    private Integer templateId;

    @NotBlank(message = "content不能为空")
    @Column(name = "content", nullable = false, length = -1)
    private String content;

    @NotBlank(message = "问题类型不能为空白")
    @QuestionTypeExists(message = "问题类型非法")
    @Column(name = "type", nullable = false, length = -1)
    private String type;

    @Column(name = "i_order", nullable = false)
    private Integer iOrder;

    @NotNull(message = "请选择是否必答")
    @Range(min = 0, max = 1, message = "isRequired的取值为0或1")
    @Column(name = "is_required", nullable = false)
    private byte isRequired = 1;

    @ToString.Exclude
    @JSONField(serialize = false)
    @ManyToOne(targetEntity = SurveyTemplate.class, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "template_id", referencedColumnName = "id", insertable = false, updatable = false)
    private SurveyTemplate surveyTemplate;

    @OrderBy("iOrder asc")
    @OneToMany(targetEntity = OptionTemplate.class, fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    @JoinColumn(name = "question_template_id", referencedColumnName = "id", insertable = false, updatable = false)
    private List<Option> optionTemplateList = new ArrayList<>();

    @Override
    public int compareTo(QuestionTemplate other) {
        if (!templateId.equals(other.getTemplateId()))
            return templateId > other.getTemplateId() ? 1 : -1;
        if (iOrder.equals(other.getIOrder()))
            return 0;

        return iOrder > other.iOrder ? 1 : -1;
    }
}
