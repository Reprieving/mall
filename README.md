# Base Boot - 用户信息管理 Web 工程

本项目基于 **Java 17 (JDK 17)** 与 **Spring Boot 3** 构建，集成了 **Redisson**、**MyBatis-Plus** 与 **MySQL**，提供了高可用、高性能的用户信息管理与认证模块 RESTful 接口。

---

## 🛠️ 一、技术栈与版本

- **运行环境**：Java 17 (LTS)
- **核心框架**：Spring Boot 3.2.5
- **持久层框架**：MyBatis-Plus 3.5.5 (`mybatis-plus-spring-boot3-starter`)
- **Redis 客户端**：Redisson 3.28.0 (`redisson-spring-boot-starter`)
- **数据库**：MySQL 8.x (Connector/J 8.x + HikariCP)
- **认证鉴权**：JWT (`jjwt 0.12.5`) + Redisson 会话管理
- **密码加密**：Spring Security Crypto (BCrypt 哈希算法)
- **数据校验**：Jakarta Validation (`@Valid`, `@Email`, `@NotBlank` 等)
- **效率工具**：Lombok

---

## 📁 二、工程结构目录

```
base-boot/
├── pom.xml                                   # Maven 核心配置 (JDK 17 + Spring Boot 3)
├── README.md                                 # 项目说明与接口文档
├── sql/
│   └── init.sql                              # 数据库建表与初始化测试数据
└── src/
    ├── main/
    │   ├── java/com/example/baseboot/
    │   │   ├── BaseBootApplication.java      # 启动入口类
    │   │   ├── common/                       # 通用公共组件
    │   │   │   ├── annotation/               # 自定义注解 (@LoginRequired, @PassToken)
    │   │   │   ├── api/                      # 统一响应 (CommonResult, ResultCode)
    │   │   │   ├── context/                  # 用户上下文 (UserContext - ThreadLocal)
    │   │   │   ├── exception/                # 业务异常与全局异常拦截 (GlobalExceptionHandler)
    │   │   │   └── utils/                    # 工具类 (JwtUtils, PasswordUtils)
    │   │   ├── config/                       # 核心配置 (MyBatisConfig, RedissonConfig, WebMvcConfig)
    │   │   ├── interceptor/                  # 认证拦截器 (AuthInterceptor)
    │   │   └── module/                       # 业务功能模块
    │   │       ├── auth/                     # 认证模块 (登录、免密登录、发验证码、注册、退出)
    │   │       │   ├── controller/
    │   │       │   ├── dto/
    │   │       │   └── service/
    │   │       └── user/                     # 用户信息模块 (查询、编辑、修改密码)
    │   │           ├── controller/
    │   │           ├── dto/
    │   │           ├── entity/
    │   │           ├── mapper/
    │   │           └── service/
    │   └── resources/
    │       ├── application.yml               # 主配置文件
    │       ├── application-dev.yml           # 开发环境配置 (MySQL、Redis/Redisson连接)
    │       └── mapper/
    │           └── SysUserMapper.xml         # MyBatis SQL 映射文件
    └── test/
        └── java/com/example/baseboot/        # 单元测试
```

---

## 🚀 三、快速启动步骤

### 1. 初始化数据库
执行 `sql/init.sql` 脚本创建数据库 `base_boot` 及 `sys_user` 用户表，并插入预置测试数据：
- 测试账号 1：`admin@example.com` / 密码：`123456`
- 测试账号 2：`test@example.com` / 密码：`123456`

### 2. 配置环境连接
修改 `src/main/resources/application-dev.yml` 中的 MySQL 和 Redis 连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/base_boot?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 你的MySQL密码
  data:
    redis:
      host: localhost
      port: 6379
      password: 你的Redis密码(若无留空)
      database: 0
```

### 3. 运行工程
使用 Maven 编译并启动工程：
```bash
mvn clean package -DskipTests
java -jar target/base-boot-1.0.0-SNAPSHOT.jar
```
或直接在 IntelliJ IDEA 中运行 `BaseBootApplication.java`。

---

## 📑 四、RESTful Web 接口清单与调用示例

### 1. 认证模块 (`/api/auth`)

#### 1.1 邮箱密码登录
- **请求方式**：`POST /api/auth/login`
- **请求体 (JSON)**：
```json
{
  "email": "admin@example.com",
  "password": "123456"
}
```
- **成功响应**：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "email": "admin@example.com",
      "username": "admin",
      "nickname": "系统管理员",
      "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=admin",
      "phone": "13800138000",
      "gender": 1,
      "bio": "热爱编程，致力于写出高质量代码",
      "status": 1,
      "createTime": "2026-08-15 15:00:00",
      "updateTime": "2026-08-15 15:00:00"
    }
  },
  "timestamp": 1786778400000
}
```

