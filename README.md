# 楼宇管理系统

基于 Spring Boot 3 + MyBatis-Plus + MySQL 8 + Vue 3 + Element Plus 构建的楼宇房屋全生命周期管理平台，实现对集团内楼宇、房屋的统一管理，包含基础数据管理、使用审批、对外租赁、统计分析及权限管控等核心功能模块。

## How to Run

### Docker 一键部署（推荐）

```bash
# 进入项目根目录
cd label-01234

# 构建并启动所有服务
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

服务启动后访问：http://localhost:8081

### 本地开发环境

**1. 数据库准备**

```bash
# 首次部署：创建 MySQL 数据库并导入初始数据
mysql -u root -p < frontend-admin/backend/src/main/resources/schema.sql
```

> **重要提示**: 
> - `schema.sql` 包含 `DROP TABLE` 语句，执行会清空已有数据
> - 首次部署时需手动执行上述命令初始化数据库
> - 应用默认不会自动执行 SQL 初始化（`spring.sql.init.mode: never`）
> - 如需开发环境自动初始化，可在 `application.yml` 中将 `mode` 改为 `always`（慎用）

**2. 启动后端**

```bash
cd frontend-admin/backend

# 修改 application.yml 中的数据库连接配置（如需）
# 使用 Maven 启动
mvn spring-boot:run
```

后端服务地址：http://localhost:8080

**3. 启动前端**

```bash
cd frontend-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务地址：http://localhost:5173

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend-admin | 8081 | 楼宇管理系统前端（Nginx） |
| backend | 8080 | 楼宇管理系统后端（Spring Boot） |
| mysql | 3307 | MySQL 8.0 数据库 |

## 测试账号

| 角色 | 用户名 | 密码 | 所属单位 | 权限说明 |
|------|--------|------|----------|----------|
| 集团管理员 | admin | 123456 | 集团总部 | 可查看/管理所有单位的楼宇房屋数据 |
| 单位管理员 | manager | 123456 | 分公司A | 仅可管理本单位内的楼宇房屋数据 |

> **注意**: 新创建用户的默认密码为 `123456`，可通过用户管理功能重置密码。

## 题目内容

楼宇管理需求方案 


楼宇管理主要实现对集团内楼宇、房屋的统一管理，包含对楼宇、房屋基础信息的维护，可实时查看各房屋的状态，及对房屋使用情况的管理追踪，通过数字化统计分析各单位、楼宇房屋的使用率、效益情况等。 

详细需求内容： 
一、楼宇房屋基础数据管理 
1、楼宇房屋基本信息的维护（房屋登记、变更修改、封存等） 
2、房屋内部资产配置管理 
3、房屋健康情况 
二、房屋使用 
1、内部使用审批（办公/宿舍/公寓/教学楼/病房使用等） 
2、对外出租管理（租赁方信息、租赁情况、租赁合约等） 
三、统计分析 
1、房屋信息统计 
可实时统计查看各楼宇、房屋的情况，支持多维度条件查询，可根据单位、楼号、房间、用途、状态等查询。 
2、使用情况统计 
可统计查看各单位、房屋的使用情况、利用率，可根据年度、月度不同时间范围进行统计查询。 
3、租赁情况统计 
可按单位、年度、月度统计分析租赁收益情况。 

四、权限管理 
各单位楼宇房屋分权管理，各单位相关管理人员只可管理查看本单位内的房屋，集团总部可管理查看所有信息。 

根据以上方案，用springboot+mysql+vue框架实现上述方案中的所有功能，要求页面好看，功能完整，代码可读性强

---

## 技术栈

- **前端**: Vue 3 + Vite 5 + Element Plus + Pinia + ECharts 5 + Axios + SCSS
- **后端**: Java 17 + Spring Boot 3.2 + MyBatis-Plus 3.5 + MySQL 8.0
- **部署**: Docker + Docker Compose + Nginx

## 项目结构

```
label-01234/
├── README.md                          # 项目说明文档
├── docker-compose.yml                 # Docker Compose 配置
├── .gitignore                         # Git 忽略文件
├── docs/
│   └── project_design.md              # 项目设计文档
└── frontend-admin/                    # 前端项目
    ├── Dockerfile                     # 前端 Docker 构建
    ├── nginx.conf                     # Nginx 配置
    ├── package.json                   # 前端依赖
    ├── vite.config.js                 # Vite 配置
    ├── index.html                     # HTML 入口
    ├── src/
    │   ├── main.js                    # 入口文件
    │   ├── App.vue                    # 根组件
    │   ├── router/                    # 路由配置
    │   ├── stores/                    # Pinia 状态管理
    │   ├── api/                       # API 接口层
    │   ├── layouts/                   # 布局组件
    │   ├── views/                     # 页面组件
    │   ├── components/                # 公共组件
    │   ├── styles/                    # 全局样式
    │   └── utils/                     # 工具函数
    └── backend/                       # 后端项目
        ├── Dockerfile                 # 后端 Docker 构建
        ├── pom.xml                    # Maven 依赖
        └── src/main/
            ├── java/com/building/
            │   ├── config/            # 配置类
            │   ├── common/            # 通用类
            │   ├── security/          # JWT 安全
            │   ├── aspect/            # AOP 切面
            │   ├── entity/            # 实体类
            │   ├── mapper/            # 数据访问层
            │   ├── service/           # 业务逻辑层
            │   ├── dto/               # 数据传输对象
            │   └── controller/        # 控制器层
            └── resources/
                ├── application.yml    # 应用配置
                └── schema.sql         # 数据库初始化脚本
```

## 功能模块

- **数据概览**: 楼宇/房屋/租金/审批核心指标看板，状态分布和用途分布图表
- **楼宇管理**: 楼宇信息 CRUD、状态管理（正常/封存）
- **房屋管理**: 多维度条件查询（单位/楼号/房间/用途/状态）、登记/变更/封存
- **资产配置**: 按房间管理内部资产（家具/电器/办公设备）
- **房屋健康**: 健康检查记录管理（日常巡检/专项检查/安全检查）
- **使用审批**: 内部使用申请与审批流程（办公/宿舍/公寓/教学/病房）
- **对外出租**: 租赁方信息管理、租赁合约管理、终止/续签
- **房屋统计**: 多条件筛选统计，状态/用途分布可视化
- **使用统计**: 按年度/月度统计利用率，审批情况概览
- **租赁统计**: 月度租金趋势、各单位租赁分布、收益分析
- **用户管理**: 用户 CRUD、角色分配、密码重置
- **权限管控**: 基于角色的数据隔离（集团看全部，单位看本级）
