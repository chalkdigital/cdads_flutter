# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/arungupta/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-printmapping out.map
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,Signature,InnerClasses,Deprecated,SourceFile,LineNumberTable,EnclosingMethod

# Preserve all annotations.

-keepattributes *Annotation*

# Preserve all public classes, and their public and protected fields and
# methods.

# -keep public class * {
#     public protected *;
# }

# Preserve all .class method names.

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Ignoring all of the external "org" libraries
# (for example org.apache & org.jackson)
-keep class org.** { *; }
-dontwarn org.**

-keep public class com.google.** { *; }
-dontwarn com.google.**

-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}

-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }

-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Preserve all native method names and the names of their classes.

 -keepclasseswithmembernames class * {
     native <methods>;
 }

# Preserve the special static methods that are required in all enumeration
# classes.

 -keepclassmembers class * extends java.lang.Enum {
 <fields>;
     public static **[] values();
     public static ** valueOf(java.lang.String);
 }

# Explicitly preserve all serialization members. The Serializable interface
# is only a marker interface, so it wouldn't save them.
# You can comment this out if your library doesn't use serialization.
# If your code contains serializable classes that have to be backward
# compatible, please refer to the manual.

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Your library may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

-keep class com.chalkdigital.banner.ads.HtmlBanner {
    public *;
}

-keep class com.chalkdigital.banner.mraid.MraidBanner {
    public *;
}
-keep class com.chalkdigital.interstitial.mraid.MraidInterstitial {
 public *;
      }
-keep class com.chalkdigital.interstitial.mraid.SparkInterstitial {
 public *;
 }
-keep class com.chalkdigital.interstitial.ads.HtmlInterstitial {
    public *;
}
-keep public class com.chalkdigital.interstitial.ads.CDAdInterstitial {
    public *;
}
-keep interface com.chalkdigital.interstitial.ads.CDAdInterstitial$InterstitialAdListener {*;}

-keep public class com.chalkdigital.interstitial.ads.CDAdVideoInterstitial {
    public *;
}
-keep interface com.chalkdigital.interstitial.ads.CDAdVideoInterstitial$InterstitialVideoAdListener {*;}
-keep public class com.chalkdigital.ads.CDAdView {
    public *;
}
-keep public class com.chalkdigital.common.CDAdRequest {
    public *;
}

-keep class com.chalkdigital.common.CDAdRequest$Builder {
    *;
}

-keep interface com.chalkdigital.ads.CDAdView$CDAdViewListener {
    *;
}

-keep public class com.chalkdigital.common.CDAdsUtils{
    public *;
}

-keep class * extends com.chalkdigital.banner.ads.CustomEventBanner {}
-keep class * extends com.chalkdigital.interstitial.ads.CustomEventInterstitial {}
-keep class * extends com.chalkdigital.nativeads.CustomEventNative {
    public protected *;
}

-keep class com.chalkdigital.nativeads.CDAdCustomEventVideoNative {
    public protected *;
}

-keep class com.chalkdigital.nativeads.CDAdCustomEventVideoNative$* {
    *;
}

-keepclassmembers class * { @com.chalkdigital.common.util.ReflectionTarget <methods>; }
-keep class com.chalkdigital.ads.factories.* {
   public protected *;
}
-keep class * extends com.chalkdigital.nativeads.CDAdAdRenderer {
    public protected *;
}

-keep class com.chalkdigital.nativeads.NativeAd {
   public protected *;
}

-keep class com.chalkdigital.nativeads.NativeErrorCode {
   public *;
}

-keep class com.chalkdigital.nativeads.VideoConfiguration {
   public *;
}

-keep class com.chalkdigital.nativeads.MediaViewBinder {
   public protected *;
}

-keep class com.chalkdigital.nativeads.MediaViewBinder$Builder {
    *;
}

-keep class com.chalkdigital.nativeads.factories.* {
   public protected *;
}

-keep class com.chalkdigital.nativeads.* {
   public protected *;
}

-keep class com.chalkdigital.nativeads.CDAdNative {
   public protected *;
}

