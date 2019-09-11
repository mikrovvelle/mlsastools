package com.marklogic.sastools;

import com.epam.parso.Column;
import com.epam.parso.impl.SasFileReaderImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SasFileReaderJson extends SasFileReaderImpl {

    private ObjectMapper mapper = new ObjectMapper();
    private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;

    public SasFileReaderJson(InputStream inputStream) {
        super(inputStream);
    }

    ArrayNode readNextToArray() throws IOException {
        Object[] thing = this.readNext();
        return mapper.valueToTree(thing);
    }

    ObjectNode readNextToObject() throws IOException {
        List<Column> columns = this.getColumns();
        List<Object> values = Arrays.asList(this.readNext());
        ObjectNode objectNode = mapper.createObjectNode();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (values.get(i) instanceof Double)
                objectNode.put(column.getName(), (double) values.get(i));
            else if (values.get(i) instanceof Integer)
                objectNode.put(column.getName(), (int) values.get(i));
            else if (values.get(i) instanceof Long)
                objectNode.put(column.getName(), (long) values.get(i));
            else
                objectNode.put(column.getName(), (byte[]) values.get(i));
        }
        return objectNode;
    }

    List<ObjectNode> readDataSetToObjectArray() throws IOException {
        List<ObjectNode> objectArray = new ArrayList<>();
        long maxRow = this.getSasFileProperties().getRowCount();
        for (long i = 0; i < maxRow; i++) {
            objectArray.add(this.readNextToObject());
        }
        return objectArray;
    }

    public ArrayNode readDataSetToArrayNode() throws IOException {
        long maxRow = this.getSasFileProperties().getRowCount();
        ArrayNode arrayNode = new ArrayNode(jsonNodeFactory, (int) maxRow);
        for (long i = 0; i < maxRow; i++) {
            arrayNode.add(this.readNextToObject());
        }
        return arrayNode;
    }
}
