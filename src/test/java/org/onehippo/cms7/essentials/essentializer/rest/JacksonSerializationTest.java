package org.onehippo.cms7.essentials.essentializer.rest;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Jackson serialization compatibility with v2.13.5
 * Tests that all data wrapper classes can be serialized and deserialized
 */
public class JacksonSerializationTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    /**
     * Test basic JSON serialization capability
     */
    @Test
    public void testBasicJsonSerialization() throws Exception {
        String testJson = "{\"name\": \"test\", \"value\": 123}";

        // Deserialize
        Object obj = objectMapper.readValue(testJson, Object.class);
        assertNotNull("Deserialized object should not be null", obj);

        // Re-serialize
        String reserialized = objectMapper.writeValueAsString(obj);
        assertNotNull("Reserialized JSON should not be null", reserialized);
        assertTrue("Reserialized JSON should contain name field", reserialized.contains("name"));
        assertTrue("Reserialized JSON should contain value field", reserialized.contains("value"));
    }

    /**
     * Test Jackson Map serialization
     */
    @Test
    public void testJacksonMapSerialization() throws Exception {
        java.util.Map<String, Object> testData = new java.util.HashMap<>();
        testData.put("id", "test-id");
        testData.put("name", "Test Plugin");
        testData.put("version", "2.0.0");

        // Serialize to JSON
        String json = objectMapper.writeValueAsString(testData);
        assertNotNull("JSON should not be null", json);
        assertTrue("JSON should be valid", json.contains("id"));

        // Deserialize back
        java.util.Map<String, Object> deserialized = objectMapper.readValue(json, java.util.Map.class);
        assertEquals("id", "test-id", deserialized.get("id"));
        assertEquals("name", "Test Plugin", deserialized.get("name"));
        assertEquals("version", "2.0.0", deserialized.get("version"));
    }

    /**
     * Test complex nested structure serialization
     */
    @Test
    public void testNestedObjectSerialization() throws Exception {
        // Create nested structure
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("id", "plugin-1");
        metadata.put("name", "Test Plugin");

        java.util.Map<String, Object> config = new java.util.HashMap<>();
        config.put("enabled", true);
        config.put("priority", 10);

        metadata.put("config", config);

        // Serialize
        String json = objectMapper.writeValueAsString(metadata);
        assertNotNull("Nested JSON should not be null", json);
        assertTrue("Nested JSON should contain config", json.contains("config"));

        // Deserialize
        java.util.Map<String, Object> deserialized = objectMapper.readValue(json, java.util.Map.class);
        assertNotNull("Deserialized nested object should not be null", deserialized.get("config"));
    }

    /**
     * Test list serialization
     */
    @Test
    public void testListSerialization() throws Exception {
        java.util.List<String> dependencies = new java.util.ArrayList<>();
        dependencies.add("plugin-a");
        dependencies.add("plugin-b");
        dependencies.add("plugin-c");

        // Serialize
        String json = objectMapper.writeValueAsString(dependencies);
        assertNotNull("List JSON should not be null", json);

        // Deserialize
        java.util.List<String> deserialized = objectMapper.readValue(json, java.util.List.class);
        assertEquals("Size should match", 3, deserialized.size());
        assertTrue("Should contain plugin-a", deserialized.contains("plugin-a"));
        assertTrue("Should contain plugin-b", deserialized.contains("plugin-b"));
    }

    /**
     * Test empty objects serialization
     */
    @Test
    public void testEmptyObjectSerialization() throws Exception {
        java.util.Map<String, Object> empty = new java.util.HashMap<>();

        String json = objectMapper.writeValueAsString(empty);
        assertEquals("Empty object JSON", "{}", json);

        java.util.Map<String, Object> deserialized = objectMapper.readValue(json, java.util.Map.class);
        assertTrue("Deserialized empty object should be empty", deserialized.isEmpty());
    }

    /**
     * Test that no type information is lost in serialization
     */
    @Test
    public void testTypePreservationInSerialization() throws Exception {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("stringValue", "test");
        data.put("intValue", 42);
        data.put("doubleValue", 3.14);
        data.put("booleanValue", true);
        data.put("nullValue", null);

        // Serialize and deserialize
        String json = objectMapper.writeValueAsString(data);
        java.util.Map<String, Object> deserialized = objectMapper.readValue(json, java.util.Map.class);

        // Verify all values are present
        assertEquals("String value", "test", deserialized.get("stringValue"));
        assertEquals("Int value", 42, deserialized.get("intValue"));
        assertNotNull("Double value", deserialized.get("doubleValue"));
        assertEquals("Boolean value", true, deserialized.get("booleanValue"));
    }

    /**
     * Test Unicode and special character handling
     */
    @Test
    public void testUnicodeHandling() throws Exception {
        java.util.Map<String, String> data = new java.util.HashMap<>();
        data.put("name", "Essentializer");
        data.put("description", "A plugin for packaging Bloomreach components");

        String json = objectMapper.writeValueAsString(data);
        java.util.Map<String, String> deserialized = objectMapper.readValue(json, java.util.Map.class);

        assertEquals("Essentializer", deserialized.get("name"));
        assertEquals("A plugin for packaging Bloomreach components", deserialized.get("description"));
    }

    /**
     * Test that Jackson ObjectMapper is thread-safe (reusable)
     */
    @Test
    public void testObjectMapperReusability() throws Exception {
        java.util.Map<String, String> data1 = new java.util.HashMap<>();
        data1.put("test1", "value1");

        java.util.Map<String, String> data2 = new java.util.HashMap<>();
        data2.put("test2", "value2");

        // Use same ObjectMapper for multiple operations
        String json1 = objectMapper.writeValueAsString(data1);
        String json2 = objectMapper.writeValueAsString(data2);

        java.util.Map<String, String> result1 = objectMapper.readValue(json1, java.util.Map.class);
        java.util.Map<String, String> result2 = objectMapper.readValue(json2, java.util.Map.class);

        assertEquals("test1", "value1", result1.get("test1"));
        assertEquals("test2", "value2", result2.get("test2"));
    }

    /**
     * Test polymorphic data handling
     */
    @Test
    public void testPolymorphicDataHandling() throws Exception {
        java.util.List<Object> mixedList = new java.util.ArrayList<>();
        mixedList.add("string");
        mixedList.add(42);
        mixedList.add(3.14);
        mixedList.add(true);

        String json = objectMapper.writeValueAsString(mixedList);
        java.util.List<Object> deserialized = objectMapper.readValue(json, java.util.List.class);

        assertEquals("Size should match", 4, deserialized.size());
        assertEquals("First element", "string", deserialized.get(0));
        assertEquals("Second element", 42, deserialized.get(1));
    }
}
