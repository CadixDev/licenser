package net.minecrell.gradle.licenser;

import org.gradle.api.GradleException;

public class LicenseViolationException extends GradleException {

    public LicenseViolationException(String message) {
        super(message);
    }

}
