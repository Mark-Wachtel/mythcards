
# 📚 Programmdokumentation – Clientmodul

## 🧾 Übersicht

Diese Dokumentation beschreibt die wichtigsten Klassen deines Client-Systems für ein Java-basiertes Online-Spiel inklusive Kartenanzeige, Chat und Freundeslistenfunktionalität.

---

## 🚀 Einstiegspunkt

### `ClientMain.java`
- **Zweck**: Einstiegspunkt des JavaFX/FXGL-Clients. Initialisiert UI und Loginprozess.
- **Wichtige Methoden**: `main()`, `initSettings()`, `showLoginScene()`, `showGameScene()`
- **Besonderheiten**: JWT-Login, benutzerdefinierter Lade- und Loginbildschirm

---

## 🔐 Authentifizierung

### `AuthTokenStore.java`
- **Zweck**: Zentraler Speicher für das JWT-Token.
- **Funktionen**: `setToken()`, `getToken()`
- **Besonderheiten**: statisch, thread-unsicher, aber einfach

---

## 🃏 Kartenverwaltung

### `CardDataFetcher.java`
- **Zweck**: Holt Kartendaten vom Server via REST.
- **Nutzt**: `HttpClient`, `ObjectMapper`, `CardDataDTO`, `AuthTokenStore`
- **Funktion**: `fetchCards()` → Liste von `CardDataDTO`

### `CardLocalization.java`
- **Zweck**: Liefert sprachabhängige Texte für Karten.
- **Struktur**: `Map<language, Map<key,text>>`
- **Funktion**: `get(lang, key)`, `loadFromJson(path)`

### `CardService.java`
- **Zweck**: Verbindet `CardDataFetcher` und `CardLocalization`, cached Karten.
- **Funktion**: `fetch()`, `getLocalizedCardTitle(...)`, `getLocalizedAbility(...)`

### `CardView.java`
- **Zweck**: Visuelle Anzeige einer einzelnen Karte mit JavaFX.
- **Erbt von**: `VBox`
- **Funktion**: `createCardImage(...)`, `createStatLabel(...)`

---

## 💬 Chat-Funktion

### `ChatMessageCell.java`
- **Zweck**: JavaFX-Zelle für einzelne Chatnachrichten.
- **Erbt von**: `ListCell<ChatMessageDTO>`
- **Unterscheidet**: Eigene vs. fremde Nachrichten

### `ChatSocket.java`
- **Zweck**: WebSocket-STOMP-Client für Live-Chat.
- **Funktionen**: `connect()`, `sendMessage()`, `subscribeToConversation()`
- **Callbacks**: `messageHandler`, `badgeHandler`, `presenceHandler`

### `ChatWindow.java`
- **Zweck**: Komplette Chatfenster-UI mit History & Live-Funktion.
- **Komponenten**: `ListView`, `TextField`, REST + WebSocket

---

## 👥 Freundesliste

### `FriendCell.java`
- **Zweck**: JavaFX-Zelle für einen Freund (Name, Status, Buttons)
- **Funktionen**: `setOnChat()`, `setOnRemove()`

### `FriendListPane.java`
- **Zweck**: Container für Freundesanzeige mit Aktionen.
- **Funktionen**: `updateFriends()`, `removeFriend()`, `updateStatus()`

### `FriendServiceClient.java`
- **Zweck**: REST-Client für Freundschaftsaktionen.
- **Funktionen**: `getFriends()`, `sendFriendRequest()`, `removeFriend()`, `blockUser()`

---

*Stand: Mai 2025 – erstellt automatisch durch ChatGPT.*
