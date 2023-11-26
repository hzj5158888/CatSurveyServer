package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.Option;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptionRepository extends JpaRepository<Option, Integer> {
    Optional<Option> findByIdAndQuestionId(Integer optionId, Integer questionId);

    boolean existsByIdAndQuestionId(Integer optionId, Integer questionId);

    List<Option> findAllByQuestionId(Integer questionId);

    List<Option> findAllByQuestionId(Integer questionId, Sort sort);

    @Modifying
    @Transactional
    void deleteAllByQuestionId(Integer questionId);
}
