# PlayHub (formerly PlayPal)

![Version](https://img.shields.io/badge/version-v2.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-11%2B-orange)
![Database](https://img.shields.io/badge/Database-MongoDB_Atlas-green)
![Status](https://img.shields.io/badge/Status-Stable_Production-success)

**PlayHub** is the evolution of our premier sports connection platform. A robust console-based application designed to connect sports enthusiasts, PlayHub allows users to find training partners, create sports sessions, book professional trainers, and manage their sports schedules—all from a streamlined command-line interface.

With the release of **v2.0.0**, the application has transitioned to a cloud-connected ecosystem backed by **MongoDB Atlas**, ensuring data persistence and real-time synchronization.

---

## 📋 Table of Contents
- Key Features(#-key-features)
- Architecture & Tech Stack(#-architecture--tech-stack)
- Prerequisites(#-prerequisites)
- Installation & Usage(#-installation--usage)
- Troubleshooting(#-troubleshooting)
- Release History(#-release-history)

---

## 🚀 Key Features

### 👤 User Management
* **Secure Authentication:** Dedicated registration and login flows for both **Players** and **Trainers**.
* **Cloud Persistence:** All account data (profiles, balances, history) is securely stored in the cloud via MongoDB Atlas.
* **Profile Management:** Players manage balances; Trainers manage hourly rates and specialties.

### 📅 Intelligent Session Management
* **Create & Join:** Schedule sports events (Time, Location, Sport Type) or search for available games to join.
* **Live Logic:** Automated checks for capacity limits and participant duplication.
* **Real-time Updates:** Session data is synchronized instantly across the platform.

### 🏋️ Trainer Booking System
* **Discovery Engine:** Search for professional trainers by specialty (e.g., "Yoga", "Tennis").
* **Atomic Transactions:** Smart cost calculation with "mock" payment processing. Player balances are deducted only if the trainer receives credit.

### 💬 Integrated Communication
* **Unified Inbox:** A polymorphic messaging system that handles both direct user-to-user messages and system alerts.
* **Persistent Notifications:** System events (e.g., *Booking Confirmed*, *User Joined Session*) are saved as permanent history in the database.

### 🛡️ Admin Dashboard
* **Oversight:** System Administrators can review and approve pending Trainer accounts.
* **Status Reporting:** View real-time statistics on total users and active sessions.

---

## 🛠 Architecture & Tech Stack

PlayHub utilizes a production-ready, 7-layer architecture designed for scalability and maintainability.

* **Language:** Java 11+
* **Database:** MongoDB Atlas (Cloud)
* **Connectivity:** `mongodb-driver-sync`
* **Design Patterns:**
    * **Command Pattern:** For asynchronous generation and saving of notifications.
    * **Singleton Pattern:** For Manager classes (Auth, Booking, Session).
    * **Polymorphism:** Unified handling of User types and Message types.
* **Data Integrity:** Custom Data Access Layer (DAL) utilizing Java Reflection and "Upsert" logic to map objects to BSON documents.

---

## 📝 Prerequisites

Before running the application, ensure you meet the following requirements:

1.  **Java Development Kit (JDK):** Version 11 or higher.
    * To check your version, run: `java -version`
2.  **Internet Connection:** An active connection is required to communicate with the MongoDB Atlas cluster.

---

## 💻 Installation & Usage

PlayHub is currently packaged for Windows environments.

1.  **Download:** Navigate to the **Releases** section and download the `PlayHub_v2.0.0.zip` folder.
2.  **Unzip:** Extract the contents of the zip file to a location of your choice.
3.  **Run:** Open the folder and double-click the `run.bat` file.
4.  **Interact:** A command window will open. Use the **Number Keys** to select menu options and **Enter** to confirm.

---

## ⚠️ Troubleshooting

**Error: `UnsupportedClassVersionError`**
* **Cause:** Your installed Java version is older than Java 11.
* **Fix:** Install JDK 11 or JDK 21 from [Adoptium.net](https://adoptium.net/).

**Connection Issues**
* **Requirement:** Ensure you are connected to the internet. Offline mode is not supported in v2.0.0 due to the cloud database architecture.

---

## 📜 Release History

### **v2.0.0 - Official Launch & Cloud Integration (Latest)**
* **Rebrand:** Official name change from PlayPal to **PlayHub**.
* **Cloud Persistence:** Fully integrated with MongoDB Atlas for online data storage.
* **Persistent Notifications:** System alerts are now stored permanently as messages.
* **Stability:** Fixed infinite loop edge cases and improved input sanitization.

### **v1.1.0 - Persistent Database Pre-release**
* **Backend Overhaul:** Replaced in-memory `ArrayList` storage with MongoDB.
* **Refactoring:** Implemented custom DAL and `mongodb-driver-sync`.
* **Safety:** Added atomic transaction logic for bookings.

### **v1.0.0 - Initial Release**
* **Launch:** Initial CLI prototype with local session storage.
* **Core Features:** Basic user management, session creation, and mock payments.

---

*© 2025 PlayHub Development Team*