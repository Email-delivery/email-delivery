package az.aladdin.emaildelivery.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "email_address_book_contacts",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_address_book_email", columnNames = "email"))
public class EmailAddressBookContact extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 120)
    private String name;
}
