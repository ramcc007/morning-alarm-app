# Add project specific ProGuard rules here.
# kotlinx.serialization needs its generated serializer classes kept.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.morningalarm.app.model.**$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.morningalarm.app.model.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.morningalarm.app.model.**$$serializer { *; }
