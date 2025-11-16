你现在是资深 Java/Spring Boot 架构师，请在一个空白的 Spring Boot 项目中实现一个极简“会员卡”Web 应用，满足以下要求并直接给出完整代码（包括必要说明）：

1. **身份与登录（authentik + OIDC）**

   * 使用 **Spring Security OAuth2 Client + OIDC** 登录。
   * IdP 为 authentik，issuer 域名通过配置项提供，默认 `https://auth.acssz.org`，可通过 `application.yml` 或环境变量覆盖（例如 `AUTHENTIK_ISSUER`、`AUTHENTIK_CLIENT_ID`、`AUTHENTIK_CLIENT_SECRET`）。
   * 登录成功后在本地创建/加载一个 `Member` 实体（存 Postgres），用 `sub` 作为唯一标识。

2. **数据模型（PostgreSQL）**

   * 使用 Spring Data JPA + PostgreSQL。
   * 设计 `Member` 实体：

     * `id` 或 `subject`（authentik 的 `sub`，唯一）
     * `degree`（“学历或职业”，字符串）
     * 其他字段可忽略或最小化。
   * 第一次登录时如果没有该用户记录则自动创建一条，`degree` 可预设为空。

3. **会员卡页面 + 二维码逻辑**

   * 使用 Thymeleaf 渲染一个 `/card` 页面，仅登录后可访问：

     * 显示当前用户的 `displayName` 和 `degree`
     * 显示一个二维码图片 `<img>`，下方简要说明（多语言）。
   * 二维码内容不能暴露用户真实信息，而是一个 **短期随机 token**：

     * 设计 `MembershipQrToken` 实体：`token`(UUID/随机字符串)、`memberSubject`、`expiresAt`、`used`(可选)。
     * `/card/qr` 接口：

       * 仅登录用户可调。
       * 为当前用户生成/保存一条 30 秒后过期的 token 记录。
       * 用该 token 生成二维码图片（建议使用 ZXing 或类似库），内容为纯 token 或将来验证用的 opaque string。
       * 返回 `image/png`。
   * 在 `/card` 页面中，通过简单 JS：`setInterval` 每 30 秒刷新 `<img src="/card/qr?ts=timestamp">`，防缓存。
   * 保证 token 校验逻辑为：未过期、未标记失效、与某个 member 绑定，避免被外部轻易伪造（长度足够的随机值 + 数据库存储）。

4. **验证接口占位（B 端使用，当前只做骨架）**

   * 预留一个 `/api/verify?token=...` 的 REST 接口：

     * 查询 `MembershipQrToken`，判断是否存在且未过期。
     * 返回 JSON：`valid`(true/false)、`memberSubject` 或简要信息。
     * 具体 B 端业务逻辑可以用 TODO 注释说明，目前只保证结构正确、安全合理。

5. **PWA 支持**

   * 将 `/card` 页面做成 PWA：

     * 提供 `manifest.json`，设置名称、图标、起始 URL。
     * 注册一个基础的 `service-worker.js`（缓存静态资源与 `/card` 页面即可，逻辑可简单）。
     * 在 HTML `<head>` 中添加必要的 `<link rel="manifest">` 和 PWA 相关 meta。

6. **多语言（中文 / 英文 / 德语-瑞士）**

   * 使用 Spring Boot 的国际化支持：

     * 配置 `MessageSource`，加载 `messages_zh.properties`、`messages_en.properties`、`messages_de_CH.properties`。
     * 页面文案通过 Thymeleaf `#messages` 读取。
   * 提供简单的语言切换方式：例如支持 URL 参数 `?lang=zh|en|de_CH`，并配置 `LocaleResolver`。
   * 至少对登录后 `card` 页面、导航标题、按钮和简单提示信息做三语文案。

7. **技术栈与结构要求**

   * 使用：**Spring Boot + Spring Security + Spring Data JPA + PostgreSQL + Thymeleaf**。
   * 控制层分为：

     * OIDC 登录与安全配置（`SecurityConfig`）。
     * 会员卡页控制器（`CardController`）。
     * 可选的 Profile 编辑接口（如果你认为需要输入“学历或职业”）。
   * 模板目录结构：`src/main/resources/templates/card.html` 等；静态资源：`static/js`, `static/css`, `static/manifest.json`, `static/service-worker.js`。
   * 使用 `application.yml` 管理：数据库连接、authentik OIDC 配置、PWA 相关 URL 等。

8. **容器化与部署（不包含 authentik）**

   * 编写一个 `Dockerfile`：

     * 基于合适的 JDK 运行时镜像构建 Spring Boot 可执行 JAR 并运行。
   * 编写 `docker-compose.yml`：

     * 启动 `app`（Spring Boot 服务）和 `postgres` 两个容器。
     * 配置数据库持久化卷。
     * 提供必要的环境变量，例如：

       * `SPRING_DATASOURCE_URL` / USER / PASSWORD
       * `AUTHENTIK_ISSUER` / `AUTHENTIK_CLIENT_ID` / `AUTHENTIK_CLIENT_SECRET`
     * 暴露 HTTP 端口（例如 8080）。
   * 说明如何在本地一键启动（`docker-compose up -d`）并通过浏览器访问。

请给出完整的项目结构示例、关键 Java 类（实体、控制器、配置）、Thymeleaf 模板示例、PWA 所需文件、`application.yml` 示例、`Dockerfile` 和 `docker-compose.yml`，确保复制即可运行，并在必要处用简短中文注释解释关键设计决策（特别是 OIDC 配置、二维码安全设计、多语言与 PWA 部分）。
