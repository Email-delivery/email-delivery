package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailDraft;

import java.util.Optional;

public interface EmailDraftRepository extends org.springframework.data.jpa.repository.JpaRepository<EmailDraft, Long> {

    Optional<EmailDraft> findByUserId(Long userId);
}
