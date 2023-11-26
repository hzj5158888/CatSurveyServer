package com.codecat.catsurvey.service;

import com.codecat.catsurvey.commcon.exception.ValidationException;
import com.codecat.catsurvey.commcon.models.Question;
import com.codecat.catsurvey.commcon.models.Survey;
import com.codecat.catsurvey.commcon.repository.QuestionRepository;
import com.codecat.catsurvey.commcon.repository.SurveyRepository;
import com.codecat.catsurvey.commcon.valid.group.validationTime;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@NoArgsConstructor
@Validated
public class QuestionService {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SurveyRepository surveyRepository;

    @Validated(value = validationTime.FullAdd.class)
    public void checkFullAdd(@Valid Question question) {}

    @Validated(value = validationTime.FullUpdate.class)
    public void checkFullUpdate(@Valid Question question) {}

    public void setIOrder(Integer questionId, Integer iOrder) {
        Question question = questionRepository.findById(questionId).orElseThrow(() ->
                new ValidationException("问题不存在")
        );

        List<Question> sortedQuestion = questionRepository.findAllBySurveyId(
                question.getSurveyId(), Sort.by("iOrder")
        );

        if (iOrder == null || iOrder >= sortedQuestion.size())
            iOrder = Math.max(sortedQuestion.size() - 1, 0);
        if (iOrder < 0)
            throw new ValidationException("iOrder非法");

        List<Question> res = new ArrayList<>();
        for (int i = 0; i < sortedQuestion.size(); i++)
        {
            sortedQuestion.get(i).setIOrder(i);

            Integer curId = sortedQuestion.get(i).getId();
            if (questionId.equals(curId) && !iOrder.equals(i))
                continue;
            else if (iOrder.equals(i))
            {
                res.add(question);
                iOrder = -1;
                if (!curId.equals(questionId))
                    i--;

                continue;
            }

            res.add(sortedQuestion.get(i));
        }
        if (!iOrder.equals(-1))
            res.add(question);

        for (int i = 0; i < res.size(); i++)
            res.get(i).setIOrder(i);

        questionRepository.saveAllAndFlush(res);
    }
}
