# *** Retrolambda ***

-dontwarn java.lang.invoke.*


# *** RxJava 1.0.14 ***

#-keepclassmembers class rx.internal.util.unsafe.* {
#   long producerIndex;
#   long consumerIndex;
#}
-keep class rx.** { *; }
#-dontwarn rx.**

# *** Gson ***

# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Gson specific classes
-keep class sun.misc.Unsafe { *; }

# AMP client specific classes that will be serialized/deserialized over Gson
-keep class com.anfema.ampclient.*.models.** { *; }
-keep class * extends com.anfema.ampclient.caching.CacheIndex


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


# *** Guava ***

# Configuration for Guava 18.0
# disagrees with instructions provided by Guava project: https://code.google.com/p/guava-libraries/wiki/UsingProGuardWithGuava

-keep class com.google.common.io.Resources {
    public static <methods>;
}
-keep class com.google.common.collect.Lists {
    public static ** reverse(**);
}
-keep class com.google.common.base.Charsets {
    public static <fields>;
}

-keep class com.google.common.base.Joiner {
    public static com.google.common.base.Joiner on(java.lang.String);
    public ** join(...);
}

-keep class com.google.common.collect.MapMakerInternalMap$ReferenceEntry
-keep class com.google.common.cache.LocalCache$ReferenceEntry

# http://stackoverflow.com/questions/9120338/proguard-configuration-for-guava-with-obfuscation-and-optimization
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe

# Guava 19.0
-dontwarn java.lang.ClassValue
-dontwarn com.google.j2objc.annotations.Weak
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# tests
#-keep class com.squareup.okhttp.** { *; }
#-keep interface com.squareup.okhttp.** { *; }
#-keep class com.google.gson.stream.** { *; }
#-keep class org.apache.** { *; }
