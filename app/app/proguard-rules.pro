# arrow-integrations-retrofit-adapter - https://github.com/arrow-kt/arrow-integrations/issues/121
# `Either` class needs to exist after minification for Retrofit to know how to adapt the response to it
-keep,allowobfuscation,allowshrinking class arrow.core.Either

# Unleash
-keep public class io.getunleash.** {*;}
-keep class com.fasterxml.** {*;}
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient

# Crashlytics
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-printmapping mapping.txt

# Adyen
-keep class com.adyen.checkout.core.model.** { * ;}
-keep class com.adyen.checkout.components.model.** { *; }
-keep class com.adyen.threeds2.** { *; }
-keepclassmembers public class * implements com.adyen.checkout.components.PaymentComponent {
   public <init>(...);
}
-keepclassmembers public class * implements com.adyen.checkout.components.ActionComponent {
   public <init>(...);
}
# More Adyen - https://github.com/Adyen/adyen-android/issues/1172
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `INSTANCE.serializer()` of serializable objects.
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime for polymorphic serialization.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Datastore - https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:datastore/datastore-preferences/proguard-rules.pro;l=1
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}