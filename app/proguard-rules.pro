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


#指定代码的压缩级别
-optimizationpasses 5

#包明不混合大小写
-dontusemixedcaseclassnames

#不去忽略非公共的库类
-dontskipnonpubliclibraryclasses

 #优化  不优化输入的类文件
-dontoptimize

 #预校验
-dontpreverify

 #混淆时是否记录日志
-verbose

 # 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

# 保护代码中的Annotation不被混淆
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }

# 避免混淆泛型
# 这在JSON实体映射时非常重要，比如fastJson
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 保留所有的本地native方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
-keep public class * extends android.support.v4.app.Fragment


#忽略警告
-ignorewarning

##记录生成的日志数据,gradle build时在本项目根目录输出##
#apk 包内所有 class 的内部结构
-dump class_files.txt
#未混淆的类和成员
-printseeds seeds.txt
#列出从 apk 中删除的代码
-printusage unused.txt
#混淆前后的映射
-printmapping mapping.txt
########记录生成的日志数据，gradle build时 在本项目根目录输出-end######

#如果引用了v4或者v7包
-dontwarn android.support.**

####混淆保护自己项目的部分代码以及引用的第三方jar包library-end####


#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable

#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#保持枚举 enum 类不被混淆
-keepclassmembers enum * {
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#support.v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

# 不要警告“找不到android.os.SystemProperties”
-dontwarn android.os.**

# 不要警告lambda相关信息
-dontwarn java.lang.invoke.**

# Glide start
# -keep public class * implements com.bumptech.glide.module.GlideModule
# -keep public class * extends com.bumptech.glide.module.AppGlideModule
# -keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
#   **[] $VALUES;
#   public *;
# }

 # Glide end

 # RxJava RxAndroid start
# -dontwarn sun.misc.**
# -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
#     long producerIndex;
#     long consumerIndex;
# }
# -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
#     rx.internal.util.atomic.LinkedQueueNode producerNode;
# }
# -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
#     rx.internal.util.atomic.LinkedQueueNode consumerNode;
# }
 # RxJava RxAndroid end

 # Gson start
# -keep class sun.misc.Unsafe { *; }
# -dontwarn com.google.gson.annotations.**
# -dontwarn com.google.gson.internal.**
# -dontwarn com.google.gson.reflect.**
# -dontwarn com.google.gson.stream.**
# -dontwarn com.google.gson.**
# -keep class com.google.gson.annotations.** { *; }
# -keep class com.google.gson.internal.** { *; }
# -keep class com.google.gson.reflect.** { *; }
# -keep class com.google.gson.stream.** { *; }
# -keep class com.google.gson.** { *; }
 # Gson end

### greenDAO 3
#-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
#public static java.lang.String TABLENAME;
#}
#-keep class **$Properties

# If you do not use SQLCipher:
# -dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
 -dontwarn rx.**

 # 如果按照上面介绍的加入了数据库加密功能，则需添加一下配置
 #sqlcipher数据库加密开始
# -keep  class net.sqlcipher.** {*;}
# -keep  class net.sqlcipher.database.** {*;}
# #sqlcipher数据库加密结束
#
## calendarview start
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context);
#}

# calendarview end

# EventBus start
# 对于带有回调函数onXXEvent的，不能混淆
#-keepclassmembers class * {
#    void *(**On*Event);
#}
-keepattributes *Annotation*
#-keepclassmembers class * {
#    @org.greenrobot.eventbus.Subscribe <methods>;
#}
#-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
#-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
#    <init>(java.lang.Throwable);
#}

# EventBus End

-keep class cn.pax.odd.keyinjection.**{*;}

-dontwarn com.pax.api.**
#-keep class com.pax.baselink.** { *; }
#-keep class com.pax.base.** { *; }
-dontwarn com.pax.gl.**
-keep class com.pax.gl.** { *; }
#-keep class com.pax.utils.** { *; }
-dontwarn com.pax.neptunelite.api.**
-keep class com.pax.neptunelite.api.**{ *; }
-keep class com.pax.dal.**{ *; }
# android-gif-drawable start
#-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
#-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}
# android-gif-drawable end