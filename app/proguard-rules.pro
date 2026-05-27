# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Keep Room generated classes
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase

# Keep Glance widget state @Serializable classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.daycounter.**$$serializer { *; }
-keepclassmembers class com.daycounter.** { *** Companion; }
-keepclasseswithmembers class com.daycounter.** { kotlinx.serialization.KSerializer serializer(...); }

# Keep entities for Room reflection
-keep class com.daycounter.data.database.entity.** { *; }

# Strip release logging (constitution Principle VI — no PII in release logs)
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
