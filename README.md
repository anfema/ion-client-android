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

The main entry point is the class `IonClient`. Create one or multiple instance of `IonClient` (the latter can be done by
using the `copy` constructor on an already created instance). Try to re-use/share the `OkHttpClient` instance to use a
common thread pool for network calls.

`IonClient` requires an instance of `CollectionProperties`, which contains the essential configuration data.

## Migration guide

### v2 -> v3

`IonClient.loadImage()` was removed. This call used `Picasso` under the hood. Replace these calls with an image loading
library of your choice (e.g. `Coil` or `Glide`) and configure that library by setting a custom `OkHttpClient` instance, 
which is best obtained by calling `OkHttpClient.withIonFileCache()`.

### v3 -> v4

- Replace `BasicAuth.getAuthHeaderValue(username, password)` with `Credentials.basic(username, password)`
- `IonClient.getInstance()` was removed, use the constructor. If multiple instances are created (which is the case for Bayern International) you might want to remember the different instances in your app.
- Log level ist set via `IonLog.logLevel` (before: `IonConfig.logLevel`)
- `IonConfig`: `authorization()`, `additionalHeaders()`, and `networkTimeout()` were removed.
   - Headers (incl. Authorization Header) must be defined in the `OkHttpClient` instance passed to the `IonClient` constructor.
   - There are okhttp helper classes in the androidkit (module: jvm-core)
- `IonConfig` renamed to `CollectionProperties`
