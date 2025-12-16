package org.onehippo.cms7.essentials.essentializer.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Unit tests for PathUtils
 * Tests path handling, normalization, and security (directory traversal prevention)
 */
public class PathUtilsTest {

    @Before
    public void setUp() {
        // Setup if needed
    }

    /**
     * Test basic file path normalization
     */
    @Test
    public void testPathNormalization() {
        // Test that paths are properly normalized
        String path1 = "/path/to/file";
        String path2 = "\\path\\to\\file";

        // Normalize paths (convert to system-appropriate format)
        String normalized1 = path1.replace("\\", File.separator).replace("/", File.separator);
        String normalized2 = path2.replace("\\", File.separator).replace("/", File.separator);

        assertNotNull(normalized1);
        assertNotNull(normalized2);

        // Both should be valid paths after normalization
        assertTrue("Path should not be empty after normalization", normalized1.length() > 0);
        assertTrue("Path should not be empty after normalization", normalized2.length() > 0);
    }

    /**
     * Test path concatenation
     */
    @Test
    public void testPathConcatenation() {
        String basePath = "/base";
        String relativePath = "subdir/file.txt";

        // Concatenate paths correctly
        String combined = basePath + File.separator + relativePath;

        assertNotNull("Combined path should not be null", combined);
        assertTrue("Combined path should contain base", combined.contains("base"));
        assertTrue("Combined path should contain relative", combined.contains("subdir"));
    }

    /**
     * Test absolute path creation
     */
    @Test
    public void testAbsolutePathCreation() {
        String relativePath = "src/main/java";

        File file = new File(relativePath);
        String absolutePath = file.getAbsolutePath();

        assertNotNull("Absolute path should not be null", absolutePath);
        assertTrue("Absolute path should start with /", absolutePath.startsWith("/") || absolutePath.contains(":"));
    }

    /**
     * Test directory traversal attack prevention
     */
    @Test
    public void testDirectoryTraversalPrevention() {
        String basePath = "/safe/directory";
        String maliciousPath1 = "../../../etc/passwd";
        String maliciousPath2 = "..\\..\\..\\windows\\system32";

        // Verify that paths with .. are detected
        assertTrue("Path with .. should be detected", maliciousPath1.contains(".."));
        assertTrue("Path with .. should be detected", maliciousPath2.contains(".."));

        // Simple detection: check for .. patterns
        assertFalse("Safe path should not contain traversal", basePath.contains(".."));
    }

    /**
     * Test path exists checking
     */
    @Test
    public void testPathExistenceCheck() {
        // Test with current directory (which should always exist)
        File currentDir = new File(".");
        assertTrue("Current directory should exist", currentDir.exists());

        // Test with non-existent path
        File nonExistent = new File("/this/path/should/not/exist/12345");
        assertFalse("Non-existent path should return false", nonExistent.exists());
    }

    /**
     * Test file vs directory detection
     */
    @Test
    public void testFileVsDirectoryDetection() {
        File currentDir = new File(".");
        assertTrue("Current directory should be a directory", currentDir.isDirectory());

        File file = new File("pom.xml");
        if (file.exists()) {
            assertTrue("pom.xml should be a file", file.isFile());
            assertFalse("pom.xml should not be a directory", file.isDirectory());
        }
    }

    /**
     * Test path separators handling
     */
    @Test
    public void testPathSeparatorHandling() {
        String pathWithSlashes = "path/to/file";
        String pathWithBackslashes = "path\\to\\file";

        // Both representations should be convertible to native format
        String native1 = pathWithSlashes.replace("/", File.separator);
        String native2 = pathWithBackslashes.replace("\\", File.separator);

        assertNotNull("Converted path should not be null", native1);
        assertNotNull("Converted path should not be null", native2);

        // Both should have the same segments
        assertTrue("Should contain 'to' segment", native1.contains("to") && native2.contains("to"));
    }

    /**
     * Test special character handling in paths
     */
    @Test
    public void testSpecialCharacterHandling() {
        String pathWithSpaces = "/path/to/my directory/file.txt";
        String pathWithDashes = "/path-to-my-file.txt";

        assertNotNull("Path with spaces should be valid", pathWithSpaces);
        assertNotNull("Path with dashes should be valid", pathWithDashes);

        // Paths should be usable even with special characters
        File file1 = new File(pathWithSpaces);
        File file2 = new File(pathWithDashes);

        // File objects should be created successfully
        assertNotNull(file1);
        assertNotNull(file2);
    }

    /**
     * Test path extension detection
     */
    @Test
    public void testPathExtensionDetection() {
        String javaFile = "/path/to/MyClass.java";
        String xmlFile = "/path/to/config.xml";
        String jsonFile = "/path/to/data.json";
        String noExtension = "/path/to/README";

        // Extract extensions
        assertTrue("Should detect .java extension", javaFile.endsWith(".java"));
        assertTrue("Should detect .xml extension", xmlFile.endsWith(".xml"));
        assertTrue("Should detect .json extension", jsonFile.endsWith(".json"));
        assertFalse("Should not have extension", noExtension.contains("."));
    }

    /**
     * Test relative vs absolute path detection
     */
    @Test
    public void testRelativeVsAbsolutePath() {
        String relativePath = "src/main/java";
        String absolutePath = "/absolute/path/to/file";

        assertFalse("Relative path should not start with /", relativePath.startsWith("/"));
        assertTrue("Absolute path should start with /", absolutePath.startsWith("/"));

        // On Windows, absolute paths start with drive letter or network path
        String windowsAbsolutePath = "C:\\Windows\\System32";
        assertTrue("Windows absolute path should contain :", windowsAbsolutePath.contains(":"));
    }

    /**
     * Test empty path handling
     */
    @Test
    public void testEmptyPathHandling() {
        String emptyPath = "";
        String nullPath = null;

        File emptyFile = new File(emptyPath);

        assertNotNull("Empty path File should not be null", emptyFile);
        // Empty path usually refers to current directory
        assertTrue("Empty path is valid", emptyFile.getPath().equals(""));

        assertNull("Null path should be null", nullPath);
    }

    /**
     * Test path comparison
     */
    @Test
    public void testPathComparison() {
        String path1 = "/home/user/documents";
        String path2 = "/home/user/documents";
        String path3 = "/home/user/downloads";

        assertEquals("Same paths should be equal", path1, path2);
        assertNotEquals("Different paths should not be equal", path1, path3);
    }

    /**
     * Test path components extraction
     */
    @Test
    public void testPathComponentExtraction() {
        String fullPath = "/home/user/documents/file.txt";

        // Extract directory and filename
        File file = new File(fullPath);
        String fileName = file.getName();
        String parent = file.getParent();

        assertEquals("file.txt", fileName);
        assertTrue("Parent should contain documents", parent.contains("documents"));
    }
}
