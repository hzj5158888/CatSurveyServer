package com.codecat.catsurvey.service;

import com.codecat.catsurvey.common.Enum.survey.SurveyStatusEnum;
import com.codecat.catsurvey.exception.CatValidationException;
import com.codecat.catsurvey.models.Option;
import com.codecat.catsurvey.models.Question;
import com.codecat.catsurvey.models.Survey;
import com.codecat.catsurvey.models.Template;
import com.codecat.catsurvey.common.valid.group.validationTime;
import com.codecat.catsurvey.repository.TemplateRepository;
import com.codecat.catsurvey.utils.Util;
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

    @Autowired
    private UserService userService;

    public Template filter(Template template) {
        if (template == null)
            return null;

        Survey survey =  surveyService.get(template.getSurveyId());
        List<Question> questionRes = new ArrayList<>();
        List<Question> questionList = surveyService.getQuestionList(survey.getId());
        for (Question question : questionList) {
            List<Option> optionList = questionService.getOptionList(question.getId());

            List<Option> optionRes = new ArrayList<>();
            for (Option option : optionList) {
                Option newOption = Util.clone(option, Option.class);
                newOption.setId(null);
                newOption.setQuestionId(null);
                optionRes.add(newOption);
            }

            Question newQuestion = Util.clone(question, Question.class);
            newQuestion.setOptionList(optionRes);
            newQuestion.setId(null);
            newQuestion.setAnswerDetailList(new ArrayList<>());
            newQuestion.setSurveyId(null);
            questionRes.add(newQuestion);
        }
        Survey newSurvey = Util.clone(survey, Survey.class);
        newSurvey.setQuestionList(questionRes);
        newSurvey.setResponseList(new ArrayList<>());
        newSurvey.setId(null);
        newSurvey.setUserId(null);

        template.setSurveyId(null);
        template.setSurvey(newSurvey);
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
        survey.setUserId(userService.getLoginId());
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
        if (template.getSurvey() == null)
            return;

        template.setId(templateId);
        template.setSurveyId(oldTemplate.getSurveyId());
        template.setCreateDate(oldTemplate.getCreateDate());
        surveyService.modify(oldTemplate.getSurveyId(), template.getSurvey());
        
        template.setSurvey(null);
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
