## Add project specific ProGuard rules here.
## By default, the flags in this com.cqyw.smart.file are appended to flags specified
## in E:\Program Files\Android\sdk/tools/proguard/proguard-android.txt
## You can edit the include path and order by changing the proguardFiles
## directive in build.gradle.
##
## For more details, see
##   http://developer.android.com/guide/developing/tools/proguard.html
#
## Add any project specific keep options here:
#
## If your project uses WebView with JS, uncomment the following
## and specify the fully qualified class name to the JavaScript interface
## class:
##-keepclassmembers class fqcn.of.javascript.interface.for.webview {
##   public *;
##}
##指定代码的压缩级别
#-optimizationpasses 5
##包明不混合大小写
#-dontusemixedcaseclassnames
##不去忽略非公共的库类
#-dontskipnonpubliclibraryclasses
# #优化  不优化输入的类文件
#-dontoptimize
# #预校验
#-dontpreverify
# #混淆时是否记录日志
#-verbose
# # 混淆时所采用的算法
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
##保护注解
#-keepattributes *Annotation*
######################记录生成的日志数据,gradle build时在本项目根目录输出################
##apk 包内所有 class 的内部结构
#-dump class_files.txt
##未混淆的类和成员
#-printseeds seeds.txt
##列出从 apk 中删除的代码
#-printusage unused.txt
##混淆前后的映射
#-printmapping mapping.txt
######################记录生成的日志数据，gradle build时 在本项目根目录输出-end################
##jar包
##-libraryjars libs/arm64-v8a/libcosine.so
##-libraryjars libs/arm64-v8a/libnio.so
##-libraryjars libs/arm64-v8a/librts_network.so
##-libraryjars libs/armeabi-v7a/libcosine.so
##-libraryjars libs/armeabi-v7a/libnio.so
##-libraryjars libs/armeabi-v7a/librts_network.so
##-libraryjars libs/x86/libcosine.so
##-libraryjars libs/x86/libnio.so
##-libraryjars libs/x86/librts_network.so
##-libraryjars libs/android-support-v4.jar
##-libraryjars libs/android-support-v7-appcompat.jar
##-libraryjars libs/fastjson-1.1.34.android.jar
##-libraryjars libs/netty-4.0.23-for-yx.final.jar
##-libraryjars libs/nim-sdk-1.5.0.jar
##-libraryjars libs/universal-image-loader-1.9.4.jar
##Activiy
#-keep public class * extends android.app.Fragment
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.support.v4.**
#-keep public class * extends android.support.v7.**
#-keep public class com.android.vending.licensing.ILicensingService
## 百度地图
#-keep class com.baidu.** { *; }
## 友盟统计
#-keep class com.umeng.** { *; }
#-keep class com.umeng.analytics.** { *; }
#-keep class com.umeng.common.** { *; }
#-keep class com.umeng.newxp.** { *; }
#-dontwarn com.google.android.maps.**
#-dontwarn android.webkit.WebView
#-dontwarn com.umeng.**
#-dontwarn com.tencent.weibo.sdk.**
#-dontwarn com.facebook.**
#-keep enum com.facebook.**
#-keepattributes Exceptions,InnerClasses,Signature
#-keepattributes *Annotation*
#-keepattributes SourceFile,LineNumberTable
#-keep public interface com.facebook.**
#-keep public interface com.tencent.**
#-keep public interface com.umeng.socialize.**
#-keep public interface com.umeng.socialize.sensor.**
#-keep public interface com.umeng.scrshot.**
#-keep public class com.umeng.socialize.* {*;}
#-keep public class javax.**
#-keep public class android.webkit.**
#-keep class com.facebook.**
#-keep class com.umeng.scrshot.**
#-keep public class com.tencent.** {*;}
#-keep class com.umeng.socialize.sensor.**
#-keep class com.tencent.mm.sdk.openapi.WXMediaMessage {*;}
#-keep class com.tencent.mm.sdk.openapi.** implements com.tencent.mm.sdk.openapi.WXMediaMessage$IMediaObject {*;}
#-keep class im.yixin.sdk.api.YXMessage {*;}
#-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
##如果引用了v4或者v7包
#-dontwarn android.support.**
#############混淆保护自己项目的部分代码以及引用的第三方jar包library-end##################
#-keep public class * extends android.view.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#    public void set*(...);
#}
##保持 native 方法不被混淆
#-keepclasseswithmembernames class * {
#    native <methods>;
#}
##保持自定义控件类不被混淆
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
##保持自定义控件类不被混淆
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
##保持自定义控件类不被混淆
#-keepclassmembers class * extends android.app.Activity {
#   public void *(android.view.View);
#}
##保持 Parcelable 不被混淆
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}
##保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的 -keepclassmembers class * implements java.io.Serializable即可
#-keepclassmembers enum * {
#  public static **[] values();
#  public static ** valueOf(java.lang.String);
#}
#-keepclassmembers class * {
#    public void *ButtonClicked(android.view.View);
#}
##不混淆资源类
#-keepclassmembers class **.R$* {
#    public static <fields>;
#}
#
##网易相关混淆配置
##-dontwarn com.netease.**
##-dontwarn io.netty.**
##-keep class com.netease.media.** {*;}
##-keep class com.netease.nimlib.** {*;}
##-keep class com.netease.rtc.** {*;}
##-keep class com.netease.nim.uikit.common.** {*;}
##-keep class com.netease.nim.uikit.contact.** {*;}
##-keep class com.netease.nim.uikit.contact_selector.** {*;}
##-keep class com.netease.nim.uikit.recent.** {*;}
##-keep class com.netease.nim.uikit.session.** {*;}
##-keep class com.netease.nim.uikit.team.** {*;}
##-keep class com.netease.nim.uikit.uinfo.** {*;}
##-keep class com.netease.nim.uikit.ImageLoaderKit {*;}
##-keep class com.netease.nim.uikit.LocationProvider {*;}
##-keep class com.netease.nim.uikit.NimUIKit {*;}
##如果 netty 使用的官方版本，它中间用到了反射，因此需要 keep。如果使用的是我们提供的版本，则不需要 keep
##-keep class io.netty.** {*;}
