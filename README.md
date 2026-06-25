# ChainTorque Native

**Premium 3D CAD NFT Marketplace - Android App**

A native Android application for browsing, purchasing, and managing NFT-based CAD assets on the Ethereum Sepolia testnet.

## Features

- **NFT Marketplace** - Browse, search (by title, category, or creator), and purchase premium 3D CAD assets
- **WalletConnect (AppKit)** - Connect with over 300+ crypto wallets seamlessly
- **User Profiles** - Four tabs: owned NFTs, created items, sales, and full activity history (mints, purchases, sales, relists)
- **Resell** - Re-list owned items back onto the marketplace at a new price via `relistToken`
- **Live RPC Data** - Direct blockchain interaction via `eth_getBalance` and `eth_sendTransaction`
- **Runtime Config** - Contract address, chain, and listing fee fetched from the backend on launch, so a contract redeploy is picked up without an app rebuild
- **Light / Dark Theme** - Persisted monochrome theme with a runtime toggle in Settings
- **Sepolia Testnet** - Built strictly for the Ethereum Sepolia testnet

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Hilt DI |
| Networking | Retrofit, OkHttp |
| Wallet | Reown AppKit (WalletConnect) |
| State | StateFlow, LiveData |
| Blockchain | Native JSON-RPC & AppKit interactions |

## Project Structure

```
app/src/main/java/com/example/chaintorquenative/
├── di/                    # Hilt dependency injection modules
├── mobile/
│   ├── data/
│   │   ├── api/           # Retrofit services & models
│   │   ├── config/        # AppConfig — runtime config from GET /api/config
│   │   └── repository/    # Data repositories (Web3, Market, User, Config)
│   └── ui/
│       └── viewmodel/     # Screen ViewModels
├── ui/
│   ├── components/        # Reusable global UI components
│   ├── screens/           
│   │   ├── marketplace/   # Marketplace & Item detail screens
│   │   ├── profile/       # User profile, inventory, and sales
│   │   ├── settings/      # Configuration & URLs
│   │   └── wallet/        # Wallet connection state UI
│   └── theme/             # AppColors and global theming
├── wallet/                # AppKit/WalletConnect integration managers
├── ChainTorqueApplication.kt
└── MainActivity.kt
```

## Setup

### Prerequisites
- Android Studio Ladybug or later
- JDK 17+
- Android SDK 34+

### Configuration (Critical)
To build and run the app, you **must** configure your local environment secrets.
Create a `local.properties` file in the root directory (`ChainTorque_Native/local.properties`) and add the following keys:

```properties
# Reown AppKit / WalletConnect Cloud Project ID
WALLET_CONNECT_PROJECT_ID="your_walletconnect_project_id_here"

# The deployed ChainTorque Smart Contract Address on Sepolia
# Used as a compile-time fallback; the live value is fetched from GET /api/config at runtime.
CONTRACT_ADDRESS="0x9685Ac9d1d63C1442161e64A7A325Eaa7a505F00"

# Sepolia JSON-RPC endpoint (optional; defaults to https://rpc.sepolia.org)
RPC_URL="https://rpc.sepolia.org"
```

### Build
```bash
./gradlew assembleDebug
```

## API Endpoints

The app connects to the ChainTorque backend server for off-chain metadata:
- `GET /api/config` - Runtime config (contract address, chain ID, listing fee)
- `GET /api/marketplace` - List NFT assets
- `GET /api/marketplace/:id` - Asset details
- `POST /api/marketplace/sync-purchase` - Sync blockchain purchases to DB
- `POST /api/marketplace/sync-relist` - Sync blockchain relists to DB
- `GET /api/user/:address/nfts` - User's NFTs
- `GET /api/user/:address/sales` - User's sales history
- `GET /api/user/:address/transactions` - User's full transaction history

## Testing

1. Install any compatible Wallet (e.g., **MetaMask**, **Trust Wallet**) on your Android device/emulator.
2. Create/Import a wallet and switch the network to the **Sepolia Testnet**.
3. Get test ETH from a [Sepolia faucet](https://sepoliafaucet.com/).
4. Open ChainTorque, go to the **Wallet** tab, and tap "Connect Wallet".
5. Approve the connection via the AppKit modal.
6. To buy: Select an item, tap "Buy Now", accept the irreversibility warning, and sign the transaction in your wallet.