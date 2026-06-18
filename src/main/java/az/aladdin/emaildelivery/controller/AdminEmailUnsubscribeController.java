package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.service.email.AdminEmailUnsubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("public/v1/emails")
@RequiredArgsConstructor
public class AdminEmailUnsubscribeController {

    private final AdminEmailUnsubscribeService adminEmailUnsubscribeService;

    @GetMapping(value = "/unsubscribe/{token}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> unsubscribeGet(@PathVariable String token) {
        var success = adminEmailUnsubscribeService.unsubscribe(token);
        return ResponseEntity.ok(renderPage(success));
    }

    @PostMapping("/unsubscribe/{token}")
    public ResponseEntity<Void> unsubscribePost(@PathVariable String token) {
        adminEmailUnsubscribeService.unsubscribe(token);
        return ResponseEntity.ok().build();
    }

    private String renderPage(boolean success) {
        if (success) {
            return """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head><meta charset="UTF-8"><title>Unsubscribed</title>
                    <style>body{font-family:system-ui,sans-serif;max-width:480px;margin:4rem auto;padding:0 1rem;color:#111}
                    h1{font-size:1.25rem}p{color:#555;line-height:1.5}</style></head>
                    <body><h1>You have been unsubscribed</h1>
                    <p>You will no longer receive marketing emails from us at this address.</p></body></html>
                    """;
        }
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head><meta charset="UTF-8"><title>Link expired</title>
                <style>body{font-family:system-ui,sans-serif;max-width:480px;margin:4rem auto;padding:0 1rem;color:#111}
                h1{font-size:1.25rem}p{color:#555;line-height:1.5}</style></head>
                <body><h1>This unsubscribe link is invalid or has expired</h1>
                <p>If you still receive unwanted emails, please contact support.</p></body></html>
                """;
    }
}
