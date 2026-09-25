# ViScriptTeam

ViScriptTeam 是一个面向 RPG 地图、PVE 地图和剧情服务器的阵营关系模组。它提供阵营数据、实体阵营归属、玩家对阵营的声望、敌对阵营关系、友伤控制、基础阵营索敌和 nameTag 颜色提示。

当前项目目标不是替代原版队伍系统，而是给地图作者一个更适合“怪物、NPC、势力、玩家关系”的轻量阵营层。

## 适用版本

- Minecraft: `1.21.1`
- NeoForge: `21.1.216`
- Java: `21`
- LDLib2: `2.2.9`
- Mod ID: `viscript_team`

## 核心概念

### 阵营

阵营是实体归属和玩家声望的基础单位。阵营 ID 会自动规范化为小写，例如 `Guards` 会保存为 `guards`。

每个阵营包含：

- 默认声望点数
- 中立阈值
- 友好阈值
- 是否允许友伤
- 是否主动攻击敌对阵营
- 敌对阵营列表

### 实体阵营

实体阵营表示某个实体属于哪个阵营，适合用于怪物、NPC、守卫、刷怪笼生成物、地图机制实体等。

推荐只把 `entity faction` 用在非玩家实体上。玩家当前不使用“阵营归属”表达身份，而是通过“玩家对阵营的声望”表达关系；这样后续可以独立设计玩家专用队伍系统。

### 玩家声望

玩家声望表示某个玩家和某个阵营之间的关系。每个玩家对每个阵营都有一个声望点数。

默认规则：

- 声望低于中立阈值：敌对
- 声望达到中立阈值，但低于友好阈值：中立
- 声望达到或超过友好阈值：友好

默认配置下：

- `< 50` 为敌对
- `50 - 99` 为中立
- `>= 100` 为友好

### 玩家队伍

玩家队伍是独立于阵营的组队系统，适合给任务、副本、剧情事件和脚本逻辑判断“哪些玩家现在是一组”。

每个队伍包含：

- 队伍 ID
- 队长
- 成员列表
- 友伤开关

玩家同一时间只能属于一个队伍。加入新队伍时，会自动离开原队伍。队长离开队伍后，会自动从剩余成员中选择新队长；如果队伍没有成员，会自动移除。

### 敌对阵营

敌对阵营关系是单向的。

如果希望 `guards` 和 `raiders` 互相敌对，需要分别添加两条关系：

```mcfunction
/viscript_team faction enemy add guards raiders
/viscript_team faction enemy add raiders guards
```

## 指令

所有指令都在 `/viscript_team faction` 下，默认需要 2 级权限，也就是开启作弊的单人世界玩家或服务器 OP。

### 阵营管理

创建阵营：

```mcfunction
/viscript_team faction create <id>
```

删除阵营：

```mcfunction
/viscript_team faction delete <id>
```

查看阵营列表：

```mcfunction
/viscript_team faction list
```

查看阵营详情：

```mcfunction
/viscript_team faction info <id>
```

### 敌对关系

添加敌对阵营：

```mcfunction
/viscript_team faction enemy add <faction> <enemy>
```

移除敌对阵营：

```mcfunction
/viscript_team faction enemy remove <faction> <enemy>
```

注意：敌对关系是单向的，`A -> B` 不会自动生成 `B -> A`。

### 实体阵营

设置实体所属阵营：

```mcfunction
/viscript_team faction entity set <targets> <faction>
```

清除实体阵营：

```mcfunction
/viscript_team faction entity clear <targets>
```

查看实体阵营：

```mcfunction
/viscript_team faction entity get <target>
```

示例：

```mcfunction
/viscript_team faction entity set @e[type=minecraft:zombie,limit=1,sort=nearest] raiders
/viscript_team faction entity get @e[type=minecraft:zombie,limit=1,sort=nearest]
```

### 玩家声望

设置玩家对阵营的声望：

```mcfunction
/viscript_team faction standing set <targets> <faction> <points>
```

调整玩家对阵营的声望：

```mcfunction
/viscript_team faction standing add <targets> <faction> <delta>
```

查看玩家对阵营的声望：

```mcfunction
/viscript_team faction standing get <target> <faction>
```

示例：

```mcfunction
/viscript_team faction standing set @p guards 100
/viscript_team faction standing add @p raiders -25
/viscript_team faction standing get @p guards
```

