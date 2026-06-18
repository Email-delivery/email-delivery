package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailCampaign;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByTemplateId(Long templateId);

    @Query("""
            SELECT c FROM EmailCampaign c
            WHERE (:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(c.description, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<EmailCampaign> search(@Param("search") String search, Pageable pageable);
}
