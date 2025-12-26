# ChainTorque Native

**Premium 3D CAD NFT Marketplace - Android App**

A native Android application for browsing, purchasing, and managing NFT-based CAD assets on the Ethereum Sepolia testnet.

## Features

- **NFT Marketplace** - Browse and purchase premium 3D CAD assets
- **WalletConnect Integration** - Connect MetaMask and other wallets via Reown AppKit
- **User Profiles** - View owned NFTs, purchase history, and sales
- **Sepolia Testnet** - Built for Ethereum Sepolia testnet transactions

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Hilt DI |
| Networking | Retrofit, OkHttp |
| Wallet | Reown AppKit (WalletConnect v2) |
| State | StateFlow, LiveData |

## Project Structure

```
app/src/main/java/com/example/chaintorquenative/
├── di/                    # Hilt dependency injection
├── mobile/
│   ├── data/
│   │   ├── api/           # API service & models
│   │   └── repository/    # Data repositories
│   └── ui/
│       └── viewmodel/     # ViewModels
├── ui/
│   ├── components/        # Reusable UI components
│   ├── screens/           # Compose screens
│   └── theme/             # App theming
├── wallet/                # WalletConnect integration
├── ChainTorqueApplication.kt
└── MainActivity.kt
```

## Setup

### Prerequisites
- Android Studio Ladybug or later
- JDK 11+
- Android SDK 33+

### WalletConnect Configuration
1. Get a Project ID from [Reown Dashboard](https://cloud.reown.com/)
2. Add your package name: `com.example.chaintorquenative`
3. Update `WalletConnectConfig.PROJECT_ID` in `wallet/WalletConnectManager.kt`

### Build
```bash
./gradlew assembleDebug
```

## API Endpoints

The app connects to a backend server for:
- `GET /api/marketplace` - List NFT assets
- `GET /api/marketplace/:id` - Asset details
- `POST /api/user/register` - Register wallet
- `GET /api/user/:address/nfts` - User's NFTs

## Testing

1. Install MetaMask on your Android device
2. Switch to Sepolia testnet in MetaMask
3. Get test ETH from a [Sepolia faucet](https://sepoliafaucet.com/)
4. Connect wallet in the app

## License

MIT License

---

Built with ❤️ for the Web3 community
