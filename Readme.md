### 说明

Badget是frida的持久化工具，通过注入frida-gadget，让目标app加载该so文件，进而实现frida的hook功能。

gadget版本: 16.0.17

参考项目  [xcubebase](https://github.com/svengong/xcubebase)

参考博客地址: [Frida持久化方案(Xcube)之方案二——基于xposed](https://bbs.kanxue.com/thread-266784.htm)

<div>
<img src="https://github.com/poorld/badget/blob/main/show_1.jpg" width="300">&nbsp;&nbsp;
<img src="https://github.com/poorld/badget/blob/main/show_2.jpg" width="300">
</div>


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
│   ├── com.aaa.bbb
│   │   └── hook.js
│   │
│   └── badget.json
└
```

### 配置文件(自动生成)
```json
{
    "enabled":true,
    "pkgConfigs":{
        "com.network.xf100":{
            "appName":"xx加速器",
            "enabled":true,
            "jsPath":"/data/local/tmp/badget/com.xxx.xxx/xx.js",
            "pkgName":"com.xxx.xxx",
            "soName":"zgskl",
            "type":"Script"
        },
        "com.android.chrome":{
            "enabled":false,
            "pkgName":"com.android.chrome",
            "soName":"xykod",
            "type":"Script"
        }
    }
}
```
### 已实现
- 实现界面化配置
- 使用Material Design主题
- gadget库随机命名
- 可选交互类型
  1. Listen
  2. Connect
  3. Script
  4. ScriptDirectory

### 未实现
- frida-gadget版本动态下载(无需内置到assets目录)
- 监听脚本内容变化，并更新到/data/local/tmp/badget/packageName/hook.js
- 脚本仓库(脚本市场)
- frida-gadget库去特征
- 日志悬浮窗
- 设置界面


### 参考&致谢
- [svengong/xcubebase](https://github.com/svengong/xcubebase)
- [Dr-TSNG/Hide-My-Applist](https://github.com/Dr-TSNG/Hide-My-Applist)
- [SeeFlowerX](https://github.com/SeeFlowerX)

### V1.2
1.添加签名
2.优化代码
