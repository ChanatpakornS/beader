# Add project-specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep line numbers for readable stack traces in crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt / Dagger generated components are referenced reflectively at startup.
-keep class dagger.hilt.internal.aggregatedroot.codegen.* { *; }
-keep class hilt_aggregated_deps.* { *; }

# kotlinx.serialization keeps its own consumer ProGuard rules; nothing
# project-specific is required here beyond the defaults each library ships.
