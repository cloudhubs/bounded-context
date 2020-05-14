# How to install ws4j

1. [Download the Jar](https://code.google.com/archive/p/ws4j/downloads)
2. Add the jar to your local mvn repository

```
 mvn install:install-file -Dfile=<path to file> -DgroupId=com.sciss -DartifactId=ws4j -Dversion=1.0.1 -Dpackaging=jar 
```

# API usage

```java
BoundedContext boundedContext = new BoundedContextApiImpl().getBoundedContext(systemContext, useWuPalmer);
```
