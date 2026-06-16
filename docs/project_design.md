# 楼宇管理系统 - 项目设计文档

## 1. 系统架构

```mermaid
flowchart TD
    subgraph Frontend["前端 (Vue 3 + Element Plus)"]
        A[浏览器] --> B[Nginx :8081]
        B --> C[Vue SPA]
        C --> D[Pinia Store]
        C --> E[Vue Router]
        C --> F[Axios API Layer]
    end

    subgraph Backend["后端 (Spring Boot 3)"]
        F -->|"/api/*"| G[JWT Filter]
        G --> H[Controller Layer]
        H --> I[Service Layer]
        I --> J[MyBatis-Plus Mapper]
        H --> K[Global Exception Handler]
        I --> L[Operation Log AOP]
    end

    subgraph Database["数据库"]
        J --> M[(MySQL 8.0)]
    end
```

## 2. ER 图

```mermaid
erDiagram
    SYS_DEPARTMENT ||--o{ SYS_USER : "has"
    SYS_DEPARTMENT ||--o{ BUILDING : "owns"
    BUILDING ||--o{ ROOM : "contains"
    ROOM ||--o{ ROOM_ASSET : "has"
    ROOM ||--o{ ROOM_HEALTH : "checked"
    ROOM ||--o{ USAGE_APPROVAL : "applied"
    ROOM ||--o{ RENTAL : "rented"
    RENTAL ||--o{ RENTAL_CONTRACT : "signed"
    SYS_USER ||--o{ OPERATION_LOG : "generates"

    SYS_DEPARTMENT {
        bigint id PK
        varchar name
        varchar code
        bigint parent_id
        int level
        int sort
        tinyint status
    }

    SYS_USER {
        bigint id PK
        varchar username
        varchar password
        varchar real_name
        varchar role
        bigint department_id FK
        tinyint status
    }

    BUILDING {
        bigint id PK
        varchar name
        varchar code
        bigint department_id FK
        varchar address
        int floors
        decimal total_area
        varchar structure_type
        int build_year
        tinyint status
    }

    ROOM {
        bigint id PK
        bigint building_id FK
        bigint department_id FK
        varchar room_number
        int floor
        decimal area
        varchar purpose
        varchar status
    }

    ROOM_ASSET {
        bigint id PK
        bigint room_id FK
        varchar asset_name
        varchar asset_type
        int quantity
        varchar status
        date purchase_date
    }

    ROOM_HEALTH {
        bigint id PK
        bigint room_id FK
        date check_date
        varchar check_type
        varchar result
        varchar description
        varchar handler
        date next_check_date
    }

    USAGE_APPROVAL {
        bigint id PK
        bigint room_id FK
        bigint department_id FK
        varchar applicant
        varchar usage_type
        date start_date
        date end_date
        varchar status
        varchar reason
        varchar approver
        datetime approve_time
    }

    RENTAL {
        bigint id PK
        bigint room_id FK
        varchar tenant_name
        varchar tenant_contact
        varchar tenant_company
        date start_date
        date end_date
        decimal rent_amount
        varchar payment_cycle
        varchar status
    }

    RENTAL_CONTRACT {
        bigint id PK
        bigint rental_id FK
        varchar contract_number
        date sign_date
        date start_date
        date end_date
        decimal total_amount
        varchar status
    }

    OPERATION_LOG {
        bigint id PK
        bigint user_id FK
        varchar module
        varchar action
        varchar target
        text detail
        varchar ip
        datetime created_at
    }
```

## 3. 接口清单

### AuthController `/api/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 登录获取 JWT Token |
| GET | /info | 获取当前用户信息 |

### BuildingController `/api/buildings`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 楼宇列表（分页、条件查询） |
| POST | / | 新增楼宇 |
| PUT | /{id} | 修改楼宇 |
| PUT | /{id}/status | 变更状态 |
| DELETE | /{id} | 删除楼宇 |

### RoomController `/api/rooms`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 房屋列表（多维度查询） |
| POST | / | 房屋登记 |
| PUT | /{id} | 变更修改 |
| PUT | /{id}/status | 变更状态 |
| DELETE | /{id} | 删除房屋 |

### RoomAssetController `/api/room-assets`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 按房间查询资产 |
| POST | / | 新增资产 |
| PUT | /{id} | 修改资产 |
| DELETE | /{id} | 删除资产 |

### RoomHealthController `/api/room-health`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 健康记录列表 |
| POST | / | 新增检查记录 |
| PUT | /{id} | 修改记录 |

### UsageApprovalController `/api/approvals`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 审批列表 |
| POST | / | 提交使用申请 |
| PUT | /{id}/approve | 审批通过 |
| PUT | /{id}/reject | 审批驳回 |

### RentalController `/api/rentals`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 租赁列表 |
| POST | / | 新增租赁 |
| PUT | /{id} | 修改租赁 |
| PUT | /{id}/terminate | 终止租赁 |
| GET | /{id}/contracts | 合约列表 |
| POST | /{id}/contracts | 新增合约 |

### StatisticsController `/api/statistics`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /dashboard | 首页概览数据 |
| GET | /rooms | 房屋信息统计 |
| GET | /usage | 使用情况统计 |
| GET | /rental | 租赁收益统计 |

### SysUserController `/api/users`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 用户列表 |
| POST | / | 新增用户 |
| PUT | /{id} | 修改用户 |
| DELETE | /{id} | 删除用户 |
| PUT | /{id}/reset-password | 重置密码 |

## 4. UI/UX 规范

| 属性 | 值 |
|------|------|
| 主色调 | #1890FF（科技蓝） |
| 成功色 | #52C41A |
| 警告色 | #FAAD14 |
| 危险色 | #FF4D4F |
| 顶部导航高度 | 60px |
| 侧边栏宽度 | 220px / 折叠 64px |
| 内容区背景 | #F0F2F5 |
| 卡片背景 | #FFFFFF |
| 卡片圆角 | 8px |
| 按钮圆角 | 4px |
| 间距体系 | 8px / 16px / 24px |
| 字体 | -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif |
| 基准字号 | 14px |
| 图表库 | ECharts 5.x |
