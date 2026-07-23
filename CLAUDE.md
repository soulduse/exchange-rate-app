# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Korean exchange rate Android application that:
- Displays real-time currency exchange rates for multiple countries
- Provides a calculator for currency conversion
- Allows users to set price alerts for specific exchange rates
- Parses exchange rate data from Korean financial websites using JSoup
- Supports both Korean and English languages

**Package**: `com.dave.soul.exchange_app`

## Build and Development Commands

### Building the app
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew bundleRelease          # Build release AAB (Android App Bundle)
```

### Running tests
```bash
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests
```

### Cleaning the project
```bash
./gradlew clean                  # Clean build artifacts
```

### Other useful commands
```bash
./gradlew dependencies           # View dependency tree
./gradlew tasks                  # View all available tasks
```

## Architecture Overview

### Language Mix
- **Kotlin**: Newer components (MainActivity, SplashActivity, utility classes, DI module)
- **Java**: Core business logic (parsers, managers, adapters, fragments, models, RealmController)

### Core Components

#### 1. Data Layer (`realm/`, `model/`)
- **RealmController.java**: Central database operations controller. All Realm CRUD operations go through this singleton.
- **Models**: `ExchangeRate`, `AlarmModel`, `CalcuCountries`, `ParserModel` - all are Realm objects
- Realm DB stores: user-selected countries, exchange rate data, calculator settings, alarm configurations

#### 2. Parsing Layer (`paser/`)
- **ExchangeParser.java**: Uses JSoup to scrape exchange rate data from Korean financial websites
- **ExchangeInfo.java**: Interface with constants (BASE_URL, SECOND_URL, country codes, parsing indices)
- Special handling: Japanese Yen (JPY) values are divided by 100 due to different data format
- Parses HTML tables to extract: base price, buy/sell prices, send/receive prices, US exchange rates

#### 3. Data Management (`manager/`)
- **DataManager.java**: Orchestrates data flow between parser and Realm. Singleton pattern.
- **ParserManager.java**: Handles async execution of parsing tasks
- Network-aware: If connected, fetches fresh data; if disconnected, uses cached Realm data

#### 4. UI Layer (`view/`)
- **MainActivity.kt**: Tab-based interface with ViewPager
  - Tab 1 (OneFragment): Exchange rate list with RecyclerView
  - Tab 2 (TwoFragment): Calculator for currency conversion
  - Tab 3 (ThreeFragment): Alarm settings for price alerts
- **SetCountryActivity**: Country selection screen
- **SettingActivity**: App settings (parsing interval, startup screen, language, alarm sound)
- Uses DataBinding for view inflation

#### 5. Background Services (`view/service/`)
- **AlarmService**: Monitors exchange rates and triggers notifications when user-defined thresholds are met
- **BackupWorker**: WorkManager-based periodic task
- **RestartService**: BroadcastReceiver for service restart
- **RestartAlarm.kt**: Manages alarm registration/unregistration

#### 6. Dependency Injection (`di/`)
- **AppModule.kt**: Koin module defining singletons (AdProvider, CommonSharedPref, AdChecker, SharedPreferences)

### Key Libraries and Tools
- **Realm**: Local database for offline-first architecture
- **JSoup**: HTML parsing for exchange rate data extraction
- **Koin**: Dependency injection (version 1.0.0)
- **Firebase**: Crashlytics, Analytics, Remote Config, Ads
- **DataBinding**: View binding enabled
- **Glide**: Image loading for country flags
- **Timber**: Logging (debug only)
- **Kakao SDK**: Sharing functionality
- **JUnit & Mockito**: Testing frameworks

## Important Notes

### Data Parsing
- Exchange rates are scraped from Korean financial websites (Naver-based URLs)
- Parsing can be fragile - the `errorCheckAndRemoveArray()` method in ExchangeParser handles inconsistencies
- Special case: South African Rand (ZAR) and countries with "공화국" in their name require extra handling

### Database Operations
- Always use RealmController methods for database operations
- Realm operations must be wrapped in transactions
- Auto-increment ID logic is handled in `getAutoIncrement()`

### Threading
- Parsing happens asynchronously via ParserManager callbacks
- Realm instances are thread-confined - don't share across threads
- UI updates happen in callbacks after async operations complete

### Application Lifecycle
- MyApplication.kt initializes: Timber (debug), Realm config, Koin DI, Firebase Ads
- MainActivity registers/unregisters alarms and broadcast receivers in lifecycle methods
- RealmConfiguration uses `deleteRealmIfMigrationNeeded()` - schema changes will wipe data

### Testing
- JUnit 4 for unit tests
- Mockito for mocking
- Test files should be in `app/src/test/` (unit) or `app/src/androidTest/` (instrumentation)

## Development Tips

- Min SDK: 23 (Android 6.0)
- Target SDK: 33 (Android 13)
- Version: 1.8.5 (versionCode 14)
- Keystore file exists: `exchange-rate-app.jks` (for release signing)
- When modifying parsers, test thoroughly as HTML structure changes can break parsing logic
- Alarm logic is in RealmController.getAlarms() - it compares user-set prices with current rates
- Korean/English string resources are in `res/values/` and `res/values-en/`