#### 1.2 发送邮箱验证码
- **请求方式**：`POST /api/auth/send-code`
- **请求体 (JSON)**：
```json
{
  "email": "test@example.com"
}
```
- **成功响应**（验证码有效期 5 分钟，测试阶段接口直接返回 `code`）：
```json
{
  "code": 200,
  "message": "验证码发送成功（5分钟内有效）",
  "data": {
    "email": "test@example.com",
    "code": "839201"
  },
  "timestamp": 1786778400000
}
```

#### 1.3 邮箱验证码免密登录
- **请求方式**：`POST /api/auth/login-code`
- **说明**：如果该邮箱用户不存在，会自动创建账号并完成登录。
- **请求体 (JSON)**：
```json
{
  "email": "test@example.com",
  "code": "839201"
}
```

#### 1.4 新用户注册
- **请求方式**：`POST /api/auth/register`
- **请求体 (JSON)**：
```json
{
  "email": "newuser@example.com",
  "username": "newuser",
  "password": "Password123",
  "nickname": "新注册用户"
}
```

#### 1.5 退出登录
- **请求方式**：`POST /api/auth/logout`
- **请求头**：`Authorization: Bearer <token>`
- **成功响应**：
```json
{
  "code": 200,
  "message": "退出登录成功",
  "data": null,
  "timestamp": 1786778400000
}
```

---

### 2. 用户信息模块 (`/api/user`)
> ⚠️ **注意**：该模块所有接口均需在 HTTP 请求头中携带 Token：
> `Authorization: Bearer <your_jwt_token>`

#### 2.1 查询当前登录用户信息
- **请求方式**：`GET /api/user/info`
- **请求头**：`Authorization: Bearer <token>`
- **成功响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "email": "admin@example.com",
    "username": "admin",
    "nickname": "系统管理员",
    "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=admin",
    "phone": "13800138000",
    "gender": 1,
    "bio": "热爱编程，致力于写出高质量代码",
    "status": 1,
    "createTime": "2026-08-15 15:00:00",
    "updateTime": "2026-08-15 15:00:00"
  },
  "timestamp": 1786778400000
}
```

#### 2.2 编辑当前登录用户个人资料
- **请求方式**：`PUT /api/user/update`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "nickname": "管理员-极客版",
  "avatar": "https://api.dicebear.com/7.x/avataaars/svg?seed=admin-pro",
  "phone": "13888888888",
  "gender": 1,
  "bio": "全栈工程师，持续精进"
}
```
- **成功响应**：返回修改后的最新用户信息脱敏对象。

#### 2.3 修改当前登录用户密码
- **请求方式**：`PUT /api/user/update-password`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "oldPassword": "123456",
  "newPassword": "NewPassword@2026"
}
```

#### 2.4 根据用户 ID 查询指定用户
- **请求方式**：`GET /api/user/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **路径参数**：`id` (用户ID)

#### 2.5 查询所有用户列表
- **请求方式**：`GET /api/user/list`
- **请求头**：`Authorization: Bearer <token>`

---

### 3. 用户多主体实名认证模块 (`/api/user/cert`)
> ⚠️ **注意**：该模块接口均需在 HTTP 请求头中携带 Token：
> `Authorization: Bearer <your_jwt_token>`

#### 3.1 提交个人实名认证
- **请求方式**：`POST /api/user/cert/personal`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "realName": "张伟",
  "idCard": "110101199003072391",
  "idCardFrontPic": "https://example.com/idcard/front.jpg",
  "idCardBackPic": "https://example.com/idcard/back.jpg"
}
```

#### 3.2 提交个体工商户认证
- **请求方式**：`POST /api/user/cert/individual`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "realName": "张伟",
  "idCard": "110101199003072391",
  "idCardFrontPic": "https://example.com/idcard/front.jpg",
  "idCardBackPic": "https://example.com/idcard/back.jpg",
  "companyName": "海淀区中关村极光便利店",
  "businessLicenseNo": "92110108MA01XXXX12",
  "businessLicensePic": "https://example.com/cert/license_individual.jpg",
  "companyAddress": "北京市海淀区中关村南大街1号"
}
```

