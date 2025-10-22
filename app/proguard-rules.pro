# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all annotations
-keepattributes *Annotation*

# Keep source file names for better crash reports
-keepattributes SourceFile,LineNumberTable

# ====== Kotlin ======
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ====== Coroutines ======
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ====== Jetpack Compose ======
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ====== Firebase ======
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase Messaging
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }

# ====== AdMob ======
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep class com.google.ads.** { *; }

# ====== Room Database ======
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep all DAO interfaces
-keep interface * extends androidx.room.Dao {
    *;
}

# Keep database classes
-keep class com.example.smart_folder_1.data.database.** { *; }
-keep class com.example.smart_folder_1.data.model.** { *; }

# ====== Data Models ======
-keep class com.example.smart_folder_1.data.model.** { *; }
-keepclassmembers class com.example.smart_folder_1.data.model.** { *; }

# ====== Coil (Image Loading) ======
-keep class coil.** { *; }
-dontwarn coil.**

# ====== Lifecycle ======
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# ====== WorkManager ======
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}
-dontwarn androidx.work.**

# ====== Serialization ======
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ====== Remove logging in release ======
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ====== Keep custom classes ======
-keep class com.example.smart_folder_1.** { *; }

# ====== Enum ======
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ====== Parcelable ======
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# ====== Serializable ======
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}