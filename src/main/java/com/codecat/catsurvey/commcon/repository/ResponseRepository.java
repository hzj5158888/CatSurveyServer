package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Integer> {
    Optional<Response> findByIdAndSurveyId(Integer responseId, Integer surveyId);

    List<Response> findAllBySurveyId(Integer surveyId);

    boolean existsByIdAndSurveyId(Integer responseId, Integer surveyId);
}