#### 3.3 提交企业实名认证
- **请求方式**：`POST /api/user/cert/enterprise`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "realName": "张伟",
  "idCard": "110101199003072391",
  "idCardFrontPic": "https://example.com/idcard/front.jpg",
  "idCardBackPic": "https://example.com/idcard/back.jpg",
  "companyName": "北京极光科技有限公司",
  "businessLicenseNo": "91110108MA01ABCD12",
  "businessLicensePic": "https://example.com/cert/license_enterprise.jpg",
  "companyAddress": "北京市海淀区中关村南大街1号院8号楼"
}
```

#### 3.4 查询当前用户的实名认证状态及详情
- **请求方式**：`GET /api/user/cert/my`
- **请求头**：`Authorization: Bearer <token>`

#### 3.5 审核实名认证申请 (平台管理端)
- **请求方式**：`POST /api/user/cert/audit`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "id": 1,
  "status": 1,
  "auditRemark": "审核通过"
}
```

#### 3.6 根据用户 ID 查询认证信息 (管理端)
- **请求方式**：`GET /api/user/cert/{userId}`
- **请求头**：`Authorization: Bearer <token>`

#### 3.7 分页查询认证审核列表 (管理端)
- **请求方式**：`GET /api/user/cert/page?certType=3&status=0&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <token>`

---

### 4. 用户收货地址模块 (`/api/user/address`)

#### 4.1 新增收货地址
- **请求方式**：`POST /api/user/address`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "name": "张伟",
  "phone": "13800138000",
  "province": "北京市",
  "city": "北京市",
  "district": "海淀区",
  "detailAddress": "中关村南大街1号院8号楼",
  "postalCode": "100086",
  "isDefault": 1,
  "tag": "公司"
}
```

#### 4.2 修改收货地址
- **请求方式**：`PUT /api/user/address/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 4.3 删除收货地址
- **请求方式**：`DELETE /api/user/address/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 4.4 获取单个收货地址详情
- **请求方式**：`GET /api/user/address/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 4.5 获取当前用户所有收货地址列表 (默认地址自动置顶)
- **请求方式**：`GET /api/user/address/list`
- **请求头**：`Authorization: Bearer <token>`

#### 4.6 获取当前用户的默认收货地址
- **请求方式**：`GET /api/user/address/default`
- **请求头**：`Authorization: Bearer <token>`

#### 4.7 设置默认收货地址
- **请求方式**：`PUT /api/user/address/{id}/default`
- **请求头**：`Authorization: Bearer <token>`

---

### 5. 商品分类模块 (`/api/category`)

#### 4.1 获取分类树形结构
- **请求方式**：`GET /api/category/tree`
- **说明**：获取全量层级分类树（一级、二级、三级分类级联嵌套），用于前端商城导航栏或后台级联选择器。

#### 4.2 获取分类平铺列表
- **请求方式**：`GET /api/category/list?parentId=1&status=1`
- **Query 参数**：`parentId` (父分类ID), `status` (状态), `name` (模糊名称)

#### 4.3 获取分类详情
- **请求方式**：`GET /api/category/{id}`

#### 4.4 创建商品分类
- **请求方式**：`POST /api/category`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "parentId": 0,
  "name": "数码电子",
  "level": 1,
  "icon": "https://example.com/icons/digital.png",
  "sort": 1,
  "status": 1
}
```

#### 4.5 修改商品分类
- **请求方式**：`PUT /api/category/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 4.6 删除商品分类
- **请求方式**：`DELETE /api/category/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：若分类下存在子分类或已关联商品 SPU，将拦截并提示无法删除。

#### 4.7 启禁用商品分类
- **请求方式**：`PUT /api/category/{id}/status?status=0`
- **请求头**：`Authorization: Bearer <token>`

---

### 5. 商品品牌模块 (`/api/brand`)

#### 5.1 分页查询品牌列表
- **请求方式**：`GET /api/brand/page?name=Apple&firstLetter=A&pageNum=1&pageSize=10`
- **Query 参数**：`name` (品牌名搜索), `firstLetter` (首字母), `status` (状态), `pageNum`, `pageSize`

#### 5.2 获取所有启用的品牌列表
- **请求方式**：`GET /api/brand/list-all`
- **说明**：获取所有状态为启用的品牌列表，供发布商品时下拉选择。

#### 5.3 获取品牌详情
- **请求方式**：`GET /api/brand/{id}`

#### 5.4 创建商品品牌
- **请求方式**：`POST /api/brand`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "name": "Apple / 苹果",
  "logo": "https://example.com/brand/apple.png",
  "description": "Apple 设计制造的电子消费品",
  "firstLetter": "A",
  "sort": 1,
  "status": 1
}
```

