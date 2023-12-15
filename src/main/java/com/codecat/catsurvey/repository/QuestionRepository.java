package com.codecat.catsurvey.repository;

import com.codecat.catsurvey.models.Question;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    Optional<Question> findByIdAndSurveyId(Integer questionId, Integer surveyId);

    @Modifying
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteAllBySurveyId(Integer surveyId);

    boolean existsByIdAndSurveyId(Integer questionId, Integer surveyId);

    List<Question> findAllBySurveyId(Integer surveyId);

    List<Question> findAllBySurveyId(Integer surveyId, Sort sort);
}
