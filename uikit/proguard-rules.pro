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
##ָ�������ѹ������
#-optimizationpasses 5
##��������ϴ�Сд
#-dontusemixedcaseclassnames
##��ȥ���Էǹ����Ŀ���
#-dontskipnonpubliclibraryclasses
# #�Ż�  ���Ż���������ļ�
#-dontoptimize
# #ԤУ��
#-dontpreverify
# #����ʱ�Ƿ��¼��־
#-verbose
# # ����ʱ�����õ��㷨
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
##����ע��
#-keepattributes *Annotation*
######################��¼���ɵ���־����,gradle buildʱ�ڱ���Ŀ��Ŀ¼���################
##apk �������� class ���ڲ��ṹ
#-dump class_files.txt
##δ��������ͳ�Ա
#-printseeds seeds.txt
##�г��� apk ��ɾ���Ĵ���
#-printusage unused.txt
##����ǰ���ӳ��
#-printmapping mapping.txt
######################��¼���ɵ���־���ݣ�gradle buildʱ �ڱ���Ŀ��Ŀ¼���-end################
##jar��
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
## �ٶȵ�ͼ
#-keep class com.baidu.** { *; }
## ����ͳ��
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
##���������v4����v7��
#-dontwarn android.support.**
#############���������Լ���Ŀ�Ĳ��ִ����Լ����õĵ�����jar��library-end##################
#-keep public class * extends android.view.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#    public void set*(...);
#}
##���� native ������������
#-keepclasseswithmembernames class * {
#    native <methods>;
#}
##�����Զ���ؼ��಻������
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
##�����Զ���ؼ��಻������
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
##�����Զ���ؼ��಻������
#-keepclassmembers class * extends android.app.Activity {
#   public void *(android.view.View);
#}
##���� Parcelable ��������
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}
##����ö�� enum �಻������ ���������������ֱ��ʹ������� -keepclassmembers class * implements java.io.Serializable����
#-keepclassmembers enum * {
#  public static **[] values();
#  public static ** valueOf(java.lang.String);
#}
#-keepclassmembers class * {
#    public void *ButtonClicked(android.view.View);
#}
##��������Դ��
#-keepclassmembers class **.R$* {
#    public static <fields>;
#}
#
##������ػ�������
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
##��� netty ʹ�õĹٷ��汾�����м��õ��˷��䣬�����Ҫ keep�����ʹ�õ��������ṩ�İ汾������Ҫ keep
##-keep class io.netty.** {*;}
