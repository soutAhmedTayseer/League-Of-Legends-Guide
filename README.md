# ⚔️ League of Legends: Champion Codex

> *"The unseen blade is the deadliest." — Zed*

Welcome, Summoner. **Champion Codex** is an offline-first, highly responsive Android application designed to give you the tactical edge on the Rift. Built entirely with modern Android development standards, this app fetches real-time data from the Riot Games API and stores it locally so your champion knowledge is always accessible—even when your Wi-Fi drops mid-queue.

---

## 📸 The Scout's Vision (Screenshots)
*A look at the UI, forged with Jetpack Compose.*

<div align="center">
  <table>
    <tr>
      <td align="center"><b>The Roster (List View)</b></td>
      <td align="center"><b>The Deep Dive (Portrait)</b></td>
      <td align="center"><b>Tactical Vision (Landscape)</b></td>
    </tr>
    <tr>
      <td><img src="link_to_screenshot_1.png" alt="Champion List View" width="250"/></td>
      <td><img src="link_to_screenshot_2.png" alt="Champion Detail Portrait" width="250"/></td>
      <td><img src="link_to_screenshot_3.png" alt="Champion Detail Landscape" width="400"/></td>
    </tr>
  </table>
</div>
<br>
*(Note: Replace `link_to_screenshot_X.png` with the actual file paths of your screenshots once you take them!)*

---

## ✨ Features

* 🛡️ **Offline Survivability (Room Database):** Network down? No problem. The app detects your connection status. If online, it fetches fresh data and caches it. If offline, it instantly retrieves the saved roster from the local Room database.
* 📊 **Deep-Dive Analytics:** Dive into the numbers. Shows over 20 specific base stats and per-level scalings (HP, MP, Attack Damage, Armor, Crit, etc.) mapped perfectly into a balanced UI grid.
* ⚔️ **Combat Visualizers:** Custom Jetpack Compose progress bars that visually translate a champion's Attack, Defense, Magic, and Difficulty ratings out of 10.
* 📱 **Responsive "Adaptive" UI:** The UI reacts to your device. Hold it in Portrait for a smooth scrolling experience, or tilt to Landscape for a split-screen tactical view (List on the left, details on the right).
* 🔔 **System Alerts (Custom Snackbars):** Custom-themed snackbars with the app's logo notify you instantly whether you are pulling data from the live API or reading from offline storage.
* 👈 **Gesture Navigation:** Fully supports Android 13+ predictive back gestures for a buttery smooth user experience.

---

## 🛠️ The Tech Forge (Architecture & Stack)
This project was forged in the fires of **Day 5 Android Kotlin Labs** to demonstrate mastery over modern Android asynchronous programming and local persistence.

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Material 3)
* **Asynchronous Engine:** Coroutines & `lifecycleScope` for non-blocking UI and background threading.
* **Network Messenger:** Retrofit2 + Gson for connecting to `ddragon.leagueoflegends.com`.
* **The Vault (Caching):** Room Database + KSP (Kotlin Symbol Processing) utilizing custom `@TypeConverters` to save complex JSON objects.
* **Image Loading:** Coil (handles network images and adaptive icons seamlessly).
* **Architecture:** Single-Activity Compose Architecture relying on native State observation.

---

## 🚀 How to Summon (Installation)

1. Clone the repository to your local machine:
   ```bash
   git clone [https://github.com/yourusername/League-Guide-App.git](https://github.com/yourusername/League-Guide-App.git)
2. Open the project in **Android Studio** (Koala or newer recommended).
3. Let Gradle sync the Runes (Dependencies).
4. Hit **Run** (`Shift + F10`) to deploy the app to your emulator or physical Android device.

---

## 🎨 Hextech Theming
The app abandons standard Material colors in favor of a custom-built League of Legends palette.

* **LolDarkBlue:** `#0A1428` (The deep void of the background)
* **LolCardBg:** `#091428` (Surface elevations)
* **LolGold:** `#C8AA6E` (Accents, borders, and primary text)
* **LolTextLight:** `#A09B8C` (Muted secondary text)

---

### 📝 Legal Disclaimer
*League of Legends: Champion Codex isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involvedin producing
or managing League of Legends. League of Legends and Riot Games are trademarks or registered trademarks of Riot Games, Inc. League of Legends © Riot Games, Inc.*
