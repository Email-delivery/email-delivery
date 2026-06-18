package az.aladdin.emaildelivery.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
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
@Table(name = "email_campaigns")
public class EmailCampaign extends BaseEntity {

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "default_subject", length = 200)
    private String defaultSubject;

    @Column(name = "default_html_body", columnDefinition = "LONGTEXT")
    private String defaultHtmlBody;

    @Column(name = "template_id")
    private Long templateId;

    @Builder.Default
    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<EmailCampaignContact> contacts = new ArrayList<>();
}
