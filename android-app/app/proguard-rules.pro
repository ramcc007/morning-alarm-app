# Add project specific ProGuard rules here.
# kotlinx.serialization needs its generated serializer classes kept.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class com.wakerep.app.model.**$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class com.wakerep.app.model.** {
    *** Companion;
}
-keep,includedescriptorclasses class com.wakerep.app.model.**$$serializer { *; }
