# MarkLogic SAS Tools

The idea here is to store SAS7BDAT file content in MarkLogic, as JSON documents, using the row-as-document convention.
This Java library provides functions to convert SAS data sets to objects, enabling easy ingest to MarkLogic, which means either:

- an ArrayList of Jackson ObjectNodes (JSON objects), with each member representing a row of the original data set, or
- a Jackson ArrayNode (a JSON array), with each member an ObjectNode representing a row of the original data set

Which method you use depends on the constraints of your project.

Additionally, you can extract the properties of a given SAS7BDAT file to a Jackson object, so it's easy to store that data in MarkLogic as well.

The dirty work is done by the [epam/parso library](https://github.com/epam/parso), which turns SAS binary data into usable Java data. "Sastools" (this project), just takes parso's output and marshalls it into Jackson.

## Context

You bring your own:

- SAS files
- MarkLogic client e.g.:
  - The REST API
  - MarkLogic's `DatabaseClient`
  - Data Services
- row naming strategy (e.g. row numbers, content hashes, etc.)

## Usage

To use the library, instantiate a `SasFileReaderJson` on an `InputStream` from a SAS7BDAT file:

```java
SasFileReaderJson sas = new SasFileReaderJson(sasInputStream);
```

Then you can use `readDataSetToObjectArray()`, `readDataSetToArrayNode()`, and `readPropertiesToObject()` to prepare the data set and/or content for ingest to MarkLogic.

### ArrayList of Jackson objects: `readDataSetToObjectArray`

If you will do your looping in Java, it's probably easiest to use this method

```java
FileInputStream fis = new FileInputStream("/path/to/sasfile.sas7bdat");
SasFileReaderJson sas = new SasFileReaderJson(fis);
List<ObjectNode> oList = sasFileReader.readDataSetToObjectArray();
```

With that list, and an existing `jsonDocumentManager` from your `DatabaseClient`, you'd probably do something like this to insert all the JSON data:

```java
for (ObjectNode o : oList) {
  JacksonHandle jacksonHandle = new JacksonHandle(o);
  jsonDocumentManager.write("/" + filename + UUID.randomUUID().toString() + ".json", jacksonHandle);
}
```

(note that you might want something more useful than random strings for your document URIs)

### (Jackson) ArrayNode of Jackson objects: `readDataSetToArrayNode`

If you will send the entire data set contents to MarkLogic in a single operation—e.g. with Data Services handling the writes on the server side—this method is preferable.

```java
FileInputStream fis = new FileInputStream("/path/to/sasfile.sas7bdat");
SasFileReaderJson sas = new SasFileReaderJson(fis);
ArrayNode arrayNode = sasFileReader.readDataSetToArrayNode();
```

Then, for example, if you have a Data Services interface, you'd call that for the insert:

```java
dataservicesInterface.myIngestApiFunction(arrayNode)
```

And then there'd be some server-side logic to write the `arrayNode` members to MarkLogic:

```js
var arrayNode; // instance of ArrayNode from Data Services client

let lngth = arrayNode.length;
let padding = lngth.toString().length;

for (r = 0; r < lngth; r++) {
  let row = rows[r];
  let rowIdx = r.toString().padStart(padding, '0');
  let rowUri = fn.stringJoin([prefix, filename, "row" + rowIdx + ".json"], "/");
  xdmp.documentInsert(rowUri, row);
}
```

### SAS file properties to a Jackson object: `readPropertiesToObject`

This one's pretty simple. It reads the SAS7BDAT file properties (not the data set) to an ObjectNode, which can then be written to MarkLogic as a flat JSON document:

```java
ObjectNode properties = sasFileReaderJson.readPropertiesToObject();
```

You'd then want to store this somwhere near the data set, to keep track of associated metadata for those rows.

