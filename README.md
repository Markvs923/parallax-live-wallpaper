# Parallax Live Wallpaper

Custom Android live wallpaper with 3D parallax depth effect driven by device tilt (rotation vector sensor).

Matches the effect from the viral X post by @cabbitar.

## Features
- 3 layers: background.png, glow.png, foreground.png
- Different movement amounts on tilt (5% / 12% / 25%, foreground opposite)
- Soft drop shadow that shifts
- Breathing pulse on glow layer
- Low-pass filter for smooth motion
- Settings screen with depth strength slider
- Battery friendly (sensor only when visible)

## How to use with your own images
1. Cut your object/subject from a photo using any AI background remover (e.g. remove.bg, PhotoRoom).
2. Create 3 PNGs:
   - background.png (full scene, farthest)
   - glow.png (mid layer, optional glow/effects, can have transparency)
   - foreground.png (transparent PNG of the main floating object)
3. Place them in `app/src/main/res/drawable/`
4. Open in Android Studio, build, and run on your S24 Ultra.
5. Set as live wallpaper from the app or system wallpaper picker.

## Build & Install
1. Clone this repo
2. Open in Android Studio (Arctic Fox or newer)
3. Sync Gradle
4. Connect your S24 Ultra (USB debugging on)
5. Run the app
6. Tap "Set as Wallpaper" or go to Wallpaper settings and select this live wallpaper

S24 Ultra is fully supported (high refresh rate, sensors, etc.).

## Requirements
- Android 8.0+ (API 26)
- Sensors: TYPE_ROTATION_VECTOR (standard on modern phones)

Enjoy the floating effect!
