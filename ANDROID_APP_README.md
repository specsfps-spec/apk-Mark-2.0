# MARK LV / JARVIS — Android companion app

This adds a native Android wrapper for the existing JARVIS dashboard. It does **not** port Windows automation to Android: the Python app must remain running on the PC for PC-control features. The app connects to the existing dashboard server at port 8000.

## What is included
- `android-app/`: Android Studio/Gradle project (Java + WebView).
- `.github/workflows/android-apk.yml`: cloud build workflow that creates a debug APK and publishes it as a downloadable Actions artifact.

## Build APK automatically (no Android SDK needed on your PC)
1. Upload this project to a GitHub repository (the whole project, including `android-app/` and `.github/workflows/`).
2. Open the repository's **Actions** tab and select **Build Android APK**.
3. Click **Run workflow**. Wait until the workflow succeeds.
4. Open that completed run and download the artifact named `MarkLV-Jarvis-Android-APK`. Extract it; the APK inside is `app-debug.apk`.

## Connect phone to PC
1. Install/run MARK LV / JARVIS on Windows and ensure its dashboard dependencies are installed (`pip install -r requirements.txt`).
2. Start the JARVIS app and allow it through Windows Firewall on private networks.
3. Connect the Android phone and PC to the same Wi-Fi.
4. Find the PC's LAN IPv4 address using `ipconfig` in Windows Command Prompt.
5. Open the Android app and enter `http://PC-IP:8000` (example format only; use the actual IP printed by JARVIS). If your dashboard has HTTPS enabled, use the exact HTTPS URL/port shown by JARVIS instead.
6. Use the existing JARVIS dashboard/login/QR workflow if prompted.

## Notes / limitations
- This is a companion app for the existing web dashboard, not a fully standalone port of the Python desktop application.
- Windows-only operations (desktop control, local files, app launching, screen capture) require the PC to stay on and JARVIS running.
- The dashboard should only be exposed on a trusted network. Do not forward port 8000 to the public internet; use a properly secured VPN for remote access.
- Debug APKs are suitable for personal testing. Play Store publication needs a release build and signing configuration.
