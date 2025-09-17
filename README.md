# ChainTorque Marketplace: Android Thin Client Guide

## Table of Contents
1. Project Overview
2. Architecture & Data Flow
3. Backend API & Data Models
4. Frontend Reference (UI/UX)
5. Android Feature Mapping
6. Step-by-Step Working (A-Z)
7. Integration: Cloud Backend & Android
8. References & Further Reading

---

## 1. Project Overview
ChainTorque Marketplace is a decentralized platform for trading CAD assets, built on Ethereum (Sepolia testnet). In this architecture, all blockchain and database logic is handled in the cloud backend. The Android app acts only as a user interface, communicating with the backend via APIs.

- **Smart Contracts** (Solidity): Asset management, transactions (handled by backend)
- **Backend** (Node.js, Hardhat): API, contract interaction, user management, wallet management
- **Frontend** (React, Vite): User interface for web
- **Android App** (To be built): Thin client using Kotlin & Jetpack Compose (API only)

---

## 2. Architecture & Data Flow
### High-Level Flow
1. **User** interacts with the app (browse, buy, sell, upload assets)
2. **Android App** sends requests to backend APIs (REST or WebSocket)
3. **Backend** manages user data, transaction records, asset storage, and all blockchain interactions
4. **Smart Contract** logic is executed by backend only
5. **Frontend** (web) is a reference for UI/UX and API usage

### Key Components
- **Backend:** REST API (`server.js`), handles all contract calls and DB operations
- **Models:** `User.js`, `Transaction.js`
- **Frontend:** React components, context, hooks

---

## 3. Backend API & Data Models
- Location: `server.js`, `models/User.js`, `models/Transaction.js`, `services/lighthouseStorage.js`
- Express server, REST endpoints for user, asset, transaction management
- Handles all smart contract interactions and asset storage (Lighthouse/IPFS)

---

## 4. Backend API & Data Models
- Location: `server.js`, `models/User.js`, `models/Transaction.js`, `services/lighthouseStorage.js`
- Express server, REST endpoints for user, asset, transaction management
- Interacts with smart contract via Web3
- Asset storage via Lighthouse/IPFS

### Typical Endpoints
- `POST /api/register` — Register user
- `POST /api/login` — Authenticate user
- `GET /api/assets` — List assets
- `POST /api/assets` — Upload asset
- `POST /api/transaction` — Buy/Sell/Transfer asset (backend handles contract call)
- `GET /api/transaction/:id` — Get transaction details

### Data Models
- **User:** id, name, email, walletAddress
- **Asset:** id, name, description, imageUrl, tokenId, owner, metadata
- **Transaction:** id, type, assetId, from, to, status, timestamp

---

## 4. Frontend Reference (UI/UX)
- Location: `Marketplace (Frontend)/src/`
- React components for asset list, detail, upload, transaction history
- Use as reference for Android screens and flows

---

## 5. Android Feature Mapping
### Core Features
1. **User Authentication** — Register, login, manage profile (API)
2. **Asset Browsing** — List assets, view details (API)
3. **Buy/Sell/Transfer Assets** — Initiate via API (backend handles contract logic)
4. **Upload Assets** — Upload CAD files/images (API)
5. **Transaction History** — List user transactions (API)

### UI Screens (Jetpack Compose)
- Splash/Login/Register
- Asset List
- Asset Detail
- Upload Asset
- Transaction History
- Profile/Settings

---

## 6. Step-by-Step Working (A-Z)
1. **User opens app**
2. **Authenticate** (register/login via backend API)
3. **Fetch assets** (GET /api/assets)
4. **View asset details** (GET /api/assets/:id)
5. **Upload asset** (POST /api/assets, upload file/image)
6. **Buy/Sell/Transfer asset** (POST /api/transaction; backend handles contract call)
7. **View transaction history** (GET /api/transaction)
8. **Profile management** (GET/POST /api/user)

---

## 7. Integration: Cloud Backend & Android
- **Backend:** Use Retrofit/OkHttp for REST API calls
- **All blockchain and DB logic is handled by backend**
- **Asset Storage:** Managed by backend (Lighthouse/IPFS)

---

## 8. References & Further Reading
- [Hardhat Docs](https://hardhat.org/)
- [Lighthouse Storage](https://docs.lighthouse.storage/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit](https://square.github.io/retrofit/)

---

## Final Notes
- The Android app is a thin client: it only calls backend APIs and displays data
- All blockchain, wallet, and database logic is handled by the backend
- Asset uploads go through backend, stored on Lighthouse/IPFS
- Transactions are logged both on-chain and in backend DB

---
