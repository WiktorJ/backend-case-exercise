# backend-case-exercise
Run with: 

```$xslt
mvn clean install
java -jar target/backend-case-exercise-1.0-SNAPSHOT-jar-with-dependencies.jar
java -jar target/backend-case-exercise-1.0-SNAPSHOT-jar-with-dependencies.jar -i ./examples/small-log.txt -o traces.txt
```

In pom Java 10 is set as source and target, however project will also run and compile with java 9 and 8.

Change this properties in pom.xml
```$xslt
        <maven.compiler.source>1.10</maven.compiler.source>
        <maven.compiler.target>1.10</maven.compiler.target>
```
To appropriate version if java 10 is not available.