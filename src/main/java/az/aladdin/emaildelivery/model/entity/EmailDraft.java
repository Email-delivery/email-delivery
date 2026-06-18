package az.aladdin.emaildelivery.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "email_drafts",
        uniqueConstraints = @UniqueConstraint(name = "uk_email_draft_user", columnNames = "user_id"))
public class EmailDraft extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 200)
    private String subject;

    @Column(name = "body_html", columnDefinition = "LONGTEXT")
    private String bodyHtml;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Builder.Default
    @Column(name = "show_cc", nullable = false)
    private boolean showCc = false;

    @Builder.Default
    @Column(name = "show_bcc", nullable = false)
    private boolean showBcc = false;

    @Builder.Default
    @OneToMany(mappedBy = "draft", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<EmailDraftRecipient> recipients = new ArrayList<>();
}
