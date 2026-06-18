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
@Table(name = "email_sender_identities")
public class EmailSenderIdentity extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String email;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private boolean defaultSender = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
