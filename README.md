# ⚡ MOGGER MOD — Forge 1.20.1

> *The ultimate aura clash system. MOGGAR or get MOGGED.*

---

## 📦 Setup

### Requirements
- Minecraft **1.20.1**
- Forge **47.2.0+**
- Java **17**

### Build from source
```bash
./gradlew build
```
The `.jar` will be in `build/libs/`. Drop it into your Forge `mods/` folder.

### Run in dev
```bash
./gradlew runClient   # Opens Minecraft with the mod loaded
./gradlew runServer   # Runs a local server
```

---

## 🥊 How It Works

### Starting a Duel
1. Walk within **6 blocks** of any living entity
2. The **`[G] MOGGAR`** button appears on screen (fades in)
3. Press **`G`** to initiate the Mog Duel

### During the Duel (10 seconds)
- Both you and the target **freeze completely**
- You are **forced to look at each other**
- A **tension overlay** fills your screen:
  - Aura bars (yours vs theirs)
  - Countdown timer
  - Dramatic visual effects
- After 10 seconds, **the system compares powers**

### Outcome
| Result | What happens |
|--------|-------------|
| **WIN** | Entity dies instantly — you gain Mog XP |
| **LOSE** | You die instantly — no XP |

---

## 📊 Power System

### Player Power
```
Player Power = Mog Level × 2
```

### Entity Mog Power
```
Entity Mog Power = Max Health × 0.25
```

| Entity | Max HP | Mog Power |
|--------|--------|-----------|
| Chicken | 4 | 1 |
| Sheep | 8 | 2 |
| Zombie | 20 | 5 |
| Enderman | 40 | 10 |
| Iron Golem | 100 | 25 |
| Wither | 300 | 75 |
| Warden | 500 | 125 |

### Random Factor
A **±20% random factor** applies — even a weaker entity has a chance!

---

## 📈 Progression

### Mog XP Reward
```
XP Gained = Entity Mog Power × 2.5
```

### Level Requirements
```
XP to Level N = 50 × (N ^ 1.5)
```

| Level | XP Required | Rank |
|-------|-------------|------|
| 1 | — | Weak Aura |
| 5 | ~559 | Normal |
| 15 | ~2905 | Sigma |
| 30 | ~8216 | Alpha |
| 50 | ~17678 | Mogger |
| 75+ | ~32476 | Supreme Mogger |

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| `G` | Initiate Mog Duel (when near entity) |

---

## 🖥️ HUD

### Bottom-Left Stats Panel
Always visible — shows your current rank, level, Mog XP, and XP bar.

### Moggar Button
Appears when near a valid target. Fades in smoothly, fades out when you walk away.

### Duel Overlay
Full-screen tension overlay during the 10-second duel with:
- Aura bars (player vs enemy)
- Live countdown
- Pulsing tension effects

### Result Flash
On win: gold flash + XP gained + new level  
On lose: red flash + "Your aura was not strong enough..."

---

## 🏗️ Technical Architecture

```
MoggerMod (entry point)
├── common/
│   ├── capability/
│   │   ├── IMoggerData         ← Stores level, XP, duel state
│   │   ├── MoggerDataImpl      ← NBT serialization
│   │   └── MoggerCapability    ← Forge capability registration
│   ├── network/
│   │   ├── MoggerNetwork       ← Channel registration
│   │   ├── PacketStartMog      ← Client → Server: start duel
│   │   ├── PacketDuelState     ← Server → Client: duel active/inactive
│   │   ├── PacketDuelResult    ← Server → Client: win/lose + XP
│   │   └── PacketSyncMogData   ← Server → Client: level/XP sync
│   └── MogDuelManager          ← Server-side duel state tracker
├── event/
│   ├── MoggerEvents            ← Server tick, duel resolution, freeze
│   └── MoggerClientEvents      ← Entity detection, key input
└── client/
    ├── MoggerClientState       ← Client-side state (no server refs)
    ├── MoggerClientSetup       ← Register HUD overlay
    └── overlay/
        └── MoggerHudOverlay    ← Full HUD renderer
```

---

## 🔧 Extending the Mod

### Adding custom entity multipliers
In `IMoggerData.calculateEntityMogPower()`, you can add entity-type checks:
```java
if (entity instanceof EnderDragonEntity) return 200;
```

### Adding more rank titles
Edit `getRankTitle()` in `MoggerHudOverlay` and `IMoggerData`.

### Changing duel duration
Edit `MogDuelManager.DUEL_DURATION_TICKS` (default: 200 = 10 seconds).

---

## 📜 License
MIT — do whatever, just MOGGAR responsibly.
