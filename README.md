# amp-client-android

Integrate this as a module for your Android project (preferrably as submodule of your current repository).

## Setup

### RetroLambda

Lambda expressions shorten code (esp. Listeners with one method). Lambda expressions are natively supported in Java since version 1.8. Android only supports Java not newer than 1.7.

To be able to use Lambda expressions anyway, we use RetroLambda. See tutorial: https://github.com/evant/gradle-retrolambda)

Prerequesite is to make Java 8 available with these two steps:
- On Mac: To check if Java 8 is already installed or to find out path after installation, run: /usr/libexec/java_home -v 1.8
- Download Java 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Set environment variable JAVA8_HOME pointing to installation path of Java 8 'Home' folder (Mac tutorial: http://stackoverflow.com/questions/135688/setting-environment-variables-in-os-x)

Every module (probably the app module) using lambda expressions need the following in the build.gradle:

```
apply plugin: 'com.android.application' // or apply plugin: 'com.android.library'
apply plugin: 'me.tatarka.retrolambda' // apply last because it might interfere with android-apt commands

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath 'me.tatarka:gradle-retrolambda:3.2.3' // activate lambda expressions (on maven central)
        classpath 'me.tatarka.retrolambda.projectlombok:lombok.ast:0.2.3.a2' // get of lint errors due to Java 1.8
    }

    // Exclude the version that the android plugin depends on.
    configurations.classpath.exclude group: 'com.android.tools.external.lombok'
}

android {
    ...

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```