-keep interface com.chalkdigital.nativeads.CDAdNative$CDAdNativeAdListener {
   *;
}

-keep class com.chalkdigital.nativeads.VideoConfiguration$Builder {
   *;
}

-keep class com.chalkdigital.banner.ads.factories.* {
   public protected *;
}
-keepclassmembers class * extends com.chalkdigital.banner.ads.CustomEventBanner$CustomEventBannerListener {
   void loadAd();
   void invalidate();
}
-keep class com.chalkdigital.interstitial.ads.factories.* {
   public protected *;
}
-keep class com.chalkdigital.common.factories.* {
   public protected *;
}
-keep class com.chalkdigital.analytics.mobile.* {
   public *;
}

-keep public class com.chalkdigital.common.CDAdGeoInfo {
  public protected *;
}

-keep public class com.chalkdigital.common.CDAdSize{
    public protected *;
}

-keep public enum com.chalkdigital.common.CDAdSize$CDAdSizeConstant{
    public protected *;
}


-keep public class com.chalkdigital.ads.CDAdErrorCode{
    *;
}
-keepclassmembers public class com.chalkdigital.network.response.* {
	private <fields>;
}
-keep public class com.chalkdigital.network.retrofit.service.CDAdApi{
    *;
}


# keep anything annotated with @Expose
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
# Also keep classes that @Expose everything
-keep @android.support.annotation.Keep class *

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in sdk.dir/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:
#When not preverifing in a case-insensitive filing system, such as Windows. This tool will unpack your processed jars,(if using windows you should then use):
-dontusemixedcaseclassnames

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#Specifies not to ignore non-public library classes. As of version 4.5, this is the default setting
-dontskipnonpubliclibraryclasses

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
#-dontoptimize
-dontpreverify
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-overloadaggressively
-verbose

-ignorewarnings

-dontwarn com.switch_smile.**
#-dontwarn com.facebook.**
#-dontwarn com.twitter.**
#-dontwarn com.winterwell.**
#-dontwarn winterwell.**
#-dontwarn oauth.signpost.**
-dontwarn android.support.**
-dontwarn java.lang.Class
#-dontwarn com.parse.FacebookAuthenticationProvider
#-dontwarn bolts.**
#-dontwarn com.google.android.gms.**
-dontwarn com.google.gson.**
-dontwarn com.android.volley.**
-dontwarn android.net.http.**
-dontwarn org.apache.http.**
-dontwarn org.joda.time.**
-dontwarn org.eclipse.paho.**
-dontwarn com.parse.**
-dontwarn io.fabric.**
-dontwarn com.squareup.picasso.**
#-dontwarn twitter4j.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp3.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
-dontwarn com.bumptech.glide.**

#Specifies to write out some more information during processing. If the program terminates with an exception, this option will print out the entire stack trace, instead of just the exception message.
-verbose

#The -optimizations option disables some arithmetic simplifications that Dalvik 1.0 and 1.5 can't handle. Note that the Dalvik VM also can't handle aggressive overloading (of static fields).
#To understand or change this check http://proguard.sourceforge.net/index.html#/manual/optimizations.html
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#Use 5 step of optimization
#-optimizationpasses 5

# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.

#To repackage classes on a single package
#-repackageclasses ''

#Uncomment if using annotations to keep them.
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature

#Keep the R
-keepclassmembers class **.R$* {
    public static <fields>;
}

###### ADDITIONAL OPTIONS NOT USED NORMALLY

#To keep callback calls. Uncomment if using any
#http://proguard.sourceforge.net/index.html#/manual/examples.html#callback
#-keep class mypackage.MyCallbackClass {
#   void myCallbackMethod(java.lang.String);
#}

#Uncomment if using Serializable
-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#Keep classes that are referenced on the AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends android.support.v7.app.AppCompatActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
#Compatibility library
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v7.app.Fragment
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.DialogFragment
-keep public class * extends android.support.v4.app.DialogFragment

