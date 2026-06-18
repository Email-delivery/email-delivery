package az.aladdin.emaildelivery.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name = "email_templates")
public class EmailTemplate extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(length = 200)
    private String subject;

    @Column(name = "html_body", columnDefinition = "LONGTEXT")
    private String htmlBody;
}
