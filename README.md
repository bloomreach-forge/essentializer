# Essentializer

Hippo essentials plugin which can be used to package existing functionality (components, templates, files etc.) 
as an essentials plugin


## Installation

Add Hippo repositories to your project root pom.xml file: 

```
    <repository>
      <id>hippo-maven2-forge</id>
      <name>Hippo Maven 2 Forge</name>
      <url>http://maven.onehippo.com/maven2-forge</url>
    </repository>
  

```


Add following dependency to essentials/pom.xml


```xml
    <dependency>
      <groupId>org.onehippo.cms7</groupId>
      <artifactId>hippo-essentials-plugin-essentializer</artifactId>
      <version>1.0.2</version>
    </dependency>

```


Go to Essentials > Tools > Essentializer:

http://localhost:8080/essentials/#/tools/essentializer

Screenshot: 

![Essentializer](/screen.png)