#-keeppackagenames com.facebook.android,com.twitter.android,com.winterwell.jgeoplanet,winterwell.json,winterwell

-keep class javax.** { *; }
-keep class org.** { *; }
-keep class com.google.**{*;}
#-keep class com.facebook.** { *; }
#-keep class com.bolts.**{*;}
-keep class com.crashlytics.**{*;}
-keep class com.squareup.**{*;}
-keep class com.bumptech.glide.**{*;}
#-keep class com.nostra13.**{*;}
#-keep class com.daimajia.**{*;}

#-keep class com.twitter.custom.** { *; }
#-keep class com.winterwell.jgeoplanet.** { *; }
#-keep class winterwell.json.** { *; }
#-keep class winterwell.jtwitter.** { *; }
-keep class twitter4j.** { *; }
-keep class io.fabric.** { *; }
#-keep class oauth.signpost.** { *; }
-keep class android.support.**{*;}
#-keep class bolts.**{*;}
-keep class retrofit2.converter.gson.**{*;}
-keep class com.google.gson.**{*;}
-keep class okio.**{*;}
-keep class retrofit.**{*;}
-keep class com.android.volley.**{*;}
-keep class org.eclipse.paho.**{*;}

-keepattributes SourceFile,LineNumberTable
-keep class com.parse.*{ *; }

# Fabric Proguard Config
-keep class com.google.android.gms.common.GooglePlayServicesUtil {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {*;}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {*;}

-keep public class com.google.android.gms.ads.identifier.** {
    public protected *;
}

-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.* { *; }

#To maintain custom components names that are used on layouts XML.
#Uncomment if having any problem with the approach below
#-keep public class custom.components.package.and.name.**

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
 -keepclassmembers public class * extends android.view.View {
  void set*(***);
  *** get*();
}

#To remove debug logs:
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** w(...);
    public static *** i(...);
    public static *** e(...);
}

#To avoid changing names of methods invoked on layout's onClick.
# Uncomment and add specific method names if using onClick on layouts
#-keepclassmembers class * {
# public void onClickButton(android.view.View);
#}

#Maintain java native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

#To maintain custom components names that are used on layouts XML:
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keep public class * extends android.view.View {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep public class * extends android.view.ViewGroup{*;}

#Maintain enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#To keep parcelable classes (to serialize - deserialize objects to sent through Intents)
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Preserve all public classes, and their public and protected fields and
# methods.
-keep public class * {
    public protected *;
}

# Preserve all .class method names.
-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

# Preserve the special static methods that are required in all enumeration
# classes.
-keepclassmembers class * extends java.lang.Enum {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-keep class com.switch_smile.ssbp.core.**{*;}

#-keep class com.switch_smile.ssbp.core.ads.**{*;}
#-keep class com.switch_smile.ssbp.core.db.**{*;}
#-keep class com.switch_smile.ssbp.core.event.**{*;}
#-keep interface com.switch_smile.ssbp.core.event.SSBPEventListener{*;}
#-keep interface com.switch_smile.ssbp.core.listener.* { *; }
#-keep class com.switch_smile.ssbp.core.model.**{*;}
#-keep class com.switch_smile.ssbp.core.request.**{*;}
#-keep class com.switch_smile.ssbp.core.respone.**{*;}
#-keep class com.switch_smile.ssbp.core.scan.**{*;}
#-keep class com.switch_smile.ssbp.core.LogUtil{*;}
#-keep class com.switch_smile.ssbp.core.SSBPCommon{*;}
#-keep class com.switch_smile.ssbp.core.SSBPDateTime{*;}
#-keep class com.switch_smile.ssbp.core.SSBPNetUtility{*;}
#-keep class com.switch_smile.ssbp.core.SSBPOfferIF{*;}
#-keep class com.switch_smile.ssbp.core.SSBPUtility{*;}

-keep class com.switch_smile.ssbp.**{*;}
-keep class com.chalkdigital.mediation.**{*;}

#-keep class com.switch_smile.ssbp.lite.SSBPSdkIF.SSBPSdkIFListener{*;}