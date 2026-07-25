# Dice Roller / Zar At

Offline Android dice roller built with Java, XML, Material 3, and OpenGL ES 2.0.

## Features

- Single and double dice modes
- Programmatically generated dice textures; no external image assets
- Real 3D cube rotation with polished OpenGL ES animation
- Shake-to-roll support with accelerometer cooldown
- Local roll history, limited to the latest 20 rolls
- Local settings for sound, vibration, shake-to-roll, dice theme, app theme, and animation speed
- Turkish and English string resources
- No internet permission, analytics, ads, Firebase, accounts, or cloud sync

## Tech Stack

- Java + XML Android views
- OpenGL ES 2.0 rendering
- Minimum SDK: 24

## Build

Open the project in Android Studio, or run:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat lint
```

## Run

1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Select an emulator or physical Android device.
4. Run the `app` configuration.

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Screenshots

Screenshots can be added here for the single die mode, double dice mode, and roll result state.

## Architecture

The app keeps UI state in `MainViewModel`, stores settings and recent rolls in `DiceRepository`, renders dice in `DiceRenderer`, and keeps dice orientation math in `DiceOrientationMapper`.
