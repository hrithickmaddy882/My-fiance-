# MyFinance Android app

A simple Kotlin Android expense tracker starter project.

## Features
- Manually set/update the bank balance.
- Add an expense; the app subtracts it from the saved balance.
- Stores balance, daily expense total and recent expense entries locally on the device.
- Shows an ongoing notification with the balance saved in this app.

## Important limitations
- This starter app does **not** connect to Punjab National Bank, PhonePe, SMS or bank servers.
- It cannot verify your live bank balance or automatically read transactions.
- Enter amounts from trusted PNB transaction messages/statements and update the balance yourself.
- The notification shows the balance stored in this app, not a live bank balance.

## Open in Android Studio
1. Extract the ZIP.
2. Open Android Studio.
3. Choose **Open** and select the extracted `MyFinance` folder (the folder containing `settings.gradle.kts`).
4. Wait for Gradle sync to finish. Allow Android Studio to download the required Gradle/Android components.
5. Connect an Android phone with USB debugging enabled, or create an emulator.
6. Press the green **Run ▶** button and select your device.

Requires Android Studio with JDK 17 support and Android SDK 35. The project uses Android Gradle Plugin 8.7.3 and Kotlin 2.0.21.

## Upload to GitHub
1. Sign in at https://github.com and create a repository named `MyFinance`.
2. In the repository choose **Add file → Upload files**.
3. Extract this ZIP on your computer first.
4. Upload the files and folders inside `MyFinance` (including `app`, `build.gradle.kts`, and `settings.gradle.kts`).
5. Choose **Commit changes**.
