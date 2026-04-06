# LumaMeter

[English](README.md) | [简体中文](README.zh-CN.md)

LumaMeter 是一个基于 Jetpack Compose 和 CameraX 构建的 Android 测光应用。它通过采样相机画面的亮度信息来估算场景明亮度，并提供 EV、光圈、快门和 ISO 等基础曝光参考。

当前版本是一个面向快速测光流程的 MVP，重点放在现代 Material 3 界面和便于后续扩展的清晰架构上。

## 概览

- 实时相机预览
- 基于相机 Y 平面的实时亮度分析
- 三种测光模式
  - 平均测光
  - 中央重点测光
  - 点测光
- 两种曝光优先模式
  - 光圈优先
  - 快门优先
- 实时显示 EV、光圈、快门和 ISO
- 点击测光交互
- AE 锁定
- 曝光补偿
- 校准偏移
- 英文和简体中文本地化
- Material 3 界面

## 为什么做这个应用

LumaMeter 旨在快速回答三个问题：

1. 我应该测哪里？
2. 我应该控制哪个参数？
3. 我现在该采用什么曝光组合？

因此，这个应用围绕单一主界面展开，而不是设计成层级很深的多页面流程。

## 界面展示

当前仓库还没有包含截图。后续建议补充以下素材：

- 主测光界面
- 控制面板
- 多语言界面预览

README 图片示例：

```md
![Main Screen](docs/images/main-screen.png)
![Controls](docs/images/controls.png)
```

## 功能

### 测光流程

- 使用 CameraX 预览与 Y 通道采样进行实时测光
- 支持单次测光模式，可在关闭实时刷新后点击预览获取一次结果
- 提供点测光、中央重点测光、平均测光三种模式
- 支持点击移动测光点，并在预览上显示测光指示
- 实时显示 EV、当前测得亮度与平均亮度

### 曝光控制

- 提供光圈优先与快门优先两种曝光建议模式
- 内置 ISO 50 到 6400 的常用档位
- 支持 AE Lock 锁定当前读数
- 提供 `-3 EV` 到 `+3 EV` 的曝光补偿滑杆
- 提供 `-2 EV` 到 `+2 EV` 的校准偏移，用于设备或流程微调

### 工作流与界面

- 单主界面优先的测光流程，预览、结果与常用控制集中展示
- 设置页支持实时测光与单次测光切换
- 提供跟随系统、日间、夜间三种主题，并保存用户选择
- 提供光圈值与快门值列表，支持添加和删除自定义项
- 在支持的设备上提供倍率按钮与滑杆两种变焦控制
- 支持英文与简体中文界面

## 架构

项目采用轻量化的 `Clean Architecture + MVVM` 风格：

```text
UI (Jetpack Compose / Material 3)
  -> ViewModel (state + interaction)
    -> Domain (exposure calculation)
      -> Data (CameraX luminance analyzer)
```

### 数据流

```mermaid
flowchart LR
    A[Camera Preview] --> B[Y Plane Sampling]
    B --> C[Metering Mode Calculation]
    C --> D[EV Estimation]
    D --> E[Exposure Result]
    E --> F[Compose UI]
```

### 分层职责

- `ui/`
  - 界面、面板、手势，以及 Material 3 呈现层
- `viewmodel/`
  - UI 状态聚合、AE Lock、补偿、校准与模式切换
- `domain/exposure/`
  - 纯 Kotlin 的曝光模型与计算逻辑
- `data/camera/`
  - 从 Y 通道提取亮度信息的 CameraX 分析器

## 项目结构

```text
app/src/main/java/com/yourbrand/lumameter/pro/
|-- MainActivity.kt
|-- data/
|   `-- camera/
|       `-- LuminanceAnalyzer.kt
|-- domain/
|   `-- exposure/
|       |-- ExposureCalculator.kt
|       `-- ExposureModels.kt
|-- ui/
|   |-- meter/
|   |   |-- MeterCameraPreview.kt
|   |   `-- MeterScreen.kt
|   `-- theme/
|       |-- Color.kt
|       |-- Theme.kt
|       `-- Type.kt
`-- viewmodel/
    `-- MeterViewModel.kt
```

## 技术栈

- Kotlin
- Android Gradle Plugin 9.1.0
- Jetpack Compose
- Material 3
- CameraX
- ViewModel
- StateFlow
- Coroutines

## 测光策略

当前实现采用一种务实的基于亮度的近似方案：

1. 从图像 Y 通道采样亮度
2. 应用所选测光模式
3. 将亮度映射到 `EV100`
4. 再结合 ISO、曝光补偿和校准偏移换算出曝光建议

这让当前版本适合用于：

- 快速曝光参考
- 日常拍摄辅助
- 后续扩展成更高级测光工具的基础版本

但它暂时还不能被视作专业独立测光表的完全替代品。

## 本地化

当前应用支持：

- English
- 简体中文

资源文件位于：

- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values-zh/strings.xml`

用户可见文本已经从 UI 和状态处理逻辑中抽离，后续如果增加更多语言，代码层面的改动会比较小。

## 快速开始

### 环境要求

- Android Studio
- JDK 17 或更高版本
- Android SDK 与构建工具
- 支持相机的 Android 设备或模拟器

### 运行应用

1. 用 Android Studio 打开项目
2. 等待 Gradle 同步完成
3. 运行 `app` 模块
4. 首次启动时授予相机权限

### 构建

macOS / Linux：

```bash
./gradlew assembleDebug
```

Windows：

```powershell
.\gradlew.bat assembleDebug
```

## 测试

当前测试覆盖已经包含领域层与状态层单元测试：

- `ExposureCalculatorTest`
- `MeterViewModelTest`

后续建议补充：

- 测光模式计算测试
- UI 截图测试
- 分析器边界情况测试
- CameraX 权限与生命周期相关的集成测试

## 当前状态

已实现：

- 实时测光与单次测光两种工作模式
- 点测光 / 中央重点 / 平均测光与点击移动测光点
- 光圈优先 / 快门优先曝光计算与 ISO 预设
- AE Lock、曝光补偿与校准偏移
- 主题设置、变焦控制与中英文界面
- 可增删的自定义光圈值 / 快门值列表
- 覆盖曝光计算与 ViewModel 状态切换的基础单元测试

计划中：

- 测光历史与结果回看
- 传感器辅助或融合式测光
- 更完善的设备校准流程与机型配置
- 自定义参数库与更多偏好项的持久化
- 截图、演示图与发布素材
- 更完整的 UI / 集成测试

## 路线图

- [x] 实时测光主界面
- [x] 单次测光模式
- [x] 点测光 / 平均测光 / 中央重点测光
- [x] 光圈优先 / 快门优先
- [x] AE Lock、曝光补偿与校准偏移
- [x] 主题设置与中英文本地化
- [x] 自定义光圈 / 快门列表
- [x] 支持设备上的变焦控制
- [ ] 测光历史
- [ ] 传感器辅助 / Lux 测光
- [ ] 自定义参数库与更多偏好项持久化
- [ ] 更完善的校准流程与设备配置
- [ ] 截图与发布素材
- [ ] 更完整的 UI / 集成测试
