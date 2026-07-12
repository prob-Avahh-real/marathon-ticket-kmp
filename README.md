# 马拉松报名 (MarathonTicket)

马拉松赛事报名应用 — 浏览赛事、抢票报名、订单管理。

## 技术架构

```
marathon-ticket-kmp/
├── composeApp/              # Compose Multiplatform UI
│   └── src/commonMain/
│       └── kotlin/com/marathonticket/ui/screens/
│           ├── HomeScreen       # 赛事列表
│           ├── DetailScreen     # 赛事详情
│           ├── OrdersScreen     # 订单管理
│           ├── GrabDialog       # 抢票弹窗
│           └── MarathonTicketApp
├── shared/                  # KMP 共享模块
│   └── src/commonMain/
│       └── kotlin/com/marathonticket/
│           ├── model/       # MarathonEvent, Order, User
│           └── data/        # HttpClient, MockData
├── iosApp/                  # iOS 原生入口
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## 构建命令

```bash
./gradlew build                # 构建全部
./gradlew :shared:build        # 共享模块
./gradlew :composeApp:build    # UI 模块
```

## 测试命令

```bash
./gradlew :shared:check        # 共享模块测试
./gradlew :composeApp:check    # UI 模块测试
./gradlew allTests             # 全平台测试
```
