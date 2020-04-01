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

# Gson
-keep class com.hedvig.app.feature.chat.dto.** { *; }

-keep class com.hedvig.app.react.data.** { *; }
-keepnames class com.hedvig.app.react.data.** { *; }

# Adyen
-keep class com.adyen.checkout.base.model.** { *; }
-keep class com.adyen.threeds2.** { *; }
-keepclassmembers public class * implements com.adyen.checkout.base.PaymentComponent {
   public <init>(...);
}
