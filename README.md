# JSDB (Java Simple DB)
A simple Mongo like interface upon SQLite.

The JSDB interface provides a MONGO like interface on top of SQLite for those how looking simple (Java/Json) interface 
and embeded database. 

Below follows some examples outlining the essential functionality provided by JSDB
Full examples are found in the [_TestDB.java_](https://github.com/hoddmimes/JSDB/blob/main/src/test/java/com/hoddmimes/sdb/test/TestDB.java) class. 

### Create Database
```java
public static void createDatabase(String pSqlFile) throws JSDBException
```
Create a Database file in the location specified by the parameter _pSqlFile_

### Create Collection
```java
 public JSDBCollection createCollection(String pName, JSDBKey... pKeys ) throws JSDBException
```
Creates a Collection with keys specified by the parameter _pKeys_


### Open Database
```java
 public void openDatabase( String pSqlFile) throws JSDBException
```
Open a Database in the location specified by the parameter _pSqlFile_

### Close Database
```java
public void closeDatabase()
```
Close an open database

### Get Collection
```java
public JSDBCollection getCollections(String pName) throws JSDBException
```

Get a _Collection_ object specified by the _pName_ parameter

## JSDBCollection

All operations on data in the DB is performed in the context of a _collection_.
The JSDBCollection interface provides the following functionality.

### Find All
```java
public List<JsonObject> findAll() throws JSDBException 
```
Retrieve all JsonObjects kept/stored in the _collection_.

```java
public List<JsonObject> findAll(int pOffset, int pMaxObjects ) throws JSDBException 
```
Retrieve all JsonObjects kept/stored in the _collection_ from pOffset and limited to _pMaxObjects_.
May be applicable when having large data sets.

### Find 
```java
public List<JsonObject> find(String pFilterString) throws JSDBException 
```
Retrieve all JsonObjects kept/stored in the _collection_ matching the filter specified by the parameter _pFilterString_

### Insert
```java
public void insert(JsonObject pObject, boolean pUpdate) throws JSDBException
```
Insert (or update) a JsonObject in the _collection_. An update will occur if the object exists and the _pUpdate_ parameter is true.
If the objects exists and the _pUpdate_ parameter is false, an exception is thrown.

```java
public void insert(JsonObject pObject) throws JSDBException
```
Insert or update a JsonObject in the _collection_. An update will occur if the object exists.

### Update 
```java
public int update(String pFilterString, JsonObject pDeltaObject ) throws JSDBException
```
Updates objects in the _collection_ with data specified by the parameter _pDeltaObject_ matching the filter specified by the
the _pFilterString_ parameter. 

The pDeltaObject contains the field/structor of the data that should be updated. The delta object contains a subset of the data
elements in the JSON objects kept/stored in the _collection_.

```java
public void update(JsonObject pObject, boolean insert) throws JSDBException 
```
Update (or insert) an JsonObject in the _collection_. An insert will occur if the object does exists and the _pInsert_ parameter is true.
If the object does not exist and the _pInsert_ parameter is false, an exception is thrown.

```java
public void update(JsonObject pObject) throws JSDBException
```
Update a JsonObject in the _collection_. If the object does not exists an exception is thrown.

### Delete 
```java
public void deleteAll() throws JSDBException
```
Deletes all JsonObject in the _collection_.

```java
public void delete( String pFilterString) throws JSDBException 
```
Deletes all JsonObject in the _collection_ matching the the filter.

## Collection Filter
A collection filter is used to select a set of JsonObjects from the collections.
The filter is a combination of logical operators, fields and properaties.

For example;
```
    ($and: ($gt: (age, 28)) ($like: (name,'Fr%')))
```
in the example above any JsonObject containing a field 'age' having a value > 28 and a field 'name' having a
(string) value starting with 'Fr' will match and be subject for operation.

The logical operation on data field are $GT:,$GTE:,$LT:,$LTE:,$NE:,$EQ:,$LIKE:
For booleans the $NE:,$EQ: operators are the only applicable ones.
The $LIKE: operator is only applicable for String fields, and have the save semantic as SQL LIKE.

The operator $AND: and $OR: are used to combined 2 or more logical expressions.

It's possible to specify fields in Json object substructures. The character "." is used as separator of structures 
```JSON
    {"foo": {"bar" : {"frotz" : 4711}}}
}
```
_The notation for addressing the field 'frotz' is equivalent to "foo.bar.frotz"_

_Note!_ if a field specified is not a key in the collection. The filter needs to be matched agains all 
JsonObjects in the collection, otherwise the SQL filter can be applied directly with better performance.




