package az.aladdin.emaildelivery.email;

import org.springframework.util.StringUtils;

public final class AdminEmailHtmlEnhancer {

    private AdminEmailHtmlEnhancer() {
    }

    public static String withTrackingPixel(String htmlBody, String trackingToken, String publicBaseUrl) {
        if (!StringUtils.hasText(htmlBody) || !StringUtils.hasText(trackingToken) || !StringUtils.hasText(publicBaseUrl)) {
            return htmlBody;
        }

        var base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        var pixel = """
                <img src="%s/public/v1/emails/open/%s" width="1" height="1" alt="" style="display:none!important;max-height:1px;max-width:1px;border:0;" />
                """.formatted(base, trackingToken);

        var lower = htmlBody.toLowerCase();
        var bodyClose = lower.lastIndexOf("</body>");
        if (bodyClose >= 0) {
            return htmlBody.substring(0, bodyClose) + pixel + htmlBody.substring(bodyClose);
        }

        return htmlBody + pixel;
    }

    public static String withUnsubscribeFooter(String htmlBody, String unsubscribeToken, String publicBaseUrl) {
        if (!StringUtils.hasText(htmlBody) || !StringUtils.hasText(unsubscribeToken) || !StringUtils.hasText(publicBaseUrl)) {
            return htmlBody;
        }

        var base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        var footer = """
                <div style="margin-top:32px;padding-top:16px;border-top:1px solid #e5e7eb;font-size:12px;color:#6b7280;text-align:center;">
                <p>If you no longer wish to receive these emails, <a href="%s/public/v1/emails/unsubscribe/%s">unsubscribe here</a>.</p>
                </div>
                """.formatted(base, unsubscribeToken);

        var lower = htmlBody.toLowerCase();
        var bodyClose = lower.lastIndexOf("</body>");
        if (bodyClose >= 0) {
            return htmlBody.substring(0, bodyClose) + footer + htmlBody.substring(bodyClose);
        }

        return htmlBody + footer;
    }

    public static String unsubscribeUrl(String unsubscribeToken, String publicBaseUrl) {
        if (!StringUtils.hasText(unsubscribeToken) || !StringUtils.hasText(publicBaseUrl)) {
            return null;
        }
        var base = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;
        return "%s/public/v1/emails/unsubscribe/%s".formatted(base, unsubscribeToken);
    }
}