#### 5.5 修改商品品牌
- **请求方式**：`PUT /api/brand/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 5.6 删除商品品牌
- **请求方式**：`DELETE /api/brand/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 5.7 启禁用商品品牌
- **请求方式**：`PUT /api/brand/{id}/status?status=1`
- **请求头**：`Authorization: Bearer <token>`

---

### 6. 商品规格管理模块 (`/api/spec`)

#### 6.1 创建规格项 (支持附带初始规格值)
- **请求方式**：`POST /api/spec/key`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "categoryId": 3,
  "name": "机身颜色",
  "sort": 1,
  "status": 1,
  "initialValues": ["原色钛金属", "暗夜黑", "白色钛金属"]
}
```

#### 6.2 修改规格项
- **请求方式**：`PUT /api/spec/key/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 6.3 删除规格项
- **请求方式**：`DELETE /api/spec/key/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：若规格项已被商品 SPU 或 SKU 引用，系统将拦截并报错。

#### 6.4 获取规格项详情
- **请求方式**：`GET /api/spec/key/{id}`
- **说明**：返回规格项基础信息及旗下所有规格值列表。

#### 6.5 分页查询规格项列表
- **请求方式**：`GET /api/spec/key/page?categoryId=3&name=颜色&pageNum=1&pageSize=10`

#### 6.6 按分类查询可用规格模板
- **请求方式**：`GET /api/spec/category/{categoryId}`
- **说明**：查询指定分类及通用规格（`categoryId=0`）下所有启用的规格项和可选值列表，供后台发布 SPU 时快速选用。

#### 6.7 新增单个规格值
- **请求方式**：`POST /api/spec/value`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "specKeyId": 1,
  "value": "远峰蓝",
  "sort": 4,
  "status": 1
}
```

#### 6.8 批量新增规格值
- **请求方式**：`POST /api/spec/value/batch`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "specKeyId": 2,
  "values": ["512GB", "1TB"]
}
```

#### 6.9 修改规格值
- **请求方式**：`PUT /api/spec/value/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 6.10 删除规格值
- **请求方式**：`DELETE /api/spec/value/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：若已被 SKU 引用，系统拦截并报错。

#### 6.11 查询指定规格项下的所有规格值
- **请求方式**：`GET /api/spec/key/{keyId}/values`

---

### 7. 商品 SPU 模块 (`/api/spu`)

#### 7.1 创建 SPU 商品 (多规格多表关联录入与店铺绑定)
- **请求方式**：`POST /api/spu`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：支持单规格与多规格商品录入。绑定所属店铺（`shopId`），录入时自动校验店铺有效性、规格项与规格值，在同一事务中落库主表及 `pms_spu_spec_relation`、`pms_sku_spec_value` 关系表，并自动计算回写 `minPrice`、`maxPrice` 与 `totalStock`。
- **请求体示例 (JSON - 多规格商品)**：
```json
{
  "shopId": 1,
  "name": "Apple iPhone 15 Pro",
  "spuCode": "SPU-IPHONE15P",
  "categoryId": 3,
  "brandId": 1,
  "title": "钛金属设计，A17 Pro 芯片，4800万像素主摄",
  "description": "<p>iPhone 15 Pro 全新登场，强悍性能与钛金属机身。</p>",
  "mainPic": "https://example.com/product/iphone15pro/main.jpg",
  "sliderPics": "[\"https://example.com/product/iphone15pro/1.jpg\"]",
  "specType": 1,
  "unit": "台",
  "status": 1,
  "sort": 1,
  "specList": [
    {
      "specKeyId": 1,
      "specName": "机身颜色",
      "specValueIds": [1, 2]
    },
    {
      "specKeyId": 2,
      "specName": "存储容量",
      "specValueIds": [3, 4]
    }
  ],
  "skuList": [
    {
      "skuCode": "SKU-IP15P-TIT-128",
      "name": "Apple iPhone 15 Pro 原色钛金属 128GB",
      "pic": "https://example.com/product/iphone15pro/tit.jpg",
      "price": 7999.00,
      "originalPrice": 8999.00,
      "costPrice": 6500.00,
      "stock": 50,
      "weight": 0.18,
      "volume": 0.001,
      "specValues": [
        { "specKeyId": 1, "specValueId": 1 },
        { "specKeyId": 2, "specValueId": 3 }
      ],
      "status": 1
    },
    {
      "skuCode": "SKU-IP15P-TIT-256",
      "name": "Apple iPhone 15 Pro 原色钛金属 256GB",
      "pic": "https://example.com/product/iphone15pro/tit.jpg",
      "price": 8999.00,
      "originalPrice": 9999.00,
      "costPrice": 7500.00,
      "stock": 50,
      "weight": 0.18,
      "volume": 0.001,
      "specValues": [
        { "specKeyId": 1, "specValueId": 1 },
        { "specKeyId": 2, "specValueId": 4 }
      ],
      "status": 1
    }
  ]
}
```

#### 7.2 修改 SPU 商品
- **请求方式**：`PUT /api/spu/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 7.3 删除 SPU 商品
- **请求方式**：`DELETE /api/spu/{id}`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：级联删除 SPU、`pms_spu_spec_relation`、旗下所有 SKU 及其 `pms_sku_spec_value` 绑定。

