# *** Retrolambda ***

-dontwarn java.lang.invoke.*

# *** Gson ***

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Gson specific classes
-keep class sun.misc.Unsafe { *; }

# ION client specific classes that will be serialized/deserialized over Gson
-keep class com.anfema.ionclient.**.models.** { *; }
-keep class * extends com.anfema.ionclient.**.CacheIndex { *; }


# *** OkHttp3 ***

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**


# *** Retrofit2 ****

-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
# If your rest service methods throw custom exceptions, because you've defined an ErrorHandler.
-keepattributes Signature
# If in your rest service interface you use methods with Callback argument.
-keepattributes Exceptions
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**


# *** Picasso ***

-dontwarn com.squareup.okhttp.**


# *** Joda Time 2.3 ***

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }
-dontwarn org.joda.convert.**

# *** Apache Commons Compress 1.12 ***

-dontwarn org.apache.**

###### Rules of picasso snapshot

### PICASSO

# Checks for OkHttp versions on the classpath to determine Downloader to use.
-dontnote com.squareup.picasso.Utils
# Downloader used only when OkHttp 2.x is present on the classpath.
-dontwarn com.squareup.picasso.OkHttpDownloader
# Downloader used only when OkHttp 3.x is present on the classpath.
-dontwarn com.squareup.picasso.OkHttp3Downloader


### OKHTTP

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote okhttp3.internal.Platform
-dontnote com.squareup.okhttp.internal.Platform


### OKIO

# java.nio.file.* usage which cannot be used at runtime. Animal sniffer annotation.
-dontwarn okio.Okio
# JDK 7-only method which is @hide on Android. Animal sniffer annotation.
-dontwarn okio.DeflaterSink
