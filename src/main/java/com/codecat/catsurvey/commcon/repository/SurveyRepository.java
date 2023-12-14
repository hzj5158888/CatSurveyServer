package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.Survey;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Integer> {
    List<Survey> findAllByUserId(Integer userId);

    List<Survey> findAllByUserId(Integer userId, Sort sort);

    Optional<Survey> findByIdAndUserId(Integer surveyId, Integer userId);

    boolean existsByIdAndUserId(Integer surveyId, Integer userId);
}
