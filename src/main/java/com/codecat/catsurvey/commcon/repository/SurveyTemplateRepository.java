package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.SurveyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyTemplateRepository extends JpaRepository<SurveyTemplate, Integer> {
}
