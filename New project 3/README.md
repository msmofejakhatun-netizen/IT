# RestoPro Captain

Native Android captain ordering app built with Kotlin, Jetpack Compose, MVVM, Clean Architecture, Material 3, Retrofit, OkHttp, Room, Socket.IO, Coroutines, Hilt, Navigation Compose, DataStore Preferences, and Coil.

This project intentionally contains no WebView, Capacitor, React Native, Electron, iframe, or web-to-APK architecture.

## Modules

- `data/local`: Room entities and DAO source of truth for menu, tables, orders, captains, settings, and pending sync.
- `data/remote`: Retrofit APIs, OkHttp interceptors, dynamic LAN base URL, JWT auth, retry and timeout handling.
- `data/repository`: Offline-first repositories for auth, menu, tables, orders, settings, sync payload mapping.
- `domain`: Role, order, table models and use cases.
- `ui`: Native Compose screens for login, dashboard, tables, menu/cart/KOT, running orders, and printer settings.
- `socket`: Socket.IO manager with reconnect and live table update handling.
- `sync`: Coroutine-based queue flusher for pending offline operations.
- `printer`: Android Bluetooth RFCOMM ESC/POS thermal printing for KOT and test prints.

## Requirements

- Android Studio with JDK 17
- Android Gradle Plugin 8.7+
- Minimum SDK 26, target SDK 35

Open the folder in Android Studio and sync Gradle. The app expects a LAN backend compatible with endpoints under `http://<server-ip>:5000/api/captain/...`.
