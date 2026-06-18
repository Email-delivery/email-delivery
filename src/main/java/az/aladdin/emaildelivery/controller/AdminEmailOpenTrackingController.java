package az.aladdin.emaildelivery.controller;

import az.aladdin.emaildelivery.service.email.AdminEmailOpenTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("public/v1/emails")
@RequiredArgsConstructor
public class AdminEmailOpenTrackingController {

    private static final byte[] TRANSPARENT_GIF = Base64.getDecoder().decode(
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    private final AdminEmailOpenTrackingService adminEmailOpenTrackingService;

    @GetMapping("/open/{token}")
    public ResponseEntity<byte[]> trackOpen(@PathVariable String token) {
        adminEmailOpenTrackingService.recordOpen(token);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .cacheControl(CacheControl.noStore())
                .body(TRANSPARENT_GIF);
    }
}
