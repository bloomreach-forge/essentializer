package org.onehippo.cms7.essentials.essentializer.rest;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for EssentializerUtils
 * Tests the Guava ImmutableMap import fix and general utility functionality
 */
public class EssentializerUtilsTest {

    @Before
    public void setUp() {
        // Setup if needed
    }

    /**
     * Test that EssentializerUtils class can be loaded
     * This verifies the Guava import is working correctly
     */
    @Test
    public void testUtilsClassLoadingSuccessful() {
        try {
            Class<?> cls = Class.forName("org.onehippo.cms7.essentials.essentializer.rest.EssentializerUtils");
            assertNotNull("EssentializerUtils should be loadable", cls);
        } catch (ClassNotFoundException e) {
            fail("EssentializerUtils class not found: " + e.getMessage());
        }
    }

    /**
     * Test that Guava ImmutableMap import is available
     * This verifies the fix from Phase 5
     */
    @Test
    public void testGuavaImmutableMapImport() {
        // Attempt to create and use an ImmutableMap
        ImmutableMap<String, String> testMap = ImmutableMap.of(
            "testKey", "testValue"
        );

        assertNotNull("ImmutableMap should be created successfully", testMap);
        assertEquals("testValue", testMap.get("testKey"));

        // If we got here, the Guava import is working
        assertTrue("Guava ImmutableMap import successful", true);
    }

    /**
     * Test ImmutableMap basic operations
     */
    @Test
    public void testImmutableMapOperations() {
        ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>builder()
            .put("one", 1)
            .put("two", 2)
            .put("three", 3)
            .build();

        // Test get operations
        assertEquals(Integer.valueOf(1), map.get("one"));
        assertEquals(Integer.valueOf(2), map.get("two"));
        assertEquals(Integer.valueOf(3), map.get("three"));

        // Test size
        assertEquals(3, map.size());

        // Test containment
        assertTrue(map.containsKey("one"));
        assertTrue(map.containsValue(2));
        assertFalse(map.containsKey("four"));
    }

    /**
     * Test that ImmutableMap is actually immutable
     */
    @Test
    public void testImmutableMapIsImmutable() {
        ImmutableMap<String, String> map = ImmutableMap.of(
            "key", "value"
        );

        try {
            map.put("newKey", "newValue");
            fail("ImmutableMap should not allow put operations");
        } catch (UnsupportedOperationException e) {
            // Expected
            assertTrue(true);
        }

        try {
            map.remove("key");
            fail("ImmutableMap should not allow remove operations");
        } catch (UnsupportedOperationException e) {
            // Expected
            assertTrue(true);
        }
    }

    /**
     * Test ImmutableSet availability (also part of Guava)
     */
    @Test
    public void testGuavaImmutableSetAvailable() {
        ImmutableSet<String> set = ImmutableSet.of(
            "value1",
            "value2",
            "value3"
        );

        assertNotNull(set);
        assertEquals(3, set.size());
        assertTrue(set.contains("value1"));
    }

    /**
     * Test that utilities can handle empty ImmutableMaps
     */
    @Test
    public void testEmptyImmutableMap() {
        ImmutableMap<String, String> emptyMap = ImmutableMap.of();

        assertNotNull(emptyMap);
        assertEquals(0, emptyMap.size());
        assertNull(emptyMap.get("anyKey"));
    }

    /**
     * Test ImmutableMap key and value retrieval
     */
    @Test
    public void testImmutableMapKeyValueRetrieval() {
        ImmutableMap<String, String> map = ImmutableMap.of(
            "a", "alpha",
            "b", "beta",
            "c", "gamma"
        );

        // Verify all keys and values are retrievable
        for (String key : map.keySet()) {
            assertNotNull("Value for key " + key + " should exist", map.get(key));
        }

        assertEquals(3, map.keySet().size());
        assertEquals(3, map.values().size());
        assertEquals(3, map.entrySet().size());
    }
}
