# ChainTorque Marketplace: Android Implementation Guide

## Table of Contents
1. Project Overview
2. Architecture & Data Flow
3. Smart Contract (ChainTorqueMarketplace.sol)
4. Backend API & Data Models
5. Frontend Reference (UI/UX)
6. Android Feature Mapping
7. Step-by-Step Working (A-Z)
8. Integration: Wallet, Web3, Backend
9. References & Further Reading

---

## 1. Project Overview
ChainTorque Marketplace is a decentralized platform for trading CAD assets, built on Ethereum (Sepolia testnet). It consists of:
- **Smart Contracts** (Solidity): Asset management, transactions
- **Backend** (Node.js, Hardhat): API, contract interaction, user management
- **Frontend** (React, Vite): User interface for web
- **Android App** (To be built): Native marketplace using Kotlin & Jetpack Compose

---

## 2. Architecture & Data Flow
### High-Level Flow
1. **User** interacts with the app (browse, buy, sell, upload assets)
2. **Android App** calls backend APIs and interacts with smart contracts via Web3
3. **Backend** manages user data, transaction records, and contract calls
4. **Smart Contract** executes asset transfers, mints, burns, etc.
5. **Frontend** (web) is a reference for UI/UX and API usage

### Key Components
- **Smart Contract:** `ChainTorqueMarketplace.sol`
- **Backend:** REST API (`server.js`), contract scripts (`deploy.js`, `batchBurn.js`)
- **Models:** `User.js`, `Transaction.js`
- **Frontend:** React components, context, hooks

---

## 3. Smart Contract (ChainTorqueMarketplace.sol)
- Location: `contracts/ChainTorqueMarketplace.sol`
- Implements ERC721 for CAD assets
- Functions: mint, burn, transfer, set metadata
- Events: Transfer, Minted, Burned
- ABI: `artifacts/contracts/ChainTorqueMarketplace.sol/ChainTorqueMarketplace.json`
- Deploy using Hardhat (`scripts/deploy.js`)
- Interact using Web3j (Kotlin) or similar

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
- `POST /api/transaction` — Record transaction
- `GET /api/transaction/:id` — Get transaction details

### Data Models
- **User:** id, name, email, walletAddress
- **Asset:** id, name, description, imageUrl, tokenId, owner, metadata
- **Transaction:** id, type, assetId, from, to, status, timestamp

---

## 5. Frontend Reference (UI/UX)
- Location: `Marketplace (Frontend)/src/`
- React components for asset list, detail, upload, wallet connect, transaction history
- Use as reference for Android screens and flows

---

## 6. Android Feature Mapping
### Core Features
1. **User Authentication** — Register, login, manage profile (API)
2. **Wallet Integration** — Connect Ethereum wallet (WalletConnect, MetaMask)
3. **Asset Browsing** — List assets, view details (API)
4. **Buy/Sell/Transfer Assets** — Contract calls via Web3j
5. **Upload Assets** — Upload CAD files/images (API)
6. **Transaction History** — List user transactions (API)

### UI Screens (Jetpack Compose)
- Splash/Login/Register
- Wallet Connect
- Asset List
- Asset Detail
- Upload Asset
- Transaction History
- Profile/Settings

---

## 7. Step-by-Step Working (A-Z)
1. **User opens app**
2. **Authenticate** (register/login via backend API)
3. **Connect wallet** (WalletConnect/MetaMask)
4. **Fetch assets** (GET /api/assets)
5. **View asset details** (GET /api/assets/:id)
6. **Upload asset** (POST /api/assets, upload file/image)
7. **Buy/Sell/Transfer asset** (Web3j contract call, sign transaction)
8. **Record transaction** (POST /api/transaction)
9. **View transaction history** (GET /api/transaction)
10. **Profile management** (GET/POST /api/user)

---

## 8. Integration: Wallet, Web3, Backend
- **Wallet:** Use WalletConnect SDK for Android
- **Web3:** Use Web3j for Ethereum contract interaction
- **Backend:** Use Retrofit/OkHttp for REST API
- **Asset Storage:** Integrate with Lighthouse/IPFS via backend

---

## 9. References & Further Reading
- [Web3j Android Docs](https://docs.web3j.io/)
- [WalletConnect Android SDK](https://docs.walletconnect.com/2.0/android)
- [Hardhat Docs](https://hardhat.org/)
- [Lighthouse Storage](https://docs.lighthouse.storage/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit](https://square.github.io/retrofit/)

---

## Final Notes
- Use backend and frontend as reference for flows and data
- All contract interactions must be signed by user’s wallet
- Asset uploads go through backend, stored on Lighthouse/IPFS
- Transactions are logged both on-chain and in backend DB

---
