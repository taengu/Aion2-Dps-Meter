# Npcap Installer

Place the official Npcap installer executable in this folder as:

```
npcap-installer.exe
```

This file is bundled into the Windows MSI by the `appResourcesRootDir` setting in `build.gradle.kts`. The app will attempt to run the bundled installer with WinPcap API-compatible mode enabled when Npcap is missing.

Source: https://npcap.com/#download
