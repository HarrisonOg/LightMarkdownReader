# ProGuard rules for LightMarkdownReader
# These rules ensure the app works correctly in release builds with R8 optimization

# Keep line numbers for debugging crashes in production
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========================================
# Gson (for RecentFilesRepository JSON serialization)
# ========================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep data model classes used with Gson
-keep class com.harrisonog.lightmarkdownreader.data.RecentFile { *; }
-keep class com.harrisonog.lightmarkdownreader.data.MarkdownFile { *; }

# Generic Gson rules
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# ========================================
# Markwon library (markdown rendering)
# ========================================
-keep class io.noties.markwon.** { *; }
-keep interface io.noties.markwon.** { *; }
-dontwarn io.noties.markwon.**

# ========================================
# Kotlin
# ========================================
-keepclassmembers class kotlinx.** { *; }
-dontwarn kotlinx.**

# ========================================
# Custom exceptions for proper error handling
# ========================================
-keep class com.harrisonog.lightmarkdownreader.data.*Exception { *; }

# ========================================
# Jetpack Compose
# ========================================
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# Keep Compose compiler annotations
-keepattributes RuntimeVisibleAnnotations
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Stable class * { *; }

# ========================================
# Android components
# ========================================
# Keep all Activities, Services, and BroadcastReceivers
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# ========================================
# R8 optimization settings
# ========================================
# Allow aggressive optimization while preserving functionality
-optimizationpasses 5
-allowaccessmodification