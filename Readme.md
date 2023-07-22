### 说明
本项目为frida gadget动态注入工具

gadget版本: 16.0.17


参考项目  [xcubebase](https://github.com/svengong/xcubebase)

参考博客地址: [Frida持久化方案(Xcube)之方案二——基于xposed](https://bbs.kanxue.com/thread-266784.htm)

badget基于xposed和frida-gadget，实现frida-gadget动态注入。libfrida_gadget.so库未重命名，请重命名过检测!


点击hello badget进行初始化，初始化需要root!!!初始化做的工作是把assets目录下的so库复制到/data/local/tmp/badget

### 实现原理
* 1.拷贝libfrida_gadget.so到/data/user/0/packageName/app_libs/目录下
* 2.在/data/user/0/packageName/app_libs/目录下生成libfrida_gadget.config.so
* 3.反射修改nativeLibraryDirectories，优先加载/data/user/0/packageName/app_libs/
* 4.调用loadLibrary0加载libfrida_gadget.so库

### 使用步骤
* 1.点击hello_badget初始化(需要root)
* 2.在/data/local/tmp/badget/目录下新建文件夹,名称为应用包名
* 3.把hook.js放在/data/local/tmp/badget/包名/ 目录下

### 目录结构
```
/data/local/tmp/badget#
├─arm64-v8a
│ └─libfrida_gadget.so
│──arm64-v7a
│  └─libfrida_gadget.so
│─com.network.xf100
│ └─hook.js
```

### 配置



### 未实现
实现界面化配置

监听脚本内容变化，并更新到/data/local/tmp/badget/packageName/hook.js

使用Material Design主题

### 日志

```log
I Loading module com.poorld.badget from /data/app/~~IL2zdeRcpczHoSYRvQltxA==/com.poorld.badget-3Bnvup5FXuuVi8hf9-3JKQ==/base.apk
I    Loading class com.poorld.badget.hook.Badget
D  handleLoadPackage: com.network.xf100
D  beforeHookedMethod:
D  attach beforeHookedMethod toPath:/data/user/0/com.network.xf100/app_libs
D  appGadgetLibPath: /data/user/0/com.network.xf100/app_libs/libfrida_gadget.so
D  复制文件到/data/user/0/com.network.xf100/app_libs/libfrida_gadget.so
D  saveAppGadgetConfig:
D  getGadgetConfigJson: {"interaction":{"type":"script","path":"\/data\/local\/tmp\/badget\/com.network.xf100\/hook.js","on_change":"reload"}}
D  gadgetConfigJson: {"interaction":{"type":"script","path":"/data/local/tmp/badget/com.network.xf100/hook.js","on_change":"reload"}}
D  saveConfig to /data/user/0/com.network.xf100/app_libs/libfrida_gadget.config.so
D  afterHookedMethod:
D  copy libs and config to : /data/user/0/com.network.xf100/app_libs
D  installNativeLibraryPath:
D  libDirs: [/data/user/0/com.network.xf100/app_libs, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.network.xf100-RSJRK8IuxruuvxywghYu5A==/lib/arm, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.network.xf100-RSJRK8IuxruuvxywghYu5A==/base.apk!/lib/armeabi-v7a, /system/lib, /system_ext/lib]
D  ClassLoader: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.network.xf100-RSJRK8IuxruuvxywghYu5A==/base.apk"],nativeLibraryDirectories=[/data/user/0/com.network.xf100/app_libs, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.network.xf100-RSJRK8IuxruuvxywghYu5A==/lib/arm, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.network.xf100-RSJRK8IuxruuvxywghYu5A==/base.apk!/lib/armeabi-v7a, /system/lib, /system_ext/lib, /system/lib, /system_ext/lib]]]
D  System.load: /data/user/0/com.network.xf100/app_libs/libfrida_gadget.so
```
