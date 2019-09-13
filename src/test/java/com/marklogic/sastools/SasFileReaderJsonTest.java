package com.marklogic.sastools;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class SasFileReaderJsonTest {

    private ClassLoader classLoader = this.getClass().getClassLoader();
    private FileInputStream fis = new FileInputStream(
            Objects.requireNonNull(classLoader.getResource("all_rand_normal.sas7bdat")).getFile());
    private SasFileReaderJson sasFileReader = new SasFileReaderJson(fis);

    private FileInputStream acrofis = new FileInputStream(
            Objects.requireNonNull(classLoader.getResource("charset_acro.sas7bdat")).getFile());
    private SasFileReaderJson acroReader = new SasFileReaderJson(acrofis);


    SasFileReaderJsonTest() throws FileNotFoundException {
    }

    @Test
    void readNextToJsonNode() throws IOException {
        ArrayNode arrayNode = sasFileReader.readNextToArray();
        double x6val = arrayNode.get(5).asLong();
        assertTrue(x6val > 0.49, "row 1, col x6 should be greater than 0.49");
    }

    @Test
    void readNextToObject() throws IOException {
        ObjectNode objectNode = sasFileReader.readNextToObject();
        double x3val = objectNode.get("x3").asDouble();
        assertTrue(x3val > 1.44, "row 1, col x5 should be greater than 0.49");
        objectNode = sasFileReader.readNextToObject();
        double x3val2 =objectNode.get("x3").asDouble();
        assertNotEquals(x3val, x3val2);
    }

    @Test
    void readSetToObjectArray() throws IOException {
        List<ObjectNode> objectNodeList = sasFileReader.readDataSetToObjectArray();
        assertEquals(objectNodeList.size(), sasFileReader.getSasFileProperties().getRowCount());
        assertNotEquals(objectNodeList.get(8).get("x3"), objectNodeList.get(9).get("x3"));
    }

    @Test
    void readDataSetToArray() throws IOException {
        ArrayNode arrayNode = sasFileReader.readDataSetToArrayNode();
        assertEquals(arrayNode.size(), sasFileReader.getSasFileProperties().getRowCount());
        assertNotEquals(arrayNode.get(5).get("x4"), arrayNode.get(6).get("x4"));
    }

    @Test
    void readCharsetAcroToObjectNodeList() throws IOException {
        assertNotNull(acroReader);
        List<ObjectNode> objectNodeList = acroReader.readDataSetToObjectArray();
        assertEquals(150, objectNodeList.size());
    }

    @Test
    void readCharsetAcroToJacksonArray() throws IOException {
        ArrayNode arrayNode = acroReader.readDataSetToArrayNode();
        assertEquals(150, arrayNode.size());
        assertEquals("Iris-setosa", arrayNode.get(5).get("Species").textValue());
        assertEquals("Iris-versicolor", arrayNode.get(92).get("Species").textValue());
        assertEquals(2.8, arrayNode.get(122).get("SepalWidth").asDouble());
    }
}
