package com.project.core_service.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling version number operations.
 * Supports semantic versioning format: vMAJOR.MINOR.PATCH
 */
@Service
@Slf4j
public class VersionService {

    private static final String DEFAULT_VERSION = "v1.0.0";
    private static final String VERSION_REGEX = "^v?\\d+\\.\\d+\\.\\d+$";

    /**
     * Increments the patch version (last number) of a semantic version string.
     * 
     * @param currentVersion The current version string (e.g., "v1.2.3" or "1.2.3")
     * @return The incremented version string with 'v' prefix (e.g., "v1.2.4")
     * @throws IllegalArgumentException if the version format is invalid or
     *                                  null/empty
     */
    public String incrementPatchVersion(String currentVersion) {
        if (currentVersion == null) {
            return DEFAULT_VERSION;
        }

        if (currentVersion.isEmpty()) {
            log.error("Cannot increment null or empty version string");
            throw new IllegalArgumentException("Version string cannot be null or empty");
        }

        // Remove 'v' prefix if present
        String versionWithoutPrefix = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

        // Validate format
        if (!("v" + versionWithoutPrefix).matches(VERSION_REGEX)) {
            log.error("Invalid version format: {}", currentVersion);
            throw new IllegalArgumentException("Invalid version format: " + currentVersion +
                    ". Expected format: vMAJOR.MINOR.PATCH (e.g., v1.2.3)");
        }

        // Split by dots
        String[] versionParts = versionWithoutPrefix.split("\\.");

        try {
            int major = Integer.parseInt(versionParts[0]);
            int minor = Integer.parseInt(versionParts[1]);
            int patch = Integer.parseInt(versionParts[2]);

            // Check for potential overflow (though unlikely in practice)
            if (patch == Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "Patch version overflow: cannot increment beyond " + Integer.MAX_VALUE);
            }

            // Increment patch version
            patch++;

            String newVersion = String.format("v%d.%d.%d", major, minor, patch);
            log.debug("Incremented version from {} to {}", currentVersion, newVersion);
            return newVersion;
        } catch (NumberFormatException e) {
            log.error("Failed to parse version numbers from: {}", currentVersion, e);
            throw new IllegalArgumentException(
                    "Invalid version format - unable to parse numeric components: " + currentVersion, e);
        }
    }

    /**
     * Validates if a version string follows the expected format.
     * 
     * @param version The version string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidVersion(String version) {
        return version != null && version.matches(VERSION_REGEX);
    }

    /**
     * Compares two version strings.
     * 
     * @param version1 First version string
     * @param version2 Second version string
     * @return negative if version1 < version2, 0 if equal, positive if version1 >
     *         version2
     */
    public int compareVersions(String version1, String version2) {
        if (!isValidVersion(version1) || !isValidVersion(version2)) {
            throw new IllegalArgumentException("Invalid version format");
        }

        String[] v1Parts = version1.replaceFirst("^v", "").split("\\.");
        String[] v2Parts = version2.replaceFirst("^v", "").split("\\.");

        for (int i = 0; i < 3; i++) {
            int v1Part = Integer.parseInt(v1Parts[i]);
            int v2Part = Integer.parseInt(v2Parts[i]);

            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }

        return 0; // Versions are equal
    }

    /**
     * Gets the default starting version.
     * 
     * @return The default version string
     */
    public String getDefaultVersion() {
        return DEFAULT_VERSION;
    }
}