## 队伍界面

所有玩家都可以通过以下指令打开队伍界面，不需要 OP 权限：

```mcfunction
/viscript_team party
/viscript_team party open
```

界面左侧是队伍成员与队伍操作，右侧是队伍聊天。没有队伍时可以创建队伍、浏览现有队伍并申请加入，或者处理其他队长发来的邀请。队长可以邀请在线玩家、处理入队申请、配置队伍友伤、右键队员进行踢出或转让队长，以及解散队伍；普通队员可以主动离队。

入队申请和邀请会随世界存档持久化。队伍聊天只保留当前服务器运行期间最近的 100 条消息，服务器重启或队伍解散后不会保留。

## 队伍后台指令

下面的队伍后台管理指令仍然需要 2 级权限，供管理员、地图作者和命令脚本直接调整队伍数据。

创建队伍并指定队长：

```mcfunction
/viscript_team party create <id> <leader>
```

删除队伍：

```mcfunction
/viscript_team party delete <id>
```

查看队伍列表：

```mcfunction
/viscript_team party list
```

查看队伍详情：

```mcfunction
/viscript_team party info <id>
```

让玩家加入队伍：

```mcfunction
/viscript_team party join <players> <party>
```

让玩家离开当前队伍：

```mcfunction
/viscript_team party leave <players>
```

查看玩家所在队伍：

```mcfunction
/viscript_team party get <player>
```

设置队长：

```mcfunction
/viscript_team party leader set <party> <leader>
```

修改队伍配置：

```mcfunction
/viscript_team party modify <party> friendly_fire <true|false>
```

查看队伍对某个阵营的有效声望：

```mcfunction
/viscript_team party standing get <party> <faction>
/viscript_team party standing get <party> <faction> <min|average|leader|max>
```

默认不写策略时使用 `min`。

### 队伍有效声望

Party 本身不单独保存阵营声望，而是在需要判断阵营态度时根据队伍成员动态计算。默认策略是 `min`，也就是取队伍成员中对该阵营声望最低的值。

例如玩家 A 对 `guards` 为 `100`，玩家 B 对 `guards` 为 `-50`，两人在同一个 party 里时，这个 party 面对 `guards` 的有效声望就是 `-50`。这样可以避免敌对玩家靠加入友好队伍绕过守卫、怪物 AI、副本准入或阵营权限。

内置策略：

| 策略 | 含义 | 建议用途 |
| --- | --- | --- |
| `min` | 队伍成员最低声望，默认策略 | 守卫仇恨、敌对判断、副本准入 |
| `average` | 队伍成员平均声望，向下取整 | 结算展示、软性评价 |
| `leader` | 队长声望 | 剧情入口、任务同步入口 |
| `max` | 队伍成员最高声望 | “队里有人认识该阵营即可触发”的特殊脚本 |

原版/模组实体的目标选择、伤害拦截和 nameTag 颜色都会使用默认的 `min` 有效声望。玩家自己的声望仍然独立保存，队伍只是运行时判断关系时的一层视角。

## 快速示例

下面是一组适合 RPG/PVE 地图开局初始化的示例命令。

创建两个阵营：

```mcfunction
/viscript_team faction create guards
/viscript_team faction create raiders
```

设置双方互相敌对：

```mcfunction
/viscript_team faction enemy add guards raiders
/viscript_team faction enemy add raiders guards
```

生成两个测试实体并设置阵营：

```mcfunction
/summon minecraft:iron_golem ~ ~ ~ {CustomName:'{"text":"Guard"}',CustomNameVisible:1b}
/summon minecraft:zombie ~5 ~ ~ {CustomName:'{"text":"Raider"}',CustomNameVisible:1b}
/viscript_team faction entity set @e[type=minecraft:iron_golem,limit=1,sort=nearest] guards
/viscript_team faction entity set @e[type=minecraft:zombie,limit=1,sort=nearest] raiders
```

设置玩家关系：

```mcfunction
/viscript_team faction standing set @p guards 100
/viscript_team faction standing set @p raiders 0
```

结果：

- 玩家对 `guards` 友好，守卫 nameTag 显示为绿色。
- 玩家对 `raiders` 敌对，袭击者 nameTag 显示为红色。
- 如果实体 AI 允许索敌和攻击，`guards` 和 `raiders` 会按敌对阵营关系互相锁定。

