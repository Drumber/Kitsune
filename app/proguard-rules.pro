# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#-printusage r8-report/usage.txt

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate

# General
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*,EnclosingMethod,Exceptions,InnerClasses

# Kotlin reflection
-keep class kotlin.Metadata { *; }

# Slf4j
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder

# Jackson
-keepnames class com.fasterxml.jackson.** { *; }
-keepclassmembers class * {
     @com.fasterxml.jackson.annotation.* *;
}
-dontwarn com.fasterxml.jackson.databind.**

# jsonapi-converter
-keepclassmembers class * {
    @com.github.jasminb.jsonapi.annotations.* *;
}
-keep class * implements com.github.jasminb.jsonapi.ResourceIdHandler

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

############################################
# Kitsune specific rules
############################################

# keep all classes
-keep class io.github.drumber.kitsune.** { *; }

# keep search filters
-keep class io.github.drumber.kitsune.domain.algolia.FilterCollectionEntry** { *; }
-keep class com.algolia.instantsearch.filter.state.FilterGroupID** { *; }
-keep class com.algolia.instantsearch.filter.state.Filters** { *; }
-keep class com.algolia.search.model.filter.Filter** { *; }
-keep class com.algolia.search.model.Attribute** { *; }
