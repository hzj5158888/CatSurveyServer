package com.codecat.catsurvey.repository;

import com.codecat.catsurvey.models.OptionTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface OptionTemplateRepository extends JpaRepository<OptionTemplate, Integer> {
    List<OptionTemplate> findAllByQuestionTemplateId(Integer questionTemplateId);

    List<OptionTemplate> findAllByQuestionTemplateId(Integer questionTemplateId, Sort sort);

    boolean existsByIdAndQuestionTemplateId(Integer optionTemplateId, Integer questionTemplateId);

    @Modifying
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteAllByQuestionTemplateId(Integer questionTemplateId);

    Optional<OptionTemplate> findByIdAndQuestionTemplateId(Integer optionTemplateId, Integer questionTemplateId);
}
