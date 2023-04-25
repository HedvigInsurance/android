# region okhttp - https://square.github.io/okhttp/features/r8_proguard/
# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# A resource is loaded with a relative path so the package of this class must be preserved.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp platform used only on JVM and when Conscrypt and other security providers are available.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# https://github.com/square/retrofit/issues/3751#issuecomment-1192043644
# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# endregion

# region okio - https://square.github.io/okio/#r8-proguard
# Animal Sniffer compileOnly dependency to ensure APIs are compatible with older versions of Java.
-dontwarn org.codehaus.mojo.animal_sniffer.*
# endregion

# arrow-integrations-retrofit-adapter - https://github.com/arrow-kt/arrow-integrations/issues/121
# `Either` class needs to exist after minification for Retrofit to know how to adapt the response to it
-keep,allowobfuscation,allowshrinking class arrow.core.Either

# Adyen dependencies - https://github.com/Adyen/adyen-android/issues/1172
-dontwarn org.slf4j.impl.StaticLoggerBinder

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
