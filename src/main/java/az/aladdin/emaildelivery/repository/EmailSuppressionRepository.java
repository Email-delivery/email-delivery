package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailSuppression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;

public interface EmailSuppressionRepository extends JpaRepository<EmailSuppression, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT LOWER(s.email) FROM EmailSuppression s
            WHERE LOWER(s.email) IN :emails
            """)
    Set<String> findExistingEmailsLowercase(@Param("emails") Collection<String> emails);

    @Query("""
            SELECT s FROM EmailSuppression s
            WHERE (:search IS NULL OR LOWER(s.email) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY s.unsubscribedAt DESC
            """)
    Page<EmailSuppression> search(@Param("search") String search, Pageable pageable);
}
