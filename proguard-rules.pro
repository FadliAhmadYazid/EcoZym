# Minimal ProGuard rules

# Keep all app classes
-keep class com.ecozym.wastemanagement.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Suppress warnings
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**
-dontwarn kotlinx.coroutines.**

# Keep attributes
-keepattributes Signature
-keepattributes *Annotation*