#### 7.4 查询 SPU 完整详情
- **请求方式**：`GET /api/spu/{id}`
- **说明**：返回 SPU 基础信息、所属店铺详情 (`shop`) 与店铺名 (`shopName`)、分类名、品牌名、选用的规格维度及全量 SKU 绑定的结构化规格明细。

#### 7.5 分页查询 SPU 列表
- **请求方式**：`GET /api/spu/page?shopId=1&keyword=iPhone&categoryId=3&brandId=1&status=1&pageNum=1&pageSize=10`
- **说明**：支持按所属店铺 `shopId` 精确筛选，返回结果包含每个商品对应的 `shopName`。

#### 7.6 修改商品上下架状态
- **请求方式**：`PUT /api/spu/{id}/status?status=1`
- **请求头**：`Authorization: Bearer <token>`

#### 7.7 批量修改商品上下架状态
- **请求方式**：`PUT /api/spu/batch/status`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "ids": [1, 2, 3],
  "status": 1
}
```

---

### 8. 商品 SKU 模块 (`/api/sku`)

#### 8.1 为指定 SPU 新增单个 SKU
- **请求方式**：`POST /api/sku/spu/{spuId}`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：新增并落库 `pms_sku_spec_value`，自动重新汇总同步 SPU 的总库存与最低/最高价格。

#### 8.2 修改单个 SKU 信息
- **请求方式**：`PUT /api/sku/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 8.3 删除单个 SKU
- **请求方式**：`DELETE /api/sku/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 8.4 查询单个 SKU 详情
- **请求方式**：`GET /api/sku/{id}`
- **说明**：包含该 SKU 绑定的所有规格明细列表 (`specValues`)。

#### 8.5 查询指定 SPU 下的所有 SKU 列表
- **请求方式**：`GET /api/sku/spu/{spuId}`

#### 8.6 单独调整 SKU 库存
- **请求方式**：`PUT /api/sku/{id}/stock`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "stock": 100
}
```

#### 8.7 单独调整 SKU 价格
- **请求方式**：`PUT /api/sku/{id}/price`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "price": 7699.00,
  "originalPrice": 8699.00,
  "costPrice": 6200.00
}
```

#### 8.8 启用/禁用单个 SKU
- **请求方式**：`PUT /api/sku/{id}/status?status=1`
- **请求头**：`Authorization: Bearer <token>`

---

### 9. 用户端订单模块 (`/api/order`)

#### 9.1 订单结算预览 (核算金额与运费)
- **请求方式**：`POST /api/order/preview`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "receiverProvince": "北京市",
  "items": [
    { "skuId": 1, "quantity": 1 }
  ]
}
```

