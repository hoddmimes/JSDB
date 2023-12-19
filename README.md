# JSDB (Java Simple DB)
A simple Mongo like interface upon SQLite.

The JSDB interface provides a MONGO like interface on top of SQLite for those how looking simple (Java/Json) interface 
and embeded database.

Below follows some examples outlining the essential functionality provided by JSDB
Full examples are found in the _TestDB.java_ class. 

## Create Database
```java
public static void createDatabase(String pSqlFile) throws JSDBException
```
Create a Database file in the location specified by the parameter _pSqlFile_

## Create Collection
```java
 public JSDBCollection createCollection(String pName, JSDBKey... pKeys ) throws JSDBException
```
Creates a Collection with keys specified by the parameter _pKeys_


## Open Database
```java
 public void openDatabase( String pSqlFile) throws JSDBException
```
Open a Database in the location specified by the parameter _pSqlFile_