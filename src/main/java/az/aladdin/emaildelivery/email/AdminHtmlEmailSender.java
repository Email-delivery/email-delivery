package az.aladdin.emaildelivery.email;

import az.aladdin.emaildelivery.annotation.NoFieldLogging;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminHtmlEmailSender {

    private final JavaMailSender javaMailSender;

    public void send(
            JavaMailSender mailSender,
            EmailFromAddress from,
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String htmlBody,
            List<AttachmentPayload> attachments) throws MessagingException {
        send(mailSender, from, to, cc, bcc, subject, htmlBody, attachments, null);
    }

    public void send(
            JavaMailSender mailSender,
            String from,
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String htmlBody,
            List<AttachmentPayload> attachments) throws MessagingException {
        send(mailSender, new EmailFromAddress(from, null), to, cc, bcc, subject, htmlBody, attachments, null);
    }

    public void send(
            JavaMailSender mailSender,
            EmailFromAddress from,
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String htmlBody,
            List<AttachmentPayload> attachments,
            String listUnsubscribeUrl) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var hasAttachments = attachments != null && !attachments.isEmpty();
        var helper = new MimeMessageHelper(message, hasAttachments, "UTF-8");

        helper.setFrom(buildInternetAddress(from));
        helper.setTo(to);
        if (cc != null && cc.length > 0) {
            helper.setCc(cc);
        }
        if (bcc != null && bcc.length > 0) {
            helper.setBcc(bcc);
        }
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        if (listUnsubscribeUrl != null && !listUnsubscribeUrl.isBlank()) {
            message.setHeader("List-Unsubscribe", "<" + listUnsubscribeUrl + ">");
            message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }

        if (hasAttachments) {
            for (var attachment : attachments) {
                var contentType = attachment.contentType() == null || attachment.contentType().isBlank()
                        ? "application/octet-stream"
                        : attachment.contentType();
                helper.addAttachment(
                        attachment.fileName(),
                        new ByteArrayResource(attachment.content()),
                        contentType);
            }
        }

        javaMailSender.send(message);
        log.debug("Admin email sent to {} recipient(s)", to.length);
    }

    public void send(
            String[] to,
            String[] cc,
            String[] bcc,
            String subject,
            String htmlBody,
            List<AttachmentPayload> attachments) throws MessagingException {
        var from = ((JavaMailSenderImpl) javaMailSender).getUsername();
        send(javaMailSender, new EmailFromAddress(from, null), to, cc, bcc, subject, htmlBody, attachments);
    }

    private InternetAddress buildInternetAddress(EmailFromAddress from) throws MessagingException {
        try {
            if (from.hasDisplayName()) {
                return new InternetAddress(from.email(), from.displayName(), "UTF-8");
            }
            return new InternetAddress(from.email());
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Failed to encode From address", e);
        }
    }

    @NoFieldLogging
    public record AttachmentPayload(String fileName, String contentType, byte[] content) {
    }
}
