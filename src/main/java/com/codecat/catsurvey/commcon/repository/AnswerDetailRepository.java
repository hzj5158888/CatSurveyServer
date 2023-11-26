package com.codecat.catsurvey.commcon.repository;

import com.codecat.catsurvey.commcon.models.AnswerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface AnswerDetailRepository extends JpaRepository<AnswerDetail, Integer> {
    Optional<AnswerDetail> findByIdAndResponseId(Integer answerDetailId, Integer responseId);

    List<AnswerDetail> findAllByResponseId(Integer responseId);

    boolean existsByResponseIdAndQuestionId(Integer id, Integer id1);
}
