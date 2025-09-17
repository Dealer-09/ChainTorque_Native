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
### Core Features (Implemented)
1. **Asset Browsing** — List assets, view details (API)
2. **Buy/Sell/Transfer Assets** — Purchase NFTs via API (backend handles contract logic)
3. **Transaction History** — Tabs for owned NFTs, purchases, and sales (API)
4. **Profile Management** — View user profile, stats, and wallet balance
5. **Wallet Integration** — User connects wallet by entering address; wallet validation and balance fetching supported
6. **Error Handling & Loading States** — Progress bars and snackbars for feedback

### Features To Be Added
- **Asset Upload UI:** API endpoint exists, but UI for uploading assets is not yet implemented
- **Authentication:** No email/password or social login; only wallet address-based profile
- **Notifications/Real-Time Updates:** Not implemented
- **Advanced Filtering/Sorting:** Basic search present; advanced filters not shown
- **WalletConnect/MetaMask Integration:** Only manual address entry supported

### UI Screens (Jetpack Compose & Fragments)
- Marketplace (asset list, search, details, purchase)
- User Profile (owned NFTs, purchases, sales)
- Wallet (connect, view balance, QR scan placeholder)
- Settings (if implemented)

---

## 6. Step-by-Step Working (A-Z)
1. **User opens app**
2. **Connect wallet** (enter wallet address; no WalletConnect/MetaMask integration)
3. **Browse marketplace** (view assets, search, see details)
4. **Purchase assets** (confirm purchase, backend handles transaction)
5. **View transaction history** (owned NFTs, purchases, sales)
6. **View and manage profile** (profile info, wallet balance)
7. **Upload asset** (UI not yet implemented; API endpoint available)

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
- Asset uploads go through backend, stored on Lighthouse/IPFS (UI for upload needs to be added)
- Transactions are logged both on-chain and in backend DB
- Wallet integration is via manual address entry; no direct WalletConnect/MetaMask
- Authentication is wallet-based; no email/password or social login

---
