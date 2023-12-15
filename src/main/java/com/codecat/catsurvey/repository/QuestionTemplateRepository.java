package com.codecat.catsurvey.repository;

import com.codecat.catsurvey.models.QuestionTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, Integer> {
    Optional<QuestionTemplate> findByIdAndTemplateId(Integer questionTemplateId, Integer surveyTemplateId);

    List<QuestionTemplate> findAllByTemplateId(Integer surveyTemplateId);

    List<QuestionTemplate> findAllByTemplateId(Integer surveyTemplateId, Sort sort);

    boolean existsByIdAndTemplateId(Integer questionTemplateId, Integer surveyTemplateId);

    @Modifying
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteAllByTemplateId(Integer surveyTemplateId);
}
