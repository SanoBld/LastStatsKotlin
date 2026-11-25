-keepattributes *Annotation*, InnerClasses, Signature, RuntimeVisibleAnnotations
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.sanobld.laststats.**$$serializer { *; }
-keepclassmembers class com.sanobld.laststats.** {
    *** Companion;
}
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