## 行为说明

### nameTag 颜色

客户端会根据当前玩家对实体所属阵营的态度修改 nameTag 颜色；如果玩家在 party 中，则使用该 party 默认 `min` 策略计算出的有效态度：

- 友好：绿色
- 中立：黄色
- 敌对：红色

模组不会强制显示 nameTag。是否显示仍然由原版或其他模组控制，例如 `CustomNameVisible`、实体自带 nameTag 显示规则、地图作者写入的 NBT 等。

### 伤害规则

模组会在伤害事件中拦截不允许的伤害：

- 同队伍玩家之间默认不能互伤，除非该队伍开启友伤。
- 同阵营实体之间默认不能互伤，除非该阵营允许友伤。
- 玩家对某阵营为友好时，玩家和该阵营实体之间默认不能互伤，除非该阵营允许友伤。
- 中立关系不会提供伤害保护，只是不一定会主动索敌。
- 敌对关系允许造成伤害。

这层伤害拦截是兜底逻辑，即使某些 AI 或其他机制绕过了普通目标选择，也会尽量避免友好目标受到伤害。

### 索敌规则

模组会给服务端加载的 `Mob` 注入一个低优先级阵营目标 goal。

基础规则：

- 没有阵营的实体不会额外扫描阵营目标。
- 有阵营的实体会把敌对玩家视为可主动攻击目标。
- 有阵营的实体会把敌对阵营列表中的实体视为可主动攻击目标。
- 同阵营实体不会被主动锁定。
- 如果阵营关闭了主动攻击敌对阵营，则不会主动扫描敌对阵营实体。

这个 goal 是低优先级补充，适合兼容原版和多数使用 goal 系统的模组实体。对于完全自定义 AI 的实体，建议在对应 AI 中单独调用 `FactionApi` 做兼容。

### 数据保存

阵营、实体归属、玩家声望和玩家队伍会保存到世界数据中。删除阵营时，会同时清理实体归属、玩家声望和其他阵营中引用到该阵营的敌对关系。队伍成员变动会保存到同一份世界数据里。

## 配置

配置文件路径：

```text
config/viscript_team_config.toml
```

当前配置项：

| 配置项 | 默认值 | 说明 |
| --- | ---: | --- |
| `defaultFactionPoints` | `50` | 玩家对新建阵营的默认声望点数 |
| `neutralFactionPoints` | `50` | 低于该值为敌对，达到该值后至少为中立 |
| `friendlyFactionPoints` | `100` | 达到或超过该值为友好 |
| `defaultFactionFriendlyFire` | `false` | 新建阵营是否默认允许友伤 |
| `defaultAttackEnemyFactions` | `true` | 新建阵营是否默认主动锁定敌对阵营目标 |

配置只影响新建阵营的默认数据。已经存在的阵营会保存自己的数据，不会因为修改默认配置而自动重写。

## 给地图作者的建议

- 在地图初始化函数或服务器开服脚本中创建固定阵营。
- 敌对关系如果需要互相生效，记得双向添加。
- 生成特殊实体后，立即用 `entity set` 写入阵营。
- 需要 nameTag 颜色提示时，由地图或脚本自行设置实体名称和 `CustomNameVisible`。
- 玩家剧情进度、声望任务、击杀奖励等系统可以通过 `standing set` 和 `standing add` 调整关系。
- 任务同步、副本准入和副本结算可以把 `party` 当作玩家分组单位，任务模组或脚本只需要读取队伍成员。

## 给开发者的接口

代码中提供了 `FactionApi`，可以在其他模组或自定义 AI 中读取和修改阵营关系。

常用能力包括：

- 获取或创建阵营
- 设置实体阵营
- 获取实体阵营
- 设置或调整玩家声望
- 获取玩家或队伍对阵营的有效声望
- 获取玩家队伍
- 判断两个玩家是否同队
- 判断两个实体是否能互相伤害
- 判断某个实体是否应该主动锁定目标

自定义 AI 推荐使用：

