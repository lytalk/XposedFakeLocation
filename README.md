# **XposedFakeLocation**

![GitHub License](https://img.shields.io/github/license/noobexon1/XposedFakeLocation?color=blue)
![GitHub Release Date](https://img.shields.io/github/release-date/noobexon1/XposedFakeLocation?color=violet)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/noobexon1/XposedFakeLocation/total)
![GitHub repo size](https://img.shields.io/github/repo-size/noobexon1/XposedFakeLocation)
![GitHub Repo stars](https://img.shields.io/github/stars/noobexon1/XposedFakeLocation)
![GitHub Release](https://img.shields.io/github/v/release/noobexon1/XposedFakeLocation?color=red)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)]()

**XposedFakeLocation** is an Android application and Xposed module that allows you to spoof your device's location globally or for specific apps without using "mock location" from the developer options. Customize your location with precision, including sensor data, and add randomization within a specified radius for enhanced privacy.


<div align="center">
    <img src="images/xposedfakelocation.webp" alt="App Logo" width="256" />
</div>


---

## **Table of Contents**

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Development](#development)
- [License](#license)
- [Disclaimer](#disclaimer)
- [Acknowledgements](#acknowledgements)

---

## **Features**

- **Global Location Spoofing**: Override your device's location data system-wide (Unstable for now).
- **Per-App Location Control**: Apply location spoofing to specific applications.
- **Custom Coordinates**: Set precise latitude and longitude.
- **Altitude and Accuracy Settings**: Customize altitude, accuracy and other custom sensor values.
- **Randomization**: Add random offsets within a specified radius for enhanced privacy.
- **User-Friendly Interface**: Modern Material Design 3 UI built with Jetpack Compose.
- **Intuitive Navigation**: Easy access to maps, favorite locations, and settings.
- **Community Integration**: Direct links to Telegram, Discord, and GitHub communities.

---

## **Prerequisites**

- **Rooted Android Device**: The app requires root access to function properly. That being said, you can try working with Xposed virtual environement on non rooted device.
- **LSPosed**: Install the Xposed Framework compatible with your Android version.
  - [LSPosed](https://github.com/LSPosed/LSPosed)

---

## **Installation**

You can always install the latest stable version from the releases page. If you want to build by yourself:

1. **Clone or Download the Repository**

   ```bash
   git clone https://github.com/noobexon1/XposedFakeLocation.git
   ```

2. **Build the Application**

   - Open the project in **Android Studio**.
   - Build the APK using **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - Alternatively, use Gradle:

     ```bash
     ./gradlew assembleDebug
     ```

3. **Install the APK on Your Device**

   - Transfer the APK to your device.
   - Install the APK using a file manager or via ADB:

     ```bash
     adb install app/build/outputs/apk/debug/app-debug.apk
     ```

4. **Activate the Xposed Module**

   - Open **Xposed Installer** or **LSPosed Manager**.
   - Enable the **XposedFakeLocation** module.
   - If you decide to apply the module system wide, Reboot your device to apply changes.

---

## **Usage**

1. **Launch the App**

   - Open **XposedFakeLocation** from your app drawer.

2. **Navigate the Interface**

   - Use the navigation drawer to access different sections:
     - **Map**: Primary interface for location selection
     - **Favorites**: Saved locations for quick access
     - **Settings**: Configure application behavior
     - **About**: View application information

3. **Select a Location**

   - Use the integrated map to select your desired location.
   - Tap on the map to set the fake location.

4. **Configure Settings**

   - Access the **Settings** screen to customize:

     - **Accuracy**: Enable and set a custom horizontal and/or vertical accuracy value.
     - **Altitude**: Enable and set a custom altitude.
     - **Other Sensor Data**: New spoofable sensors data added in new versions.
     - **Randomization Radius**: Set the radius in meters for location randomization.

5. **Start Spoofing**

   - Toggle the **Start** button to begin location spoofing.
   - The app will now override your device's location data based on the target(s) specified in the Xposed manager app.

6. **Stop Spoofing**

   - Toggle the **Stop** button to cease location spoofing.

---

### **Favorites**

- Save frequently used locations for quick access.
- If a marker is already present on the map, the coordinates for the new favorite location will automatically be copied to the fields from it.
- Manage your favorites by adding or removing locations.
- Access your favorites through the navigation drawer for easy selection.

---

## **Development**

### **Built With**

- **Kotlin**: Programming language for Android development.
- **Jetpack Compose**: Modern toolkit for building native Android UI with Material Design 3.
- **Material 3 Design**: Latest design system from Google for an enhanced user experience.
- **Xposed API**: Framework for runtime modification of system and app behavior.
- **OSMDroid**: Open-source map rendering engine for Android.

### **User Interface**

- **Navigation Drawer**: Easy access to all major app features
- **Material Design Components**: Consistent design language throughout the app
- **Adaptive Layouts**: Compatible with various screen sizes and orientations

### **Prerequisites**

- **Android Studio Flamingo** or newer.
- **Android SDK** with API level 31 or above.
- **Kotlin** version 1.8.0 or above.

### **Building from Source**

1. **Clone the Repository**

   ```bash
   git clone https://github.com/noobexon1/XposedFakeLocation.git
   ```

2. **Open in Android Studio**

   - Navigate to the project directory.
   - Open the project with **Android Studio**.

3. **Sync Gradle**

   - Allow Gradle to download all dependencies.

4. **Build and Run**

   - Connect your rooted device or start an emulator with root capabilities.
   - Run the app from **Android Studio**.

---

## **License**

Distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.

---

## **Disclaimer**

This application is intended for **development and testing purposes only**. Misuse of location spoofing can violate terms of service of other applications and services. Use at your own risk. There is no responsibility whatsoever for any damage to the device.

---

## **Acknowledgements**

- [GpsSetter](https://github.com/Android1500/GpsSetter) - Highly inspired by this amazing project!
- [Xposed Framework](https://repo.xposed.info/) - Java hooks
- [LSPosed](https://github.com/LSPosed/LSPosed) - The go-to Xposed framework manager app.
- [OSMDroid](https://github.com/osmdroid/osmdroid) - Open-source offline map interface.
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit for Android.
- [Material Design 3](https://m3.material.io/) - Latest design system from Google.
- [Line Awesome Icons](https://icons8.com/line-awesome) - Beautiful icon set used in the app.


