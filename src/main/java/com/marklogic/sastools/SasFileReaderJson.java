package com.marklogic.sastools;

import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;
import com.epam.parso.impl.SasFileReaderImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SasFileReaderJson extends SasFileReaderImpl {

    private ObjectMapper mapper = new ObjectMapper();
    private JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
    private SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");

    /**
     * constructor entry point for SAS-to-JSON conversion.
     *
     * @param inputStream should represent a SAS7BDAT file
     */
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
            else if (values.get(i) instanceof Date) {
                Date date = (Date) values.get(i);
                String dateString = iso8601.format(date);
                objectNode.put(column.getName(), dateString);
            }
            else
                objectNode.put(column.getName(), (String) values.get(i));
        }
        return objectNode;
    }

    /**
     * parse SAS7BDAT data set to ArrayList of JSON objects
     *
     * @return java.util.ArrayList of Jackson (JSON) objects:
     *  - each member object represents one row from the SAS7BDAT data set
     *  - object keys represent column names
     *  - object values represent the cell value for that column in that row
     * @throws IOException if parsing the SAS7BDAT format fails
     */
    public List<ObjectNode> readDataSetToObjectArray() throws IOException {
        List<ObjectNode> objectArray = new ArrayList<>();
        long maxRow = this.getSasFileProperties().getRowCount();
        for (long i = 0; i < maxRow; i++) {
            objectArray.add(this.readNextToObject());
        }
        return objectArray;
    }

    /**
     * parse SAS7BDAT data set to a single JSON Array
     *
     * @return Jackson ArrayNode (JSON array) of JSON objects:
     *  - each child object represents one row from the SAS7BDAT data set
     *  - object keys represent column names
     *  - object values represent the cell value for that column in that row
     * @throws IOException if parsing the SAS7BDAT format fails
     */
    public ArrayNode readDataSetToArrayNode() throws IOException {
        long maxRow = this.getSasFileProperties().getRowCount();
        ArrayNode arrayNode = new ArrayNode(jsonNodeFactory, (int) maxRow);
        for (long i = 0; i < maxRow; i++) {
            arrayNode.add(this.readNextToObject());
        }
        return arrayNode;
    }

    /**
     * build a JSON object out of a SAS7BDAT file's properties.
     *
     * @return flat Jackson (JSON) object of properties which apply to the entire SAS7BDAT file.
     * "dateCreated" and "dateModified" are converted to iso8601 dates.
     */
    public ObjectNode readPropertiesToObject() {
        SasFileProperties properties = this.getSasFileProperties();
        ObjectNode o = mapper.valueToTree(properties);
        o.set("dateCreated", new TextNode(iso8601.format(properties.getDateCreated())));
        o.set("dateModified", new TextNode(iso8601.format(properties.getDateModified())));
        return o;
    }
}
