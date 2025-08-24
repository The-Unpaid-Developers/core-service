package com.project.core_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VersionService Tests")
class VersionServiceTest {

    @InjectMocks
    private VersionService versionService;

    @Nested
    @DisplayName("Increment Patch Version Tests")
    class IncrementPatchVersionTests {

        @Test
        @DisplayName("Should increment patch version correctly for version with 'v' prefix")
        void shouldIncrementPatchVersionCorrectlyWithVPrefix() {
            // Arrange
            String currentVersion = "v1.2.3";

            // Act
            String result = versionService.incrementPatchVersion(currentVersion);

            // Assert
            assertEquals("v1.2.4", result);
        }

        @Test
        @DisplayName("Should increment patch version correctly for version without 'v' prefix")
        void shouldIncrementPatchVersionCorrectlyWithoutVPrefix() {
            // Arrange
            String currentVersion = "1.2.3";

            // Act
            String result = versionService.incrementPatchVersion(currentVersion);

            // Assert
            assertEquals("v1.2.4", result);
        }

        @Test
        @DisplayName("Should handle patch version overflow correctly")
        void shouldHandlePatchVersionOverflowCorrectly() {
            // Arrange
            String currentVersion = "v1.2.999";

            // Act
            String result = versionService.incrementPatchVersion(currentVersion);

            // Assert
            assertEquals("v1.2.1000", result);
        }

        @Test
        @DisplayName("Should handle zero patch version correctly")
        void shouldHandleZeroPatchVersionCorrectly() {
            // Arrange
            String currentVersion = "v1.0.0";

            // Act
            String result = versionService.incrementPatchVersion(currentVersion);

            // Assert
            assertEquals("v1.0.1", result);
        }

        @Test
        @DisplayName("Should return default version for null input")
        void shouldReturnDefaultVersionForNullInput() {
            // Act
            String result = versionService.incrementPatchVersion(null);

            // Assert
            assertEquals("v1.0.0", result);
        }

        @Test
        @DisplayName("Should throw exception for empty input")
        void shouldThrowExceptionForEmptyInput() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> versionService.incrementPatchVersion(""));

