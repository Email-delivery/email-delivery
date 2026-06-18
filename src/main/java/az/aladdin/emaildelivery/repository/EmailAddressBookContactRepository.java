package az.aladdin.emaildelivery.repository;

import az.aladdin.emaildelivery.model.entity.EmailAddressBookContact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailAddressBookContactRepository extends JpaRepository<EmailAddressBookContact, Long> {

    Optional<EmailAddressBookContact> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    @Query("""
            SELECT c FROM EmailAddressBookContact c
            WHERE (:search IS NULL OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(c.name, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            """)
    Page<EmailAddressBookContact> search(@Param("search") String search, Pageable pageable);
}
