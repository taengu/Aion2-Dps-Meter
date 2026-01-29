# AION2meter-TW

A combat analysis (DPS meter) tool for **AION 2**. Lovingly forked from [Aion2-Dps-Meter](https://github.com/TK-open-public/Aion2-Dps-Meter)

🔗 **GitHub Repository:** https://github.com/taengu/Aion2-Dps-Meter  
💬 **Discord (Support & Community): https://discord.gg/Aion2Global**

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub Issues](https://img.shields.io/github/issues/taengu/Aion2-Dps-Meter)](https://github.com/taengu/Aion2-Dps-Meter/issues)
[![GitHub Pull Requests](https://img.shields.io/github/issues-pr/taengu/Aion2-Dps-Meter)](https://github.com/taengu/Aion2-Dps-Meter/pulls)

> **Important Notice**  
> This project will be **paused or made private** if requested by the game operator, if packet encryption or other countermeasures are introduced, or if there is an official statement prohibiting its use.

---

## Usage

1. Install **Npcap**:  
   https://npcap.com/#download  
   - You **must** check **“Install Npcap in WinPcap API-compatible Mode”**

2. Download the latest release:  
   👉 https://github.com/taengu/Aion2-Dps-Meter/releases

3. If AION 2 is already running, **go to the character selection screen first**.

4. Run `aion2meter4j.exe` **as Administrator**.

5. If the UI appears, the application has started successfully.

6. If the DPS meter does not appear:
   - Teleport using a **Kisk**, **Hideout**, or enter/exit a dungeon
   - Then repeat steps **3–4**

7. If the meter stops working after previously functioning:
   - Teleport or enter a dungeon again to refresh packet capture
   - If it still does not work, restart from step **3**

---

## UI Explanation

- **Blue box** – Monster name display (planned)
- **Brown box** – Reset current combat data
- **Pink box** – Expand / collapse DPS meter
- **Red box** – Class icon (shown when detected)
- **Orange box** – Player nickname (click for details)
- **Light blue box** – DPS for current target
- **Purple box** – Contribution percentage
- **Green box** – Combat timer  
  - Green: in combat  
  - Yellow: no damage detected (paused)  
  - Grey: combat ended

Clicking a player row opens detailed statistics.

> **Hit count** refers to **successful hits**, not skill casts.


## Build Instructions
> ⚠️ **Regular users do NOT need to build the project.**  
> This section is for developers only.

```bash
# Clone the repository
git clone https://github.com/taengu/Aion2-Dps-Meter.git

# Enter the directory
cd Aion2-Dps-Meter

# Build the distribution (Windows)
./gradlew packageDistributionForCurrentOS
```



---

## FAQ

**Q: What's different from the original meter?**
- The original was written for KR servers and uses a hard-coded method for finding game packets.
- This version adds auto-detection and support for VPNs/Ping Reducers. It also has been translated to English skills/spells and UI.


**Q: The UI appears, but no damage is shown.**  
- Verify Npcap installation  
- Exit the app, go to character select, then relaunch

**Q: I see DPS from others but not myself.**  
- DPS is calculated based on the monster with the highest total damage  
- Use the same training dummy

**Q: Contribution is not 100% while solo.**  
- Name capture may have failed

**Q: Are chat or command features supported?**  
- Not currently

**Q: Hit count is higher than skill casts.**  
- Multi-hit skills count each hit separately

**Q: Some skills show as numbers.**  
- These are usually Godstones  
- Report others via GitHub Issues

---

## Download

👉 https://github.com/taengu/Aion2-Dps-Meter/releases

Please do not harass players based on DPS results.  
Use at your own risk.

---

## Community & Support

💬 **Join our Discord:** https://discord.gg/Aion2Global
