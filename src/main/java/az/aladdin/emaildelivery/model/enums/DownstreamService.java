package az.aladdin.emaildelivery.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The back-ends this admin panel orchestrates.
 */
@Getter
@RequiredArgsConstructor
public enum DownstreamService {

    PMS("stay-board (PMS)"),
    RMS("stay-board-rms (RMS)");

    private final String displayName;
}
