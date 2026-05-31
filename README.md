# DinoLL288s Housebuilder

A client-side Forge 1.20.1 mod that helps build a small customizable starter house by controlling normal player block placement.

## Features

- In-game customization screen
- Quick build keybind
- 5x5 and 7x7 house footprints
- Adjustable wall height, door side, door size, and door offset
- Optional floor and roof
- Wall, floor, and roof materials selected from the block in your main hand
- Works in creative and survival using normal client-side placement

## Keybinds

- `H`: Open the housebuilder menu
- `B`: Start a quick build with saved settings

## Survival Notes

Survival mode requires the selected building blocks to be available in your hotbar. Creative mode can automatically place the selected material into the active hotbar slot.

## Mod Loader

- Minecraft: 1.20.1
- Forge: 47.x
- Side: Client only

## Release Build

Run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-release.ps1
```

The upload-ready file will be written to:

```text
dist\dinoll288s_housebuilder-0.1.0-forge-1.20.1.jar
```

## CurseForge Upload

CurseForge uploads require an author API token and the project ID from the project URL.

```powershell
$env:CURSEFORGE_TOKEN = "your-token-here"
powershell -ExecutionPolicy Bypass -File .\scripts\upload-curseforge.ps1 -ProjectId "your-project-id"
```

The upload is tagged as `1.20.1`, `Forge`, and `Client`.
