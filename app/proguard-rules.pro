# kotlinx.serialization — @Serializable 모델의 serializer 유지 (릴리스 조용한 파싱 실패 방지)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.dave.soul.exchange_app.**$$serializer { *; }
-keepclassmembers class com.dave.soul.exchange_app.** { *** Companion; }
-keepclasseswithmembers class com.dave.soul.exchange_app.** { kotlinx.serialization.KSerializer serializer(...); }

# Retrofit — AGP 8 은 R8 full mode 기본. retrofit 2.9 consumer rules 로는 부족해
# 인터페이스/Continuation/Response 제네릭이 축소되면 릴리스에서만 조용히 호출이 깨진다.
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation,allowshrinking class <1>
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
