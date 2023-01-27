# okio and okhttp
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform

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

# Facebook Yoga
# Odyssey components are using facebook yoga, and the components are not called from application
# code (they are part of a SDUI library). R8 will then try to optimise by removing classes from
# yoga, since those does not seem to be called from anywhere.
-keep class com.facebook.** { *; }

# Odyssey classes
# Due to serialization and getting class names we need to keep these classes.
-keep class com.hedvig.odyssey.** { *; }

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
