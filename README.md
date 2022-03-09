Spring Batch Demo
=================


To Build:
```
mvn package
```

To run:
* Set up your databases
* Execute
```
java -jar target/batch-processing-complete-0.0.1-SNAPSHOT.jar --spring.target-db.username=target_user --spring.target-db.password=yourpassword --spring.source-db.username=source_user --spring.source-db.password=yourpassword
```
