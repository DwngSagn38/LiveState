# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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
# Giữ lại model dùng trong Gson
# Gson
-keep class com.example.livestate.model.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken
-keepattributes Signature

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Lottie
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ViewBinding (optional, for safety)
-keepclassmembers class * extends androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
}

# Your base classes
-keep class com.example.livestate.base.** { *; }

# Retrofit (just in case)
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keepattributes Signature