#### 9.2 创建并提交订单 (分布式锁防超卖、扣减库存、固化商品快照)
- **请求方式**：`POST /api/order/create`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "receiverName": "张伟",
  "receiverPhone": "13800138000",
  "receiverProvince": "北京市",
  "receiverCity": "北京市",
  "receiverDistrict": "海淀区",
  "receiverDetailAddress": "中关村南大街1号院8号楼",
  "note": "请尽快安排发货",
  "payType": 2,
  "items": [
    { "skuId": 1, "quantity": 1 }
  ]
}
```

#### 9.3 订单支付 (模拟/发起支付)
- **请求方式**：`POST /api/order/{id}/pay`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "payType": 2,
  "tradeNo": "WX20260829153000999"
}
```

#### 9.4 用户取消订单 (未支付订单取消并自动安全回滚库存)
- **请求方式**：`POST /api/order/{id}/cancel`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "cancelReason": "不想要了/重新选购"
}
```

#### 9.5 用户确认收货 (已发货 -> 已完成)
- **请求方式**：`POST /api/order/{id}/receive`
- **请求头**：`Authorization: Bearer <token>`

#### 9.6 用户删除订单 (逻辑删除已完成/已取消的订单)
- **请求方式**：`DELETE /api/order/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 9.7 获取当前用户订单详情
- **请求方式**：`GET /api/order/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 9.8 分页查询我的订单列表
- **请求方式**：`GET /api/order/my-page?status=0&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <token>`
- **Query 参数**：`status` (0-待付款, 1-待发货, 2-已发货, 3-已完成, 4-已取消, 5-已关闭), `orderSn`, `pageNum`, `pageSize`

---

### 10. 管理端订单履约与运营模块 (`/api/order/admin`)

#### 10.1 多条件分页检索订单列表
- **请求方式**：`GET /api/order/admin/page?status=1&orderSn=ORD&receiverName=张伟&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <token>`
- **Query 参数**：`orderSn`, `status`, `userId`, `receiverName`, `receiverPhone`, `startTime`, `endTime`, `pageNum`, `pageSize`

#### 10.2 查询订单全流程履约详情
- **请求方式**：`GET /api/order/admin/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 10.3 订单发货 (录入物流公司与单号，流转为已发货)
- **请求方式**：`POST /api/order/admin/{id}/delivery`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "deliveryCompany": "顺丰速运",
  "deliverySn": "SF139829381023"
}
```

#### 10.4 修改收货人信息 (待发货前)
- **请求方式**：`PUT /api/order/admin/{id}/receiver`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "receiverName": "李四",
  "receiverPhone": "13900139000",
  "receiverProvince": "上海市",
  "receiverCity": "上海市",
  "receiverDistrict": "浦东新区",
  "receiverDetailAddress": "世纪大道100号环球金融中心"
}
```

