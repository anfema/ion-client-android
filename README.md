# ion-client-android

## Setup

### Integration

1. Add this as a submodule to your Git repository with local path 'ionclient'. Folder 'ionclient' must be in the same
   directory as your app module. (If your code is not in a Git repository clone this repository into folder '
   ionclient'.)
   Carefully select a branch:
    - develop: Contains the latest version of ION
    - develop-old-ion-structure: Older version before breaking API changes, might contain some recent bugfixes
1. In settings.gradle: `include ':ionclient'`
1. In build.gradle of your app module add module dependency

```
implementation project(':ionclient')
``` 

TBD: Make ION client available as a dependency.

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
