# Creating Stories

Creating Stories is a Minecraft 1.21.1 NeoForge modpack built around exploration, staged progression, Create: Aeronautics, modular equipment, and a unified magic experience across Ars Nouveau and Iron's Spells 'n Spellbooks.

## Install with CurseForge

1. Install and open the [CurseForge app](https://www.curseforge.com/download/app).
2. Download the Creating Stories `0.4.0` profile ZIP. Do not extract it.
3. In CurseForge, open **Minecraft** and select **Import**.
4. Choose **Import Profile .zip**, select the downloaded ZIP, and wait for CurseForge to install the profile.
5. Open the profile and press **Play**. Allow the first launch several minutes; Minecraft must build caches and load a large mod set.

CurseForge requires a proper exported profile ZIP containing `manifest.json` and `overrides`; a ZIP containing an arbitrary mods folder will not import correctly. See the [official CurseForge import instructions](https://support.curseforge.com/en/support/solutions/articles/9000198501-exporting-and-importing-modpacks).

### Memory

Allocate approximately **8–10 GB of RAM** to the profile. More is not automatically better, and you should leave enough memory for Windows and background applications.

In CurseForge, open **Settings → Minecraft → Java Settings** and adjust **Allocated Memory**. Restart Minecraft after changing it.

## ⚠️ MUST READ — Recommended Distant Horizons settings

Distant Horizons can generate and render terrain far beyond Minecraft's normal render distance. This is expensive: Creating Stories also runs complex world generation and C2ME, so aggressive DH settings can cause stuttering, delayed chunk loading, excessive CPU use, or very large world files.

Use this stable starting point:

| Setting | Recommended value |
|---|---:|
| Vanilla render distance | 10–12 chunks |
| DH LOD render distance | **128 chunks** |
| Vertical quality | **Medium** |
| Horizontal quality | **Medium** |
| DH threads | **2** |
| Thread runtime ratio | **0.50–0.75** |
| Distant generator mode | **Internal Server** |
| Distant generation | Enabled initially; disable if the game stutters |

Open **Options → Distant Horizons** to change the common graphics settings. Advanced threading and generation controls are available in DH's advanced settings.

Important rules:

- Do not set DH to use every CPU core. C2ME, Minecraft, Create contraptions, mob AI, and world generation also need CPU time.
- A larger LOD distance affects CPU, GPU memory, system memory, disk usage, and initial generation time. Start at 128 and increase only after sustained testing.
- If CPU usage or stuttering is high, first reduce DH threads to 1–2 or lower the runtime ratio. If that is insufficient, disable distant generation.
- If GPU usage is high, lower LOD render distance, vertical quality, or horizontal quality.
- When entering a new world, allow distant terrain time to populate. Faster generation produces heavier lag spikes.
- Keep a frame-rate limit enabled; unlimited FPS can leave less hardware headroom for LOD generation.

These recommendations follow the Distant Horizons team's guidance to reduce CPU generation load when CPU-bound and reduce render distance/quality when GPU-bound. See the [official Distant Horizons troubleshooting guidance](https://gitlab.com/distant-horizons-team/distant-horizons/-/wikis/2-frequently-asked-questions/2-problems-and-solutions/Problems-and-Solutions).

## First-launch checks

- Confirm the main menu reaches Minecraft `1.21.1` with NeoForge.
- Create a new test world before moving an important existing world into the profile.
- Expect only one visible mana bar. Ars and Iron's spells share the pack's unified magic progression.
- Ordinary supported equipment can be converted and rebuilt through the Modular Workbench. Boss and signature weapons may intentionally remain unique.

## Updating

Creating Stories checks for new pack releases when Minecraft starts. When an update is available, use the provided release link, duplicate or back up the profile, and preserve important worlds before updating. The notification is informational and never downloads or installs a release automatically.

Do not use **Update All** on a working release unless you are testing a copy of the profile. Mod updates can change recipes, registries, world generation, rendering, or integration APIs.

Before updating:

1. Duplicate the CurseForge profile.
2. Back up important worlds.
3. Apply updates to the duplicate.
4. Test world loading, unified spell casting, modular equipment, JEI/EMI pages, Create machinery, and Distant Horizons.

## Troubleshooting

- **The imported ZIP is invalid:** confirm it is the original CurseForge profile ZIP and was not extracted/repacked.
- **The game runs out of memory:** allocate 8–10 GB and close memory-heavy background applications.
- **New-world generation stutters:** reduce DH threads/runtime ratio or temporarily disable distant generation.
- **Low FPS while terrain is already generated:** lower the DH LOD distance or quality settings.
- **A repeatable crash occurs:** preserve `logs/latest.log` and the newest file under `crash-reports/` before relaunching.

Creating Stories `0.4.0` is tested on NeoForge `21.1.235`.
