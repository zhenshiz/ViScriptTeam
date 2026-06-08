# ViScriptTeam

A lightweight faction relationship mod for RPG maps, PVE maps, and story-driven Minecraft servers. It provides faction data, entity faction assignment, per-player reputation, inter-faction hostility, friendly-fire control, faction-aware mob targeting, and color-coded name tags.

This mod is **not** a replacement for the vanilla team system. It is designed as a dedicated faction layer for map makers who need to model relationships between monsters, NPCs, forces, and players.

## Core Concepts

### Factions

A **Faction** is the fundamental unit for entity assignment and player reputation. Faction IDs are automatically normalized to lowercase (e.g. `Guards` becomes `guards`).

Each faction defines:

- **Default reputation points** &mdash; the starting reputation every player has toward this faction.
- **Neutral threshold** &mdash; reputation below this value is considered *Hostile*; at or above this value the player is at least *Neutral*.
- **Friendly threshold** &mdash; reputation at or above this value is considered *Friendly*.
- **Friendly fire toggle** &mdash; whether same-faction entities or friendly players can damage each other.
- **Attack enemy factions toggle** &mdash; whether entities in this faction will actively target members of enemy factions.
- **Enemy faction list** &mdash; a set of faction IDs this faction considers hostile.

Default configuration:

| Range | Attitude |
| --- | --- |
| `< 50` | Hostile |
| `50 &ndash; 99` | Neutral |
| `>= 100` | Friendly |

### Entity Factions

Any non-player entity can be assigned to a faction. This is intended for mobs, NPCs, guards, spawner-spawned creatures, and map-mechanic entities.

Entity faction assignment is stored by UUID, so it persists across entity reloads as long as the entity remains in the world.

Players do **not** use the entity faction system to express identity. Instead, player-faction relationships are expressed through per-player reputation, keeping the system flexible for future party design.

### Player Reputation

Each player maintains an independent reputation score for every faction. The score determines the faction's attitude toward that player:

- **Hostile** &mdash; the faction considers the player an enemy. Entities in this faction will actively target the player and deal damage.
- **Neutral** &mdash; the faction neither attacks nor protects the player.
- **Friendly** &mdash; the faction considers the player an ally. Friendly-fire rules apply, and the player is protected from damage by same-faction entities unless the faction allows friendly fire.

Reputation can be set to an absolute value or adjusted by a delta, making it suitable for quest rewards, kill penalties, story triggers, and progressive reputation systems.

### Parties

Parties are an independent grouping system separate from factions. They are designed for quests, dungeon instances, story events, and script-driven logic that needs to know "which players are currently grouped together."

Each party contains:

- **Party ID** &mdash; a unique, normalized identifier.
- **Leader** &mdash; the player who leads the party. If the leader leaves, the next member is automatically promoted.
- **Member list** &mdash; all players currently in the party.
- **Friendly fire toggle** &mdash; whether party members can damage each other.

A player can only belong to one party at a time. Joining a new party automatically leaves the previous one. Parties with no remaining members are automatically removed.

### Inter-Faction Hostility

Hostility between factions is **unidirectional**. If you want faction A and faction B to be mutually hostile, you must add both directions:

- `A -> B` does **not** automatically create `B -> A`.

This design allows asymmetric relationships, such as a faction that is hostile toward another but is not reciprocated.

## Behavior Details

### Name Tag Colors

The client renders entity name tags with color hints based on the viewing player's effective attitude toward the entity's faction:

| Attitude | Color |
| --- | --- |
| Friendly | Green |
| Neutral | Yellow |
| Hostile | Red |

The mod does **not** force name tags to appear. Visibility is still controlled by vanilla mechanics or other mods (e.g. `CustomNameVisible`, entity name display rules, NBT data set by map makers).

Name tag data is synchronized to clients on login, respawn, dimension change, and whenever faction/reputation data is modified. If the player is in a party, the effective attitude is calculated using the party's standing strategy.

### Damage Rules

The mod intercepts damage events to enforce faction and party rules as a safety net, even when custom AI or other mechanisms bypass normal target selection:

- **Same party** &mdash; party members cannot hurt each other unless the party has friendly fire enabled.
- **Same faction** &mdash; entities in the same faction cannot hurt each other unless the faction allows friendly fire.
- **Player vs. friendly faction** &mdash; when a player has a Friendly attitude toward a faction, neither the player nor the faction's entities can damage each other unless the faction allows friendly fire.
- **Neutral** &mdash; neutral relationships do not provide damage protection, but entities will not actively target neutral players either.
- **Hostile** &mdash; damage is always allowed.

### Mob Targeting

The mod injects a low-priority faction targeting goal into all `Mob` entities on the server side:

- Entities **without** a faction assignment are completely unaffected &mdash; no extra scanning overhead.
- Entities **with** a faction will:
  - Actively target players who are Hostile toward their faction.
  - Actively target entities belonging to enemy factions (if the faction's "attack enemy factions" toggle is enabled).
  - Never target entities in the same faction.
- The targeting goal runs at a low priority, making it compatible with vanilla AI and most modded entities that use the standard goal system.
- A per-tick sanity check clears invalid targets and stops navigation if a mob is somehow targeting a protected entity.

For fully custom AI implementations, the mod provides `FactionApi` methods that can be called directly.

### Party Effective Standing

Parties do **not** store their own faction reputation. Instead, effective standing is calculated dynamically from member reputations when the faction attitude needs to be determined.

The default strategy is `min` &mdash; the lowest reputation among all party members. This prevents a hostile player from joining a friendly party to bypass guard hostility, mob AI, dungeon access, or faction permissions.

Built-in strategies:

| Strategy | Description | Recommended Use |
| --- | --- | --- |
| `min` | Lowest member reputation (default) | Guard hostility, enemy detection, dungeon access control |
| `average` | Average member reputation, rounded down | Settlement displays, soft evaluations |
| `leader` | Leader's reputation only | Story triggers, quest entry points |
| `max` | Highest member reputation | Special scripts where "anyone in the party knows this faction" is sufficient |

All vanilla/modded targeting, damage interception, and name tag colors use the default `min` strategy. Individual player reputations are always stored independently &mdash; the party is merely a runtime lens for evaluating faction relationships.

### Data Persistence

All faction data, entity assignments, player reputations, and party memberships are stored in the world's saved data (attached to the overworld). This means:

- Data persists across server restarts.
- Deleting a faction automatically cleans up all associated entity assignments, player reputations, and enemy faction references in other factions.
- Party member changes are saved immediately.