#### 10.5 后台强制取消订单 (释放库存)
- **请求方式**：`POST /api/order/admin/{id}/cancel`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "cancelReason": "买家联系客服申请退款取消"
}
```

#### 10.6 后台关闭/售后退款订单 (释放库存)
- **请求方式**：`POST /api/order/admin/{id}/close?reason=已完成退款售后`
- **请求头**：`Authorization: Bearer <token>`

---

### 11. 店铺中心模块 (`/api/shop` 与 `/api/shop/admin`)

#### 11.1 申请开店入驻 (店主端)
- **请求方式**：`POST /api/shop/apply`
- **请求头**：`Authorization: Bearer <token>`
- **说明**：开店前需先完成主体认证。开店类型与认证资质强绑定（个人店/个体户店/企业旗舰店/专营店）。
- **请求体 (JSON)**：
```json
{
  "name": "极光数码官方旗舰店",
  "logo": "https://example.com/shop/logo.png",
  "banner": "https://example.com/shop/banner.png",
  "intro": "极光数码官方自营旗舰店，正品行货，全国联保。",
  "notice": "全场新品火热发售中，支持顺丰次日达！",
  "phone": "400-888-9999",
  "type": 3
}
```

#### 11.2 查询我的店铺信息 (店主端)
- **请求方式**：`GET /api/shop/my`
- **请求头**：`Authorization: Bearer <token>`

#### 11.3 修改店铺基本信息 (店主端)
- **请求方式**：`PUT /api/shop/my`
- **请求头**：`Authorization: Bearer <token>`

#### 11.4 切换店铺营业状态 (店主端)
- **请求方式**：`PUT /api/shop/my/status?status=2`
- **请求头**：`Authorization: Bearer <token>`
- **Query 参数**：`status` (1-正常营业, 2-暂停营业/打烊)

#### 11.5 查询店铺公开主页信息 (商城前台)
- **请求方式**：`GET /api/shop/{id}`

#### 11.6 分页浏览店铺列表 (商城前台)
- **请求方式**：`GET /api/shop/page?name=极光&type=3&pageNum=1&pageSize=10`

#### 11.7 多条件分页检索店铺列表 (平台管理端)
- **请求方式**：`GET /api/shop/admin/page?name=旗舰店&status=0&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <token>`

#### 11.8 查询店铺全量资质与审核详情 (平台管理端)
- **请求方式**：`GET /api/shop/admin/{id}`
- **请求头**：`Authorization: Bearer <token>`

#### 11.9 审核开店申请 (平台管理端)
- **请求方式**：`POST /api/shop/admin/audit`
- **请求头**：`Authorization: Bearer <token>`
- **请求体 (JSON)**：
```json
{
  "id": 1,
  "status": 1,
  "rejectReason": null
}
```

#### 11.10 管控店铺状态 (平台管理端)
- **请求方式**：`PUT /api/shop/admin/{id}/status?status=4`
- **请求头**：`Authorization: Bearer <token>`
- **Query 参数**：`status` (0-待审核, 1-正常营业, 2-打烊, 3-审核驳回, 4-违规封禁)

---

### 12. 运营管理员认证与 RBAC 权限体系 (`/api/admin/auth` & `/api/admin/sys`)
> ⚠️ **注意**：管理端接口需携带管理员专属 Token：`Authorization: Bearer <admin_jwt_token>`

#### 12.1 运营管理员登录
- **请求方式**：`POST /api/admin/auth/login`
- **请求体 (JSON)**：
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **响应数据**：包含管理员基本信息、所属角色与权限标识列表 (`permissions: ["*:*:*"]`)。

#### 12.2 获取当前登录管理员信息与权限清单
- **请求方式**：`GET /api/admin/auth/info`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 12.3 退出登录
- **请求方式**：`POST /api/admin/auth/logout`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 12.4 管理员账号分页列表
- **请求方式**：`GET /api/admin/sys/user/page?keyword=admin&roleId=1&status=1&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 12.5 新增管理员账号
- **请求方式**：`POST /api/admin/sys/user`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "username": "ops_zhang",
  "password": "Password@2026",
  "nickname": "张运营",
  "avatar": "https://example.com/avatar/ops.png",
  "email": "zhang@mall.com",
  "phone": "13900000000",
  "roleId": 2,
  "status": 1
}
```

#### 12.6 角色列表与权限树查询
- **请求方式**：`GET /api/admin/sys/role/list`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 12.7 新增/编辑角色
- **请求方式**：`POST /api/admin/sys/role` / `PUT /api/admin/sys/role/{id}`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "name": "商品与订单专员",
  "code": "PRODUCT_ORDER_OPS",
  "description": "负责商品审核与日常订单发货",
  "permissions": ["spu:view", "spu:audit", "order:view", "order:deliver", "order:remark"],
  "status": 1
}
```

---

### 13. 运营数据大屏与统计看板 (`/api/admin/dashboard`)

#### 13.1 今日核心指标概览
- **请求方式**：`GET /api/admin/dashboard/overview`
- **请求头**：`Authorization: Bearer <admin_token>`
- **返回字段**：`todayGmv` (今日成交额), `todayPayAmount` (今日实付金额), `todayOrderCount` (今日订单数), `todayNewUsers` (今日新增买家), `todayAov` (客单价), `totalUsers`, `totalShops`, `totalProducts`, `totalOrders`, `totalGmv`。

#### 13.2 待办事项角标汇总
- **请求方式**：`GET /api/admin/dashboard/todos`
- **请求头**：`Authorization: Bearer <admin_token>`
- **返回字段**：`pendingCertCount` (待审核主体认证), `pendingShopCount` (待审核开店), `pendingDeliverCount` (待发货订单), `lowStockProductCount` (库存告急商品数)。

