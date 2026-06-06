# DBU Connect 🟢
> **The Premium Campus Networking, Matching & Event Platform for Debre Berhan University Students.**

DBU Connect is a modern, secure, and visually stunning Android application designed exclusively for the student community of **Debre Berhan University (DBU)**. Built using modern Android development practices, it bridges the gap between campus networking, social interaction, event organization, and secure communication.

Whether you are looking for a study buddy in Software Engineering, want to coordinate a campus clean-up event, or wish to start a direct video call with a new match—DBU Connect provides a seamless, premium experience.

---

## 🌟 Key Features

### 1. 🎓 Smart Discovery & Swiping
* **Swipe Cards**: A beautiful, fluid gesture-based swiping card system to discover and connect with other students.
* **Smart Filter System**: Filter profiles based on academic departments, enrollment year, interests, and matching intent (*Friends, Dating, Study Buddy*).
* **Ethiopian Identity**: Standardized representation featuring beautiful, high-quality, authentic Ethiopian student portrait mock assets to ground the application's local university identity.

### 2. 💬 Real-Time Direct Messaging
* **Interactive Chats**: Smooth messaging interface with instant message state tracking (Sent/Sending indicators).
* **Immediate Local Sync**: Fast UI update mapping that immediately injects new messages into the active chat list upon sending.
* **Offline Caching**: Built-in support to read, draft, and browse conversations offline, leveraging local Room database caching.

### 3. 📹 Out-of-the-Box Video Calling
* **Videocam Integration**: Initiate video calls instantly using the integrated call button located in the active chat toolbar.
* **Dedicated Screens**: Clean, premium incoming and outgoing call screens with profile backgrounds, micro-animations, and call control buttons (mute, end, camera flip).

### 4. 📅 Campus Events Hub
* **Event Hosting**: Discover academic, cultural, social, or sports events organized across DBU.
* **Interactive RSVP**: Keep track of events you are interested in with status badges (*Attending, Interested, Not Attending*).
* **Create Events**: In-app form to organize new campus meetups, complete with category icons, date/time pickers, and description details.

### 5. 🔒 Two-Email Dual Security (Authentication)
* **Secure Registration**: Integration of official university emails (`@dbu.edu.et`) to verify student authenticity.
* **Recovery Mechanism**: Double-email password recovery linking official university email to a verified personal email address (`@gmail.com`), allowing a secure password reset verification process.

---

## 🛠 Tech Stack & Architecture

DBU Connect is built on top of modern, state-of-the-art Android architecture principles:

* **UI Layer**: **Jetpack Compose** — 100% declarative, highly responsive, utilizing premium animations, custom-curated components, and dark/light system palette compatibility.
* **Architecture**: **MVVM (Model-View-ViewModel)** with the **Repository Pattern** ensuring a strict separation of concerns.
* **Database**: **Room ORM** — SQLite integration supporting local state caching, offline-first interactions, and structured relationship modeling.
* **Dependency Injection**: **Hilt / Dagger** — Seamless, clean injection of APIs, databases, view models, and repositories.
* **Networking**: **Retrofit & Supabase** — High-performance HTTP client connecting the app to a Supabase backend service with fully integrated fallbacks to a comprehensive local mock server for testing.
* **Asynchronous Flow**: **Kotlin Coroutines & Flow** — Native reactive programming using `StateFlow` and `SharedFlow` to handle real-time database changes and state mutations.

---

## 📂 Project Structure

```
com.dbuconnect/
├── data/
│   ├── api/          # Supabase & Mock API Services, DTO mapping
│   ├── datastore/    # User preference and login state persistence
│   ├── db/           # SQLite Room Database, DAOs, and Type Converters
│   ├── models/       # Pure domain data entities
│   └── repository/   # Repository layer coordinating network/db caching
├── di/               # Dagger-Hilt dependency injection modules
├── presentation/     # UI Layer
│   ├── common/       # UI State wrappers (UiState, Event)
│   ├── components/   # Curated custom UI components (Buttons, Chips, Textfields)
│   ├── navigation/   # Navigation screens, route definitions, and NavHost graph
│   ├── screens/      # Feature screens (Auth, Discover, Chat, Events, Profile)
│   ├── theme/        # Color systems, typography styles, and shapes
│   └── viewmodels/   # Feature ViewModels managing StateFlow representation
```

---

## 🚀 Getting Started

### Prerequisites
* **Android Studio Ladybug (or newer)**
* **JDK 17** (or newer)
* **Android SDK 34** (or newer)

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/yasino-916/Dbu_connect.git
   cd Dbu_connect
   ```

2. **Configure Environment**
   * The app is pre-configured to run with a robust, production-grade **Mock API Service** by default. This allows you to explore the full matching, swiping, messaging, video calling, and event systems immediately without setting up external servers.
   * If you wish to connect to your live Supabase database, update the database keys in `gradle.properties` or create a `.env` file in the root directory.

3. **Build the Project**
   * Open the project in Android Studio.
   * Let Gradle sync finish completely.
   * Run compilation using Gradle wrapper:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Run on Device / Emulator**
   * Click **Run** in Android Studio to install the application on your physical device or Android Emulator.

---

## 🎨 Visual Design Guidelines

DBU Connect follows a rigorous, customized design system tailored to the DBU colors:
* **Primary Green (`#1E824C`)**: Inspires organic growth, campus environment, and reliability.
* **Primary Gold Accent (`#F39C12`)**: Delivers high contrast, representational energy, and aesthetic premium look.
* **Modern Gradients**: Used elegantly across top headers, action buttons, and card borders.
* **Typography**: Premium sans-serif scales for highly legible content layout on all screen sizes.

---

## 👥 Group Members

* **Goitom Shumey** ————-DBU1501638
* **Dawit Moges** ——————DBU1501114
* **Yasin Teshome**————— DBU1501545
* **Daniel Fekede** —————-DBU1501108

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

*Designed and engineered with 💚 for the students of Debre Berhan University.*