```java
FactionApi.shouldActivelyTarget(attacker, target)
FactionApi.canTarget(attacker, target)
FactionApi.canHurt(attacker, target)
FactionApi.getPlayerEffectiveStanding(player, "guards")
FactionApi.getPlayerEffectiveAttitude(player, "guards")
FactionApi.getPartyEffectiveStanding(level, "dungeon_01", "guards").orElse(0)
FactionApi.getPlayerParty(player)
FactionApi.isSameParty(playerA, playerB)
```

### KubeJS 脚本接口

如果安装了 KubeJS，可以直接使用 `ViScriptTeamUtil` 调用阵营逻辑。这个接口走的是命令背后的纯逻辑，不会发送聊天栏反馈。

服务端脚本常用示例：

```js
// server_scripts/example.js

// 创建阵营，已存在时返回 false
ViScriptTeamUtil.createFaction(event.server.overworld(), 'guards')
ViScriptTeamUtil.createFaction(event.server.overworld(), 'raiders')

// 添加单向敌对关系
ViScriptTeamUtil.addEnemyFaction(event.server.overworld(), 'guards', 'raiders')

// 设置实体阵营
ViScriptTeamUtil.setEntityFaction(entity, 'raiders')

// 设置和调整玩家声望
ViScriptTeamUtil.setPlayerStanding(player, 'guards', 100)
ViScriptTeamUtil.addPlayerStanding(player, 'raiders', -25)

// 创建队伍并指定队长
ViScriptTeamUtil.createParty(event.server.overworld(), 'dungeon_01', leader)

// 队伍成员关系
ViScriptTeamUtil.joinParty(player, 'dungeon_01')
ViScriptTeamUtil.leaveParty(player)
const partyId = ViScriptTeamUtil.getPlayerPartyId(player)
const sameParty = ViScriptTeamUtil.isSameParty(playerA, playerB)

// 队伍有效声望。默认策略是 min，也可以传 average / leader / max
const playerEffectiveStanding = ViScriptTeamUtil.getPlayerEffectiveStanding(player, 'guards')
const partyEffectiveStanding = ViScriptTeamUtil.getPartyEffectiveStanding(event.server.overworld(), 'dungeon_01', 'guards')
const leaderStanding = ViScriptTeamUtil.getPartyEffectiveStanding(event.server.overworld(), 'dungeon_01', 'guards', 'leader')

// 判断阵营关系，可用于自定义 AI 或脚本逻辑
if (ViScriptTeamUtil.shouldActivelyTarget(attacker, target)) {
  // 这里写自己的逻辑
}
```

客户端脚本可读取当前玩家视角下的实体态度缓存：

```js
// client_scripts/example.js

const attitude = ViScriptTeamUtil.getEntityAttitude(entity)
```

## 开发与构建

ViScriptTeam 现在作为 [ViScriptLib](https://github.com/zhenshiz/ViScriptLib) 多项目工程的一个子模块开发。**本仓库的代码无法单独构建和启动**：构建脚本依赖主工程统一提供的插件、依赖版本与运行配置，编译期也依赖同工程中的 ViScriptLib 子项目。

请通过主工程获取完整代码：

```bash
# 克隆主工程并同时拉取全部子模块
git clone --recursive https://github.com/zhenshiz/ViScriptLib.git
cd ViScriptLib

# 已克隆过主工程时，可手动拉取/更新子模块
git submodule update --init --recursive
```

常用命令（均在主工程根目录执行，需要 Java 21）：

| 命令 | 作用 |
| --- | --- |
| `./gradlew :ViScriptTeam:build` | 单独构建本模组 |
| `./gradlew :ViScriptTeam:runClient` | 启动客户端调试本模组 |
| `./gradlew buildAll` | 构建所有子项目，产物在主工程根目录 `build/libs` |
| `./gradlew cleanLibs` | 清理所有子项目的构建产物 |

对本仓库的修改需要以子模块的形式提交并推送到本仓库；随后在主工程中再提交一次指向新 commit 的子模块引用。

## 测试建议

发布前建议至少检查：

- 游戏能启动并进入世界。
- `/viscript_team faction list` 可执行。
- 阵营创建、删除、查询正常。
- 实体阵营设置、查询、清除正常。
- 玩家声望设置、增加、查询正常。
- 重进世界后阵营数据仍然存在。
- 友好目标不会受到友伤。
- 敌对目标可以被攻击。
- 敌对阵营实体可以按阵营关系索敌。
- nameTag 颜色在玩家声望变化后能同步刷新。