#### 13.3 销售与订单量走势图
- **请求方式**：`GET /api/admin/dashboard/trend?days=7`
- **请求头**：`Authorization: Bearer <admin_token>`
- **返回字段**：`dates` (日期数组), `gmvList` (每日销售额数组), `orderCountList` (每日订单量数组)。

#### 13.4 热销商品榜单 Top 10
- **请求方式**：`GET /api/admin/dashboard/top-products?limit=10`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 13.5 各品类销售占比
- **请求方式**：`GET /api/admin/dashboard/category-ratio`
- **请求头**：`Authorization: Bearer <admin_token>`

---

### 14. 买家用户运营中台 (`/api/admin/user`)

#### 14.1 买家用户多维分页检索
- **请求方式**：`GET /api/admin/user/page?keyword=zhang&status=1&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 14.2 买家全景画像档案详情
- **请求方式**：`GET /api/admin/user/{id}/detail`
- **请求头**：`Authorization: Bearer <admin_token>`
- **返回包含**：用户基本资料、累计订单数、累计消费总金额、主体实名认证详情、关联店铺详情、全量收货地址列表。

#### 14.3 买家账号封禁 / 解封
- **请求方式**：`PUT /api/admin/user/{id}/status?status=0`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 14.4 运营端重置买家密码
- **请求方式**：`PUT /api/admin/user/{id}/reset-pwd?password=NewPassword@2026`
- **请求头**：`Authorization: Bearer <admin_token>`

---

### 15. 店铺与实名认证批量管控 (`/api/admin/cert` & `/api/admin/shop`)

#### 15.1 主体实名认证批量审核
- **请求方式**：`POST /api/admin/cert/batch-audit`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "ids": [1, 2, 3],
  "status": 1,
  "auditRemark": "批量审核通过"
}
```

#### 15.2 批量调整店铺营业/封禁状态
- **请求方式**：`PUT /api/admin/shop/batch-status`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "ids": [1, 2],
  "status": 4
}
```

#### 15.3 人工修正店铺综合评分
- **请求方式**：`PUT /api/admin/shop/{id}/score?score=4.98`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 15.4 查看店铺全量全景详情
- **请求方式**：`GET /api/admin/shop/{id}/full-detail`
- **请求头**：`Authorization: Bearer <admin_token>`

---

### 16. 商品运营与库存预警 (`/api/admin/spu`)

#### 16.1 全平台商品高级跨店铺检索
- **请求方式**：`GET /api/admin/spu/page?keyword=iPhone&shopId=1&lowStock=true&lowStockThreshold=10&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 16.2 批量下架 / 删除违规商品
- **请求方式**：`DELETE /api/admin/spu/batch`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "ids": [1, 2, 3]
}
```

#### 16.3 批量调整商品分类归属
- **请求方式**：`PUT /api/admin/spu/batch/category`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "ids": [1, 2, 3],
  "targetCategoryId": 3
}
```

#### 16.4 库存告急预警列表
- **请求方式**：`GET /api/admin/spu/stock-warning?threshold=10&limit=50`
- **请求头**：`Authorization: Bearer <admin_token>`

---

### 17. 订单综合调度与流转日志/插旗 (`/api/admin/order`)

#### 17.1 全平台跨店铺订单多维高级检索
- **请求方式**：`GET /api/admin/order/page?orderSn=ORD&receiverPhone=138&status=1&adminFlag=1&pageNum=1&pageSize=10`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 17.2 获取全量订单详情 (含买家、店铺、快照与时间轴)
- **请求方式**：`GET /api/admin/order/{id}/detail`
- **请求头**：`Authorization: Bearer <admin_token>`

#### 17.3 运营订单插旗标色与内部备忘录备注
- **请求方式**：`PUT /api/admin/order/{id}/remark`
- **请求头**：`Authorization: Bearer <admin_token>`
- **请求体 (JSON)**：
```json
{
  "adminFlag": 1,
  "adminRemark": "买家要求指定顺丰次日达，已联系仓库优先打包"
}
```
> `adminFlag` 取值说明：`0-无, 1-红旗, 2-黄旗, 3-绿旗, 4-蓝旗, 5-紫旗`

#### 17.4 查询订单生命周期流转轨迹日志
- **请求方式**：`GET /api/admin/order/{id}/logs`
- **请求头**：`Authorization: Bearer <admin_token>`






