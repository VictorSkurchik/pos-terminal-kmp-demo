# R8 / ProGuard rules for the release build.

# kotlinx.serialization — keep @Serializable metadata and generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
-if @kotlinx.serialization.Serializable class **
-keep class <1> { *; }

# Ktor client (OkHttp engine) reflection.
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn org.slf4j.**
-dontwarn io.ktor.**

# Room-generated implementations.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class *

# Koin.
-keep class org.koin.** { *; }

# Keep DTO/domain model class names used by serialization.
-keep class by.vsdev.posterminal.demo.dto.** { *; }
-keep class by.vsdev.posterminal.demo.model.** { *; }
