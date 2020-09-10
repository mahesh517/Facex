# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

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

#-keep public class com.app.Fragments.CameraFragment
#-keep, allowobfuscation class com.app.Fragments.CameraFragment{
#private *;
#}
#-keep public class com.app.MlKitUtils.Classifier
#-keep public class com.app.Utils.FaceRecongintion
#-keep public class com.app.Utils.FileUtils
#-keep public interface com.app.Utils.onFaceRecognition
-dontobfuscate
