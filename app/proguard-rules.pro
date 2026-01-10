# ProGuard rules for LightMarkdownReader - Optimized for size
# These rules ensure the app works correctly while allowing maximum shrinking

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
# Markwon library - OPTIMIZED (was too broad before)
# ========================================
# Only keep the core classes we actually use
-keep class io.noties.markwon.Markwon { *; }
-keep class io.noties.markwon.Markwon$Builder { *; }
-keep class io.noties.markwon.core.** { *; }
-keep interface io.noties.markwon.MarkwonPlugin { *; }

# Allow R8 to remove unused Markwon features
-dontwarn io.noties.markwon.**

# ========================================
# Kotlin - OPTIMIZED (was too broad before)
# ========================================
# Let R8 handle Kotlin optimization - remove the broad keepclassmembers rule
-dontwarn kotlinx.**

# ========================================
# Custom exceptions for proper error handling
# ========================================
-keep class com.harrisonog.lightmarkdownreader.data.*Exception { *; }

# ========================================
# Jetpack Compose - OPTIMIZED (removed broad keep rule)
# ========================================
# R8 handles Compose optimization well - we don't need to keep everything
-dontwarn androidx.compose.**

# Keep Compose compiler annotations (these are needed)
-keepattributes RuntimeVisibleAnnotations
-keep @androidx.compose.runtime.Composable class * { *; }
-keep @androidx.compose.runtime.Stable class * { *; }

# ========================================
# Android components
# ========================================
# Keep MainActivity and other Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver

# ========================================
# R8 optimization settings
# ========================================
# Allow aggressive optimization
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''
