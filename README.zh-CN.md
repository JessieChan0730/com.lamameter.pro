# LumaMeter

[English](README.md) | [简体中文](README.zh-CN.md)

## 这是什么

LumaMeter 是一个基于 Jetpack Compose 和 CameraX 构建的 Android 测光应用。它通过采样相机画面的亮度信息来估算场景明亮度，并提供 EV、光圈、快门和 ISO 等曝光参考。

项目采用 Clean Architecture + MVVM 架构，围绕单一主界面展开，专注于快速测光工作流。

### 核心功能

- 基于 CameraX Y 通道采样的实时测光与单次测光
- 三种测光模式：平均测光、中央重点测光、点测光
- 两种曝光优先模式：光圈优先、快门优先
- AE 锁定、曝光补偿（±3 EV）、校准偏移（±5 EV）
- ND 滤镜选择器与测光补偿
- 常见场景校准预设
- ISO 50 至 6400 预设档位
- 可增删的自定义光圈值 / 快门值列表
- 倍率按钮与滑杆两种变焦控制
- 实时直方图显示
- 英文与简体中文本地化
- Material 3 界面，支持跟随系统 / 日间 / 夜间主题

## 项目目录结构

```text
LumaMeter/
├── app/src/
│   ├── main/java/.../                # 应用源码
│   │   ├── data/camera/              # CameraX Y 通道亮度提取
│   │   ├── domain/exposure/          # 纯 Kotlin 曝光模型与计算逻辑
│   │   ├── ui/components/            # 直方图、测光指示等通用组件
│   │   ├── ui/meter/                 # 主测光界面与相机预览
│   │   ├── ui/theme/                 # Material 3 主题配置
│   │   └── viewmodel/               # 基于 StateFlow 的状态聚合
│   ├── main/res/                     # 资源文件（图标、字符串、主题等）
│   ├── test/                         # JVM 单元测试
│   └── androidTest/                  # 设备端测试
├── gradle/                           # Gradle Wrapper 与版本目录
├── scripts/                          # 辅助脚本（Git Hooks 安装等）
├── .githooks/                        # Git 提交信息格式检查
└── .github/workflows/                # CI 工作流
```

## 如何运行项目

### 环境要求

- Android Studio
- JDK 17 或更高版本
- Android SDK 与构建工具
- 支持相机的 Android 设备或模拟器

### 从 Android Studio 运行

1. 用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 运行 `app` 模块
4. 首次启动时授予相机权限

### 命令行构建

macOS / Linux：

```bash
./gradlew assembleDebug
```

Windows：

```powershell
.\gradlew.bat assembleDebug
```

### 运行测试

```bash
# JVM 单元测试
./gradlew testDebugUnitTest

# 设备端测试（需要连接设备或模拟器）
./gradlew connectedDebugAndroidTest

# 代码检查
./gradlew lint
```

## 下一步计划

- [x] **基本测光功能** — 实时与单次测光、点测光 / 中央重点 / 平均测光、光圈优先 / 快门优先、AE 锁定、曝光补偿、校准偏移、ND 滤镜支持、直方图、变焦、自定义参数列表、主题设置、中英文界面
- [ ] **白平衡检测**
- [ ] **估焦测距**
- [ ] **分区曝光**
- [ ] **BUG 修复**
- [ ] **其他功能**

## 感谢支持

[https://linux.do](https://linux.do)
