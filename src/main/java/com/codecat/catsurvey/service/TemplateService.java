package com.codecat.catsurvey.service;

import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Option;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.models.Template;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.repository.TemplateRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class TemplateService {
    @Autowired
    private TemplateRepository templateRepository;

    @Autowired
    private SurveyService surveyService;

    @Autowired
    private QuestionService questionService;

    public Template filter(Template template) {
        if (template == null)
            return null;

        Survey survey =  surveyService.get(template.getSurveyId());
        List<Question> questionList = surveyService.getQuestionList(survey.getId());
        for (Question question : questionList) {
            List<Option> optionList = questionService.getOptionList(question.getId());
            for (Option option : optionList)
                option.setId(null);

            question.setOptionList(optionList);
            question.setId(null);
        }
        survey.setQuestionList(questionList);
        survey.setResponseList(new ArrayList<>());

        template.setSurveyId(null);
        template.setSurvey(survey);
        return template;
    }

    public List<Template> filter(List<Template> templateList) {
        if (templateList == null)
            return null;

        List<Template> ans = new ArrayList<>();
        for (Template template : templateList)
            ans.add(filter(template));

        return ans;
    }

    @Transactional
    public void add(Template template) {
        if (template == null)
            throw new CatValidationException("非法请求，数据为空");

        Survey survey = template.getSurvey();
        if (survey == null)
            throw new CatValidationException("问卷不能为空");

        survey.setId(null);
        survey.setStatus(SurveyStatusEnum.DRAFT.getName()); // 草稿
        surveyService.add(survey);
        template.setSurveyId(survey.getId());
        templateRepository.saveAndFlush(template);
    }

    @Transactional
    public void del(Integer templateId) {
        if (templateId == null)
            throw new CatValidationException("非法请求，无效数据");
        if (!templateRepository.existsById(templateId))
            throw new CatValidationException("模板不存在");

        templateRepository.deleteById(templateId);
    }

    @Transactional
    public void modify(Integer templateId, Template template) {
        Template oldTemplate = templateRepository.findById(templateId).orElseThrow(() ->
                new CatValidationException("模板不存在")
        );

        template.setId(templateId);
        template.setCreateDate(oldTemplate.getCreateDate());
        if (template.getSurvey() == null) {
            templateRepository.saveAndFlush(template);
            return;
        }

        if (template.getSurvey().getId() == null) {
            surveyService.add(template.getSurvey());
            template.setSurveyId(template.getSurvey().getId());
        } else {
            surveyService.modify(oldTemplate.getSurveyId(), template.getSurvey());
        }

        templateRepository.saveAndFlush(template);
    }

    @Transactional
    public List<Template> getAll() {
        return templateRepository.findAll(Sort.by(Sort.Direction.DESC, "createDate"));
    }

    @Transactional
    public Template get(Integer templateId) {
        return templateRepository.findById(templateId).orElseThrow(() ->
                new CatValidationException("模板不存在")
        );
    }

    @Validated(validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Template template) {}
}
