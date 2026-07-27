# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html
#-printusage r8-report/usage.txt


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
