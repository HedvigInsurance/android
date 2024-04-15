# arrow-integrations-retrofit-adapter - https://github.com/arrow-kt/arrow-integrations/issues/121
# `Either` class needs to exist after minification for Retrofit to know how to adapt the response to it
-keep,allowobfuscation,allowshrinking class arrow.core.Either

# Unleash
-keep public class io.getunleash.** {*;}
-keep class com.fasterxml.** {*;}
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

# Crashlytics https://firebase.google.com/docs/crashlytics/get-deobfuscated-reports?platform=android#config-r8-proguard-dexguard
-keepattributes SourceFile,LineNumberTable        # Keep file names and line numbers.

# Datastore - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore-preferences/proguard-rules.pro;l=1
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}
