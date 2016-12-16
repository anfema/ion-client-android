# ion-client-android

## Setup

### Integration

1. Add this as a submodule to your Git repository with local path 'ionclient'. Folder 'ionclient' must be in the same directory as your app module. (If your code is not in a Git repository clone this repository into folder 'ionclient'.)
1. In settings.gradle: `include ':ionclient'`
1. In build.gradle of your app module add module dependency 
```
compile project(':ionclient')
``` 

TBD: Make ION client available as a dependency.

### General

Overwrite string resource "file_provider_authority" with a value that contains the application ID.

There are two possibilities to guarantee this:

- define string resource in an XML file:
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="file_provider_authority">{YOUR_APPLICATION_ID}.fileprovider</string>
</resources>
```
If you want to be able to install multiple variants of your app at the same time on a device you need to define the differing values of "file_provider_authority" for each variant.
The following approach automatically takes care of this.

- define string resource in build.gradle of app module: 
```
    applicationVariants.all { variant ->
        variant.resValue 'string', 'file_provider_authority', variant.applicationId + '.fileprovider'
    }
```

Explanation: The ION client internally uses a file provider in order to pass PDFs to a PDF viewer app. File provider authorities must be unique among apps - i.e. a unique authority should be defined by each application ID.
However, there are technical limitations: Application ID can neither be accessed in resource XMLs nor programmatically - only in the build.gradle file of the app module (and in the Manifest).
Hence, the app module must take responsibility to avoid conflicts.  

### Optional: RetroLambda

Lambda expressions shorten code (esp. Listeners with one method). Lambda expressions are natively supported in Java since version 1.8. Android only supports Java not newer than 1.7.

To be able to use Lambda expressions anyway, we use RetroLambda. See tutorial: https://github.com/evant/gradle-retrolambda)

Prerequisite is to make Java 8 available with these two steps:

- On Mac: To check if Java 8 is already installed or to find out path after installation, run: /usr/libexec/java_home -v 1.8
- Download Java 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Set environment variable JAVA8_HOME pointing to installation path of Java 8 'Home' folder (Mac tutorial: http://stackoverflow.com/questions/135688/setting-environment-variables-in-os-x)

EVERY MODULE(!) which wants to use lambda expressions needs the following in the build.gradle:

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


## Usage

The recommended way to access the ION client is to create an adapter class (e.g. "Ion.java") to obtain an instance of IonClient with a concise syntax. The adapter class takes responsibility for creating an instance of IonConfig.

Here is a simple example:

- Ion.java
```
public class Ion
{
	public static IonClient client( Context context )
	{
		IonConfig ionConfig = new IonConfig(context.getString(R.string.base_url),
            context.getString(R.string.collection_identifier),
            context.getString(R.string.locale),
            BasicAuth.getAuthHeaderValue(context.getString(R.string.username), context.getString(R.string.password)),
            false,
            false);
        return IonClient.getInstance( ionConfig, context );
	}
```
- resource XML file
```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="base_url">https://example.com/client/v1/</string>
    <string name="collection_identifier">my_example_collection</string>
    <string name="locale">en_US</string>
    <string name="variation">default</string>
    <string name="username">my_username_</string>
    <string name="password">my_password_</string>
</resources>
```

 The example is using a single collection, locale, and variation. Automatic archive and FTS database downloads are disabled. For authentication Basic Auth is used.