            assertEquals("Version string cannot be null or empty", exception.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "1.2",
                "1.2.3.4",
                "v1.2",
                "v1.2.3.4",
                "1.2.a",
                "a.b.c",
                "v1.2.a",
                "1.2.3-SNAPSHOT",
                "version-1.2.3"
        })
        @DisplayName("Should throw exception for invalid format")
        void shouldThrowExceptionForInvalidFormat(String invalidVersion) {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> versionService.incrementPatchVersion(invalidVersion));

            assertTrue(exception.getMessage().contains("Invalid version format"));
        }

        @Test
        @DisplayName("Should handle large version numbers")
        void shouldHandleLargeVersionNumbers() {
            // Arrange
            String currentVersion = "v999.999.999";

            // Act
            String result = versionService.incrementPatchVersion(currentVersion);

            // Assert
            assertEquals("v999.999.1000", result);
        }
    }

    @Nested
    @DisplayName("Version Validation Tests")
    class VersionValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "v1.0.0",
                "v1.2.3",
                "v10.20.30",
                "v999.999.999",
                "v0.0.0",
                "1.0.0",
                "1.2.3",
                "10.20.30"
        })
        @DisplayName("Should validate correct version formats as valid")
        void shouldValidateCorrectVersionFormatsAsValid(String validVersion) {
            // Act
            boolean result = versionService.isValidVersion(validVersion);

            // Assert
            assertTrue(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid",
                "1.2",
                "1.2.3.4",
                "v1.2",
                "v1.2.3.4",
                "1.2.a",
                "a.b.c",
                "v1.2.a",
                "1.2.3-SNAPSHOT",
                "version-1.2.3",
                "",
                " ",
                "v 1.2.3",
                "v1. 2.3",
                "v1.2. 3"
        })
        @DisplayName("Should validate incorrect version formats as invalid")
        void shouldValidateIncorrectVersionFormatsAsInvalid(String invalidVersion) {
            // Act
            boolean result = versionService.isValidVersion(invalidVersion);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false for null version")
        void shouldReturnFalseForNullVersion() {
            // Act
            boolean result = versionService.isValidVersion(null);

            // Assert
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Version Comparison Tests")
    class VersionComparisonTests {

        @ParameterizedTest
        @MethodSource("provideVersionComparisonData")
        @DisplayName("Should compare versions correctly")
        void shouldCompareVersionsCorrectly(String version1, String version2, int expectedResult) {
            // Act
            int result = versionService.compareVersions(version1, version2);

            // Assert
            if (expectedResult < 0) {
                assertTrue(result < 0, String.format("Expected %s < %s", version1, version2));
            } else if (expectedResult > 0) {
                assertTrue(result > 0, String.format("Expected %s > %s", version1, version2));
            } else {
                assertEquals(0, result, String.format("Expected %s == %s", version1, version2));
            }
        }

        static Stream<Arguments> provideVersionComparisonData() {
            return Stream.of(
                    // version1, version2, expected result
                    Arguments.of("v1.0.0", "v1.0.0", 0), // Equal versions
                    Arguments.of("1.0.0", "v1.0.0", 0), // Equal versions with/without v
                    Arguments.of("v1.0.0", "v1.0.1", -1), // Patch difference
                    Arguments.of("v1.0.1", "v1.0.0", 1), // Patch difference
                    Arguments.of("v1.0.0", "v1.1.0", -1), // Minor difference
                    Arguments.of("v1.1.0", "v1.0.0", 1), // Minor difference
                    Arguments.of("v1.0.0", "v2.0.0", -1), // Major difference
                    Arguments.of("v2.0.0", "v1.0.0", 1), // Major difference
                    Arguments.of("v1.2.3", "v1.2.4", -1), // Complex patch
                    Arguments.of("v1.2.4", "v1.2.3", 1), // Complex patch
                    Arguments.of("v1.2.3", "v1.3.0", -1), // Complex minor
                    Arguments.of("v1.3.0", "v1.2.3", 1), // Complex minor
                    Arguments.of("v1.2.3", "v2.0.0", -1), // Complex major
                    Arguments.of("v2.0.0", "v1.2.3", 1), // Complex major
                    Arguments.of("v10.20.30", "v10.20.31", -1), // Large numbers
                    Arguments.of("v0.0.1", "v0.0.2", -1), // Small numbers
                    Arguments.of("v999.999.999", "v999.999.999", 0) // Very large equal
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid version1 in comparison")
        void shouldThrowExceptionForInvalidVersion1InComparison() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions("invalid", "v1.0.0"));

            assertEquals("Invalid version format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for invalid version2 in comparison")
        void shouldThrowExceptionForInvalidVersion2InComparison() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions("v1.0.0", "invalid"));

            assertEquals("Invalid version format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for both invalid versions in comparison")
        void shouldThrowExceptionForBothInvalidVersionsInComparison() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions("invalid1", "invalid2"));

            assertEquals("Invalid version format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for null versions in comparison")
        void shouldThrowExceptionForNullVersionsInComparison() {
            // Test null version1
            IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions(null, "v1.0.0"));
            assertEquals("Invalid version format", exception1.getMessage());

            // Test null version2
            IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions("v1.0.0", null));
            assertEquals("Invalid version format", exception2.getMessage());

            // Test both null
            IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class,
                    () -> versionService.compareVersions(null, null));
            assertEquals("Invalid version format", exception3.getMessage());
        }
    }

    @Nested
    @DisplayName("Default Version Tests")
    class DefaultVersionTests {

        @Test
        @DisplayName("Should return correct default version")
        void shouldReturnCorrectDefaultVersion() {
            // Act
            String result = versionService.getDefaultVersion();

            // Assert
            assertEquals("v1.0.0", result);
        }

        @Test
        @DisplayName("Should return valid default version")
        void shouldReturnValidDefaultVersion() {
            // Act
            String defaultVersion = versionService.getDefaultVersion();

            // Assert
            assertTrue(versionService.isValidVersion(defaultVersion));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete version lifecycle")
        void shouldHandleCompleteVersionLifecycle() {
            // Start with default version
            String defaultVersion = versionService.getDefaultVersion();
            assertTrue(versionService.isValidVersion(defaultVersion));
            assertEquals("v1.0.0", defaultVersion);

            // Increment several times
            String v1 = versionService.incrementPatchVersion(defaultVersion);
            assertEquals("v1.0.1", v1);
            assertTrue(versionService.isValidVersion(v1));

            String v2 = versionService.incrementPatchVersion(v1);
            assertEquals("v1.0.2", v2);
            assertTrue(versionService.isValidVersion(v2));

            String v3 = versionService.incrementPatchVersion(v2);
            assertEquals("v1.0.3", v3);
            assertTrue(versionService.isValidVersion(v3));

            // Compare versions
            assertTrue(versionService.compareVersions(defaultVersion, v1) < 0);
            assertTrue(versionService.compareVersions(v1, v2) < 0);
            assertTrue(versionService.compareVersions(v2, v3) < 0);
            assertTrue(versionService.compareVersions(v3, defaultVersion) > 0);
        }

        @Test
        @DisplayName("Should handle version comparison chain")
        void shouldHandleVersionComparisonChain() {
            // Create a chain of versions
            String[] versions = {
                    "v1.0.0",
                    "v1.0.1",
                    "v1.1.0",
                    "v1.1.1",
                    "v2.0.0",
                    "v2.0.1"
            };

            // Test that each version is less than the next
            for (int i = 0; i < versions.length - 1; i++) {
                assertTrue(versionService.compareVersions(versions[i], versions[i + 1]) < 0,
                        String.format("%s should be less than %s", versions[i], versions[i + 1]));
            }

            // Test that each version is greater than the previous
            for (int i = 1; i < versions.length; i++) {
                assertTrue(versionService.compareVersions(versions[i], versions[i - 1]) > 0,
                        String.format("%s should be greater than %s", versions[i], versions[i - 1]));
            }
        }

        @Test
        @DisplayName("Should handle version increment workflow")
        void shouldHandleVersionIncrementWorkflow() {
            // Test that default version can be incremented
            String defaultVersion = versionService.getDefaultVersion();
            assertEquals("v1.0.0", defaultVersion);

            // Increment the default version
            String result = versionService.incrementPatchVersion(defaultVersion);
            assertEquals("v1.0.1", result);

            // Validate both versions
            assertTrue(versionService.isValidVersion(defaultVersion));
            assertTrue(versionService.isValidVersion(result));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesAndBoundaryTests {

        @Test
        @DisplayName("Should handle maximum integer version values")
        void shouldHandleMaximumIntegerVersionValues() {
            // This tests behavior near integer limits (though realistically these wouldn't
            // occur)
            String largeVersion = "v999999.999999.999999";

            // Should still be valid
            assertTrue(versionService.isValidVersion(largeVersion));

            // Should be able to increment
            String incremented = versionService.incrementPatchVersion(largeVersion);
            assertEquals("v999999.999999.1000000", incremented);
        }

        @Test
        @DisplayName("Should handle whitespace in version strings")
        void shouldHandleWhitespaceInVersionStrings() {
            // Versions with leading/trailing whitespace should be invalid
            assertFalse(versionService.isValidVersion(" v1.0.0"));
            assertFalse(versionService.isValidVersion("v1.0.0 "));
            assertFalse(versionService.isValidVersion(" v1.0.0 "));

            // Increment should throw exception for whitespace versions
            assertThrows(IllegalArgumentException.class,
                    () -> versionService.incrementPatchVersion(" v1.0.0"));
            assertThrows(IllegalArgumentException.class,
                    () -> versionService.incrementPatchVersion("v1.0.0 "));
        }

        @Test
        @DisplayName("Should handle version strings with different casing")
        void shouldHandleVersionStringsWithDifferentCasing() {
            // Only lowercase 'v' should be valid
            assertTrue(versionService.isValidVersion("v1.0.0"));
            assertFalse(versionService.isValidVersion("V1.0.0"));

            // Increment should throw exception for uppercase V
            assertThrows(IllegalArgumentException.class,
                    () -> versionService.incrementPatchVersion("V1.0.0"));
        }
    }
}
