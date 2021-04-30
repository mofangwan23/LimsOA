-optimizationpasses 5
-dontskipnonpubliclibraryclassmembers
-printmapping proguardMapping.txt
-optimizations !code/simplification/cast,!field/*,!class/merging/*
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable

-keep class * implements android.os.Parcelable
-keep class * implements java.io.Serializable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService

-keep class android.support.** {*;}
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

-keepclassmembers enum * { *; }

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}


-keep class **.R$* {
 *;
}

-keepclassmembers class * {
    void *(**On*Event);
}

-keepclassmembers class * {
    void *(*Event);
}

-keepclassmembers class ** {
    @cn.flyrise.feep.core.premission.PermissionGranted <methods>;
}

-keepclassmembers class fqcn.of.javascript.interface.for.Webview {
   public *;
}

-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}

-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, jav.lang.String);
}

# 与 JavaScript 交互部分
-keepclasseswithmembers class cn.flyrise.feep.form.MeetingBoardActivity$Controller {
      <methods>;
}

-keep class cn.flyrise.android.shared.bean.** { *; }
-keep class cn.flyrise.android.shared.model.** { *; }
-keep class cn.flyrise.android.protocol.** { *; }
-keep class cn.flyrise.feep.collaboration.model.** { *; }
-keep class cn.flyrise.feep.commonality.bean.** { *; }
-keep class cn.flyrise.feep.dbmodul.** { *; }
-keep class cn.flyrise.feep.cordova.plugin.** { *; }
-keep class cn.flyrise.feep.cordova.model.** { *; }

-keep class cn.flyrise.feep.addressbook.model.** { *; }
-keep class cn.flyrise.feep.salary.model.** { *; }
-keep class cn.flyrise.feep.core.network.cookie.** { *; }
-keep class cn.flyrise.feep.core.protocol.model.** { *; }
-keep class cn.flyrise.feep.core.network.request.** { *; }
-keep class cn.squirtlez.frouter.** { *; }
-keep class cn.flyrise.feep.userinfo.modle.**{ *; }

-keep class cn.flyrise.feep.auth.views.gesture.**{*;}

#签到
-keep class cn.flyrise.feep.location.model.** { *; }
-keep class cn.flyrise.feep.location.bean.** { *; }

# okhttp3.x
-dontwarn okhttp3.**
-keep class okhttp3.** { *;}

-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.**{*;}

-keep interface okhttp3.** { *; }

# okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keep class okio.**{*;}
-dontwarn okio.**


# apache
-dontwarn org.apache.**
-keep class org.apache.** { *; }
-keepclassmembers class * extends org.apache.cordova.CordovaPlugin {
    public *;
}

# okhttp
-dontwarn com.squareup.okhttp.**
-keep class com.squareup.okhttp.{*;}

# umeng
-dontwarn com.umeng.**
-dontwarn u.aly.**
-keep class com.umeng.** { *; }
-keep class u.aly.** { *; }
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keep public class * extends com.umeng.**

# eventbus 3.0
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}

-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

-keepclassmembers class ** {
    public void onEvent*(**);
}

# Gson
-dontwarn com.google.**
-keep class com.google.gson.** {*;}
-keep class com.google.**{*;}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

# support-v4
-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v4.** { *; }

# support-v7
-dontwarn android.support.v7.**
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
-keep class android.support.v7.** { *; }

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# jpush极光推送
-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }
-keep class cn.jpush.** { *; }
#==================protobuf======================
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

-keep class cn.flyrise.feep.push.handle.**{*;}
-keep class cn.flyrise.feep.core.notification.**{*;}

#华为推送
#-ignorewarning -keepattributes *Annotation*
-keepattributes Exceptions -keepattributes InnerClasses
-keepattributes Signature
-keep class * extends com.huawei.hms.core.aidl.IMessageEntity { *; }
-keepclasseswithmembers class * implements com.huawei.hms.support.api.transport.DatagramTransport {<init>(...); }
-keep public class com.huawei.hms.update.provider.UpdateProvider { public *; protected *; }
-keep class cn.flyrise.feep.huawei.**{*;}

-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}
-keep class com.huawei.gamebox.plugin.gameservice.**{*;}
-keep public class com.huawei.android.hms.agent.** extends android.app.Activity { public *; protected *; }
-keep interface com.huawei.android.hms.agent.common.INoProguard {*;}
-keep class * extends com.huawei.android.hms.agent.common.INoProguard {*;}

-dontwarn com.hyphenate.push.**
-dontwarn com.huawei.appmarket.**
-dontwarn com.huawei.hmf.**
-dontwarn com.huawei.updatesdk.**
-dontwarn com.bumptech.glide.load.**

#小米推送
-keepclasseswithmembernames class com.xiaomi.**{*;}
-keep public class * extends com.xiaomi.mipush.sdk.PushMessageReceiver
-keep class cn.flyrise.feep.push.target.xiaomi.XiaomiBroadcastReceiver {*;}
#可以防止一个误报的 warning 导致无法成功编译，如果编译使用的 Android 版本是 23。
-dontwarn com.xiaomi.push.**

#环信推送
-dontwarn com.hyphenate.push.***
-keep class com.hyphenate.push.*** {*;}


# 百度自动更新
-keep class com.baidu.autoupdatesdk.**{*;}

# java base64
-dontwarn it.sauronsoftware.base64.**
-keep class it.sauronsoftware.base64.** { *; }

# chardet
-keep class org.mozilla.intl.chardet.** { *; }

# 高徳地图
-dontwarn com.amap.api.**
-dontwarn com.a.a.**
-dontwarn com.autonavi.**
-dontwarn com.loc.**
-keep class com.amap.api.**  { *; }
-keep class com.loc.** { *; }
-keep class com.autonavi.**  { *; }
-keep class com.a.a.**  { *; }

-keep class org.webrtc.videoengine.** { *; }
-keep class org.webrtc.voiceengine.** { *; }

# Sangfor VPN
-dontwarn com.sangfor.**
-keep class com.sangfor.** { *; }

# DBFlow
-dontwarn com.raizlabs.**
-keep class com.raizlabs.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class * extends com.raizlabs.android.dbflow.config.DatabaseHolder { *; }

-keepclasseswithmembernames class * {
    @com.raizlabs.android.dbflow.annotation.* <fields>;
}
-keepclasseswithmembernames class * {
    @com.raizlabs.android.dbflow.annotation.* <methods>;
}

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }

# Retrolambda
-dontwarn java.lang.invoke.*

# date time
-dontwarn com.borax12.materialdaterangepicker.**
-keep class com.borax12.materialdaterangepicker.** { *; }

# Bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# RxJava
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# 第三方库
-keep class com.dk.view.** { *; }
-keep class com.drag.** { *; }
-keep class com.google.android.** { *; }
-keep class com.handmark.** { *; }
-keep class com.loading.** { *; }
-keep class com.picture.** { *; }
-keep class com.softfarique.photoviewlibrary.** { *; }
-keep class com.haibuzou.datepicker.calendar.** { *; }
-keep class jp.wasabeef.richeditor.** { *; }

# 二维码扫描
-keep class com.google.zxing.** {*;}
-dontwarn com.google.zxing.**

-dontwarn com.github.rahatarmanahmed.**
-keep class com.github.rahatarmanahmed.** { *; }

-dontwarn com.github.bumptech.glide.**
-keep class com.github.bumptech.glide.** { *; }
-keep class com.bumptech.glide.integration.**{ *; }

#阿里百川反馈
-keep class com.taobao.** {*;}
-keep class com.alibaba.** {*;}
-dontwarn com.taobao.**
-dontwarn com.alibaba.**
-keep class com.ut.** {*;}
-dontwarn com.ut.**
-keep class com.ta.** {*;}
-dontwarn com.ta.**


# 测试相关代码
-dontnote junit.framework.**
-dontnote junit.runner.**
-dontwarn android.test.**
-dontwarn android.support.test.**

-dontwarn org.junit.**

-dontwarn com.hyphenate.chat.**
-keep class com.hyphenate.chat.** { *; }
-dontwarn com.hyphenate.chatui.**
-keep class com.hyphenate.chatui.** { *; }
-dontwarn com.hyphenate.easeui.**
-keep class com.hyphenate.easeui.** { *; }

# 三星指纹识别
-dontwarn com.samsung.android.sdk.**
-keep class com.samsung.android.sdk.** { *; }

# 魅族指纹识别
-dontwarn com.fingerprints.service.**
-keep class com.fingerprints.service.** { *; }

-dontwarn me.leolin.shortcutbadger.**
-keep class me.leolin.shortcutbadger.** { *; }

-dontwarn com.superrtc.**
-keep class com.superrtc.** { *; }

-dontwarn internal.org.apache.http.entity.mime.**
-keep class internal.org.apache.http.entity.mime.** { *; }

-keep class cn.flyrise.feep.utils.ChatGifGlideModule

#裁剪
-keep class com.kevin.** { *; }

#讯飞语音助手
-dontwarn com.iflytek.**
-keep class com.iflytek.**{*;}
-keep class cn.flyrise.feep.robot.analysis.**{*;}
-keep class cn.flyrise.feep.robot.entity.**{*;}
-keep class cn.flyrise.feep.robot.util.**{*;}
-keep class cn.flyrise.feep.robot.bean.**{*;}

#手机盾
-dontwarn cn.trust.**
-keep class cn.trust.**{*;}
-keep class com.taobao.securityjni.**{*;}
-keep class com.taobao.wireless.security.**{*;}
-keep class com.ut.secbody.**{*;}
-keep class com.taobao.dp.**{*;}
-keep class com.alibaba.wireless.security.**{*;}

# data-retrieval
-keep class cn.flyrise.feep.retrieval.dispatcher.** { *; }
-keep class cn.flyrise.feep.retrieval.protocol.** { *; }
-keep class cn.flyrise.feep.retrieval.bean.** { *; }

# meeting
-keep class cn.flyrise.feep.meeting7.protocol.** { *; }
-keep class cn.flyrise.feep.meeting7.selection.bean.** { *; }
-keep class cn.flyrise.feep.meeting7.ui.bean.** { *; }

# collection
-keep class cn.flyrise.feep.collection.bean.** { *; }
-keep class cn.flyrise.feep.collection.protocol.** { *; }

# study
-keep class cn.flyrise.feep.study.entity.**{ *; }

#workplan7
-keep class cn.flyrise.feep.workplan7.model.** { *; }
-keep class cn.flyrise.feep.workplan7.provider.** { *; }

#二维码扫描登陆
-keep class cn.flyrise.feep.qrcode.model.**{*;}

#X5 WebView
-dontskipnonpubliclibraryclassmembers
-dontwarn dalvik.**
-dontwarn com.tencent.smtt.**
-keep class com.tencent.smtt.export.external.**{ *; }
-keep class com.tencent.tbs.video.interfaces.IUserStateChangedListener { *; }
-keep class com.tencent.smtt.sdk.CacheManager {
	public *;
}
-keep class com.tencent.smtt.sdk.CookieManager {
	public *;
}
-keep class com.tencent.smtt.sdk.WebHistoryItem {
	public *;
}
-keep class com.tencent.smtt.sdk.WebViewDatabase {
	public *;
}
-keep class com.tencent.smtt.sdk.WebBackForwardList {
	public *;
}
-keep public class com.tencent.smtt.sdk.WebView {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebView$HitTestResult {
	public static final <fields>;
	public java.lang.String getExtra();
	public int getType();
}
-keep public class com.tencent.smtt.sdk.WebView$WebViewTransport {
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebView$PictureListener {
	public <fields>;
	public <methods>;
}
-keepattributes InnerClasses
-keep public enum com.tencent.smtt.sdk.WebSettings$** { *; }
-keep public enum com.tencent.smtt.sdk.QbSdk$** { *; }
-keep public class com.tencent.smtt.sdk.WebSettings {
    public *;
}
-keepattributes Signature
-keep public class com.tencent.smtt.sdk.ValueCallback {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebViewClient {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebChromeClient {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebChromeClient$FileChooserParams {
	public <fields>;
	public <methods>;
}
-keep class com.tencent.smtt.sdk.SystemWebChromeClient{
	public *;
}
# 1. extension interfaces should be apparent
-keep public class com.tencent.smtt.export.external.extension.interfaces.* {
	public protected *;
}
# 2. interfaces should be apparent
-keep public class com.tencent.smtt.export.external.interfaces.* {
	public protected *;
}
-keep public class com.tencent.smtt.sdk.WebViewCallbackClient {
	public protected *;
}
-keep public class com.tencent.smtt.sdk.WebStorage$QuotaUpdater {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebIconDatabase {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.WebStorage {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.QbSdk {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.QbSdk$PreInitCallback {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.Tbs* {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.utils.LogFileUtils {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.utils.TbsLog {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.utils.TbsLogClient {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}
# Added for game demos
-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.utils.Apn {
	public <fields>;
	public <methods>;
}
-keep class com.tencent.smtt.** { *; }
-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
	public <fields>;
	public <methods>;
}
-keep class MTT.ThirdAppInfoNew { *; }
-keep class com.tencent.mtt.MttTraceEvent { *; }
-keep public class com.tencent.smtt.gamesdk.* {
	public protected *;
}
-keep public class com.tencent.smtt.sdk.TBSGameBooter {
  public <fields>;
  public <methods>;
}
-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
	public protected *;
}
-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
	public protected *;
}
-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
	public *;
}
-keepclasseswithmembers class * {
  ... *JNI*(...);
}
-keepclasseswithmembernames class * {
	... *JRI*(...);
}
-keep class **JNI* { *; }

-keep class com.chad.library.adapter.** {
*;
}
-keep public class * extends com.chad.library.adapter.base.BaseQuickAdapter
-keep public class * extends com.chad.library.adapter.base.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.BaseViewHolder {
     <init>(...);
}

-keep public class cn.flyrise.feep.form.widget.handWritting.FESlate$WhiteBoard{*;}
-keep public class com.google.android.apps.brushes.**{*;}
