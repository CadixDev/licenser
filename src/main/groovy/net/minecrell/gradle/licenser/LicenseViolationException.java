package net.minecrell.gradle.licenser;

import org.gradle.api.GradleException;

/**
 * Thrown if a license violation was found.
 */
public class LicenseViolationException extends GradleException {

    /**
     * Constructs a new {@link LicenseViolationException} with the specified
     * message.
     *
     * @param message The exception message
     */
    public LicenseViolationException(String message) {
        super(message);
    }

}
