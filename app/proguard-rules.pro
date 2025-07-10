# ==========================
# ProGuard Rules for LiveState Weather App
# ==========================

# ======= Gson models: Chống lỗi parse JSON =======
-keep class com.example.livestate.model.** { *; }
-keep class com.example.livestate.entity.** { *; }
-keep class com.example.livestate.data.** { *; }
-keep class com.example.myapplication.model.** { *; }

# Giữ riêng các class quan trọng (dự phòng)
-keep class com.example.myapplication.model.WeatherResponse { *; }
-keep class com.example.livestate.model.WeatherDay { *; }

# Giữ tất cả field có @SerializedName để tránh mất khi minify
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepattributes *Annotation*
-keepattributes Signature

# ======= Retrofit ==========
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# ======= OkHttp ==========
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

# ======= Kotlin reflection (Gson cần nếu dùng data class) =======
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# ======= AndroidX & ViewBinding =======
-keep class androidx.** { *; }
-dontwarn androidx.**

-keepclassmembers class * extends androidx.viewbinding.ViewBinding {
    public static *** inflate(...);
}

# ======= Nếu dùng Glide hoặc Coil =======
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# ======= Firebase (nếu có sử dụng) =======
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ======= Adapter & ViewHolder =======
-keepclassmembers class *Adapter$*ViewHolder { *; }
-keepclassmembers class **.adapter.** { *; }
-keepclassmembers class **.viewholder.** { *; }
-keepclassmembers class com.example.livestate.ui.weather_activity.WeatherDayAdapter$WeatherDayViewHolder { *; }

# ======= Nếu dùng Lottie (hoạt hình) =======
-keep class com.airbnb.lottie.** { *; }
-dontwarn com.airbnb.lottie.**

# ======= Coroutines (nếu có dùng) =======
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# ======= Giữ base class & service =======
-keep class com.example.livestate.base.** { *; }
-keep class com.example.livestate.service.** { *; }

# ======= Giữ debug info (tuỳ chọn, giúp log rõ hơn) =======
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ======= Annotation giữ lại =======
-keepattributes *Annotation*
