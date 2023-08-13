### 说明
本项目为frida gadget动态注入工具

badget基于xposed和frida-gadget，实现frida-gadget动态注入。

gadget版本: 16.0.17

参考项目  [xcubebase](https://github.com/svengong/xcubebase)

参考博客地址: [Frida持久化方案(Xcube)之方案二——基于xposed](https://bbs.kanxue.com/thread-266784.htm)


```
免责声明：
本项目仅限用于逆向安全领域爱好者学习参考和研究目的，不得用于商业或者非法用途，否则，一切后果请用户自负。
```

### 实现原理
* 1.拷贝libfrida_gadget.so到/data/user/0/packageName/app_libs/目录下
* 2.在/data/user/0/packageName/app_libs/目录下生成libfrida_gadget.config.so
* 3.反射修改nativeLibraryDirectories，优先加载/data/user/0/packageName/app_libs/
* 4.调用loadLibrary0加载libfrida_gadget.so库

### 目录结构
```tree

├── /data/local/tmp/#tree
├── badget
│   ├── arm64-v8a
│   │   └── libfrida_gadget.so
│   ├── arm64-v7a
│   │   └── libfrida_gadget.so
│   ├── com.network.xf100
│   │   └── hook.js
│   │
│   └── badget.json
└
```

### 配置文件
```json
{
    "enabled":true,
    "pkgConfigs":{
        "com.network.xf100":{
            "enabled":true,
            "jsPath":"/data/local/tmp/badget/com.xxx.xxx/hook.js",
            "pkgName":"com.xxx.xxx",
            "appName":"xxx",
            "soName":"libgienx",
            "type":"script"
        },
        "com.android.chrome":{
            "enabled":false,
            "jsPath":"/data/local/tmp/badget/com.aaaa.bbb/hook.js",
            "pkgName":"com.aaa.bbb",
            "appName":"xxx",
            "soName":"libxhqpr",
            "type":"script"
        }
    }
}
```


### 未实现
- ~~实现界面化配置~~
- ~~使用Material Design主题~~
- ~~gadget库随机命名~~
- ~~关于界面~~
- frida-gadget版本动态下载(无需内置到assets目录)
- 监听脚本内容变化，并更新到/data/local/tmp/badget/packageName/hook.js
- 脚本仓库(脚本市场)
- frida-gadget库去特征
- 可选交互类型
  1. Listen
  2. Connect
  3. ~~Script~~
  4. ScriptDirectory

### 参考&致谢
- [svengong/xcubebase](https://github.com/svengong/xcubebase)
- [Dr-TSNG/Hide-My-Applist](https://github.com/Dr-TSNG/Hide-My-Applist)
- [SeeFlowerX](https://github.com/SeeFlowerX)

### 日志
```log
saveConfig {"enabled":true,"pkgConfigs":{"com.xxx.xxx":{"appName":"xxx","enabled":true,"pkgName":"com.xxx.xxx","type":"Script"}}}
saveConfig to /data/local/tmp/badget/badget.json
saveFileFromUri: 
uri: content://com.android.externalstorage.documents/document/primary%3ADownload%2FWeiXin%2Fxf.js
fileSavePath: /data/local/tmp/badget/com.xxx.xxx/hook.js
saveConfig {"enabled":true,"pkgConfigs":{"com.xxx.xxx":{"appName":"xxx","enabled":true,"jsPath":"/data/local/tmp/badget/com.xxx.xxx/hook.js","pkgName":"com.xxx.xxx","soName":"libnwwyg","type":"Script"}}}
saveConfig to /data/local/tmp/badget/badget.json

handleLoadPackage: com.xxx.xxx
initConfig: 
mConfigCache: null
readFile: 
mConfigCache: ConfigEntity{enabled=true, pkgConfigs={com.xxx.xxx=PkgConfig{pkgName='com.xxx.xf100', appName='xxx', jsPath='/data/local/tmp/badget/com.xxx.xxx/hook.js', soName='csclr', type=Script, enabled=true}}}
mConfig: ConfigEntity{enabled=true, pkgConfigs={com.xxx.xxx=PkgConfig{pkgName='com.xxx.xxx', appName='xxx', jsPath='/data/local/tmp/badget/com.xxx.xxx/hook.js', soName='csclr', type=Script, enabled=true}}}
beforeHookedMethod: 
appGadgetLibPath: /data/user/0/com.xxx.xxx/app_libs/libcsclr.so
copyFile: 
fromDir: /data/local/tmp/badget/armeabi-v7a
toDir: /data/user/0/com.xxx.xxx/app_libs
复制文件到/data/user/0/com.xxx.xxx/app_libs/libcsclr.so
saveAppGadgetConfig: pkgConfig=PkgConfig{pkgName='com.xxx.xxx', appName='xxx', jsPath='/data/local/tmp/badget/com.xxx.xxx/hook.js', soName='csclr', type=Script, enabled=true}
getGadgetConfigJson: {"interaction":{"type":"script","path":"\/data\/local\/tmp\/badget\/com.xxx.xxx\/hook.js","on_change":"reload"}}
gadgetConfigJson: {"interaction":{"type":"script","path":"/data/local/tmp/badget/com.xxx.xxx/hook.js","on_change":"reload"}}
saveConfig {"interaction":{"type":"script","path":"/data/local/tmp/badget/com.xxx.xxx/hook.js","on_change":"reload"}}
saveConfig to /data/user/0/com.xxx.xxx/app_libs/libcsclr.config.so
afterHookedMethod: 
ClassLoader: dalvik.system.PathClassLoader[DexPathList[[zip file "/data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.xxx.xxx-RSJRK8IuxruuvxywghYu5A==/base.apk"],nativeLibraryDirectories=[/data/user/0/com.xxx.xxx/app_libs, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.xxx.xxx-RSJRK8IuxruuvxywghYu5A==/lib/arm, /data/app/~~fXKGHaxFLAxd_-AGBS7TdQ==/com.xxx.xxx-RSJRK8IuxruuvxywghYu5A==/base.apk!/lib/armeabi-v7a, /system/lib, /system_ext/lib, /system/lib, /system_ext/lib]]]
System.load: /data/user/0/com.xxx.xxx/app_libs/libcsclr.so
```
