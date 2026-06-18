package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT t FROM EmailTemplate t
            WHERE (:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<EmailTemplate> search(@Param("search") String search, Pageable pageable);
}
