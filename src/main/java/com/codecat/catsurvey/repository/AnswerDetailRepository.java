package com.codecat.catsurvey.repository;

import com.codecat.catsurvey.models.AnswerDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface AnswerDetailRepository extends JpaRepository<AnswerDetail, Integer> {
    Optional<AnswerDetail> findByIdAndResponseId(Integer answerDetailId, Integer responseId);

    List<AnswerDetail> findAllByResponseId(Integer responseId);

    boolean existsByIdAndResponseId(Integer id, Integer responseId);

    boolean existsByResponseIdAndQuestionId(Integer responseId, Integer questionId);

    @Modifying
    @Transactional(isolation = Isolation.SERIALIZABLE)
    void deleteAllByResponseId(Integer responseId);
}
