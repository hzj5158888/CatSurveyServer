package com.codecat.catsurvey.repository;

import com.codecat.catsurvey.models.Survey;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Integer> {
    List<Survey> findAllByUserId(Integer userId);

    List<Survey> findAllByUserId(Integer userId, Sort sort);

    Optional<Survey> findByIdAndUserId(Integer surveyId, Integer userId);

    boolean existsByIdAndUserId(Integer surveyId, Integer userId);

    @Modifying
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteAllByUserId(Integer loginId);
}
