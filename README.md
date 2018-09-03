[![Build Status](https://system-dev-jenkins.product.mttnow.com/buildStatus/icon?job=system-team-java-demo/master)](https://system-dev-jenkins.product.mttnow.com/job/system-team-java-demo/job/master/)

# system-team-java-demo

Demo of CI/CD implemented by the System Team using Spring Boot and Kotlin with Groovy/Spock tests.

The code for this application is a sample and not a real world example.
The tests are extensive and can be used as guide to write tests on your applications.

Most important things on this demo is configuration of Gradle and the Jenkins pipeline file. 

## Using Kotlin
Several points of note:

### Kotlin and the Java standard library
You must have a dependency upon the Java standard library within the build configuration:
```gradle
...
dependencies {
   compile "org.jetbrains.kotlin:kotlin-stdlib"
   ...
}
``` 

By default, Kotlin targets the Java 1.6 JVM and language runtime. _This should still work, even where you use a higher Java version to build your project_. 

However, there are speed increases and additional functionality enabled by Kotlin if configured to target the Java 8 runtime. To do this, replace the dependency with '`compile "org.jetbrains.kotlin:kotlin-stdlib"`'.' 

If you use a higher JVM version again, the Java 8 standard library _should_ work, no guarantees though. 

### Using kotlin with libraries extending your classes (e.g. Spring!)
Unless explicitly specified, classes will be non-extendable within kotlin. As Spring relies on extending classes, in general, you will get errors on running the application stating something about non-final classes.

This point actually applies to any framework relying on extending your code, but this section will only detail Spring.

You can change your beans and classes by adding `open` to the definition of ALL classes:

```kotlin
@Service
class SomeService {
   //Some Spring service
} 

//Above needs to change to
@Service
open class SomeService {
   //Some Spring Service
}
```

This applies across ALL services, components and controllers in your application.

_Alternatively_, without re-visiting all classes in your app, you can add the `all-open` gradle plugin to your build:

```gradle
plugins {
  id "org.jetbrains.kotlin.jvm" version "1.2.61"
  
  //If using spring boot, you can just add the spring plugin, you get all-open by default then
  id "org.jetbrains.kotlin.plugin.spring" version "1.2.61"  //Versions must be the same
  
  //If your using something else (not spring) that relies on extending your classes, use all-open explicitly
  id "org.jetbrains.kotlin.plugin.allopen" version "1.2.61" //Use spring OR this, not both!
}
```

### The Spring boot `Application` class
OK, this is a strange one.

The Java Application class (defined as follows)
```Java
@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
```

If you con ert this using the IntelliJ Kotlin converter, you'll end up with this
```kotlin
@SpringBootApplication
object Application {
  @JvmStatic
  fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
  }
}
```

THIS DOES NOT WORK. You will receive an error stating that classes annotated with `@Configuration` cannot be final. This means you CANNOT use the `object` instantiation of the class.

Instead, you need to create an `Application` class that Spring _can_ use, and define the 'main' method outside the class

```kotlin
@SpringBootApplication
open class Application      //You can leave off the 'open' if using the all-open or spring plugins!

fun main(args: Array<String>) {
  SpringApplication.run(Application::class.java, *args)
}
```

### Using kotlin with Spock tests
You need to make sure **both** the kotlin plugin AND the groovy plugin applied in your Gradle build file.

Spock tests tend to make extensive use of named argument constructors for generation of test data
```kotlin
//The data class
data class Student(var name: String = "", var age: Int = 0)

//In test:
def "some test"() {
  ...
  Student testStudent = new Student(age: 21)
  ...
}
```

While named arguments _will_ work from Java or Kotlin classes, they will not run from Groovy generated code. The example above should change to 
```kotlin
//Same data class!

def "some test"() {
  ...
  Student testStudent = new Student("", 21)
  ...
}
```  

### Data class annotation
The Kotlin standard for data class definition is as follows:
```kotlin
data class blah(val fieldName: String)
```

Any annotations on the fields of a data class will be applied within the constructor only. This means that applying an annotation that will be applied on a continuous basis (validation for example) must be applied on the field, rather that the constructor

```kotlin
//Wrong
data class Blah(@NotEmpty var fieldName: String)

//Instead do this:
data class Blah2(@field:NotEmpty var fieldName: String) //The validation will be applied wherever the validator is called now

//Controller
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun addPerson(@Valid @RequestBody blah: Blah) {
    //An empty fieldName would be accepted by this endpoint, as the validation occurs after instantiation of the object 
  }
  
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun addPerson(@Valid @RequestBody blah: Blah2) {
    //This endpoint would reject a POST with the 'fieldName' being empty as validaton applies on the field for Blah2 
  }
   
``` 

### JSON parsing of data classes
Generally, when you have POST endpoints, you receive data as JSON payloads. This means that a JSON parser is used, either explicitly or implicitly, to parse the invoming JSON and generate the classes. In _most_ cases, this is FasterXML (aka Jackson) mapping.

From Spring, this means that _at runtime_ you will see an error `Unexpected exception during bean creation; nested exception is java.lang.NoClassDefFoundError: kotlin/reflect/jvm/ReflectJvmMapping`

In this case, you need to enable the kotlin/fasterxml module:
```gradle
...
dependencies {
   ...
   compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.2")
   ...
}
```

## Changelog

All changes can be found [here](/CHANGELOG.md)

## Support

Any questions or issues should be directed to the System TIE team.
