# Comics App - Kotlin Multiplatform

A Kotlin Multiplatform (KMP) application for browsing XKCD comics, built with shared business logic and native UIs for both Android (Jetpack Compose) and iOS (SwiftUI).

## Quick Overview

This app demonstrates a clean architecture approach to building a cross-platform mobile application where:
- **Business logic is shared** between Android and iOS using Kotlin Multiplatform
- **UI is native** on each platform (Jetpack Compose for Android, SwiftUI for iOS)
- **Data is cached locally** using SQLDelight for offline support
- **Network requests** are handled via Ktor Client
- **Dependency injection** is managed with Koin

## Solution Description

### Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (ViewModels, UI State, Event Managers)                 │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│  (Use Cases, Repository Interfaces)                     │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  (Repository Implementation, API Service, Database)     │
└─────────────────────────────────────────────────────────┘
```

**Key Components:**
- **Data Layer**: `ComicRepositoryImpl`, `XkcdApiService`, SQLDelight database
- **Domain Layer**: Use cases (`GetInitialComicsUseCase`, `LoadMoreComicsUseCase`, `ToggleFavoriteUseCase`, etc.)
- **Presentation Layer**: ViewModels (`ComicsViewModel`, `ComicDetailViewModel`, `FavoritesViewModel`)
- **Platform Layer**: Platform-specific implementations (Android/iOS drivers, HTTP clients)

### Features

1. **Comics List Screen**
   - Infinite scroll pagination (auto-loads when scrolling near bottom)
   - Error handling with retry mechanism
   - Loading states (initial load, pagination, refresh)

2. **Comic Detail Screen**
   - Displays full comic details
   - Favorite toggle functionality
   - Navigation from list screen

3. **Favorites Screen**
   - Lists all favorited comics (Android - fully implemented, iOS - placeholder)
   - Reactive updates when favorites change

4. **Offline Support**
   - Comics are cached in local SQLDelight database
   - Falls back to cached data when network fails
   - Favorite status persists locally

5. **Automatic Cache Management**
   - Smart cache cleanup that preserves favorites
   - Time-based expiration: Removes comics older than 30 days (configurable)
   - Size-based limiting: Keeps maximum 500 non-favorite comics (configurable)
   - Automatic cleanup runs on app start in the background
   - Favorites are always preserved regardless of age or cache size

6. **Cross-Platform Event System**
   - `FavoritesEventManager` uses SharedFlow to notify changes across screens
   - Enables reactive UI updates when favorites are toggled

## Thought Process

### Why Kotlin Multiplatform?

I chose KMP to maximize code sharing while maintaining native UI experiences. This approach:
- Reduces code duplication for business logic
- Allows platform-specific optimizations where needed
- Provides a single source of truth for data operations

### Architecture Decisions

1. **Clean Architecture**: Separates concerns and makes the codebase testable and maintainable
2. **MVVM Pattern**: ViewModels manage UI state and business logic, making UI reactive
3. **Use Cases**: Encapsulate business rules and make the domain layer independent of data sources
4. **Repository Pattern**: Abstracts data sources (API + Database) behind a single interface
5. **Dependency Injection (Koin)**: Simplifies dependency management and testing
6. **Cache Management**: Automatic cache cleanup prevents unbounded growth while preserving user favorites

### Testing Strategy

- **Unit Tests**: Using MockK for mocking dependencies
- **In-Memory Database**: SQLDelight's in-memory driver for isolated repository tests
- **Test Dispatchers**: `MainDispatcherRule` to handle `viewModelScope` in tests
- **Base Test Class**: `MainTest` provides common test infrastructure

### iOS Integration

- **StateFlow Extensions**: Helper functions to observe Kotlin `StateFlow` from Swift
- **ViewModel Wrappers**: `ObservableObject` wrappers that bridge Kotlin ViewModels to SwiftUI
- **Koin iOS**: Platform-specific Koin initialization for iOS

## Project Structure

```
comics/
├── shared/                          # Shared Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/             # Shared code
│       │   ├── data/               # Data layer (API, Repository impl, models)
│       │   ├── domain/             # Domain layer (Use cases, repository interfaces)
│       │   ├── presentation/        # Presentation layer (ViewModels, UI state)
│       │   ├── di/                 # Dependency injection modules
│       │   ├── platform/           # Platform abstractions
│       │   └── util/               # Utilities (CacheConfig, ErrorType, etc.)
│       ├── androidMain/            # Android-specific implementations
│       ├── iosMain/                # iOS-specific implementations
│       └── commonTest/             # Shared test code
├── androidApp/                     # Android application
│   └── src/main/
│       ├── kotlin/                 # Android UI (Jetpack Compose)
│       └── res/                    # Android resources
└── iosApp/                         # iOS application
    └── iosApp/
        ├── ui/                     # SwiftUI screens
        └── viewmodel/              # ViewModel wrappers
```

### ✅ Unit & Integration Tests

**Test Coverage:**
- `ComicRepositoryImplTest`: Tests repository with in-memory database
- `GetInitialComicsUseCaseTest`: Tests use case logic
- `ComicsViewModelTest`: Tests ViewModel state management

**Test Infrastructure:**
- `MainTest` base class with `MainDispatcherRule` for coroutine testing
- MockK for mocking dependencies
- In-memory SQLDelight driver for database tests

**Cache Management:**
- Automatic cache cleanup with time-based (30 days) and size-based (500 comics) limits
- `PerformCacheCleanupUseCase`: Handles cache cleanup logic
- `CacheConfig`: Centralized cache configuration constants
- Platform-agnostic `TimeProvider` (in platform folder) for timestamp handling


## Missing Parts & Time Constraints

### What's Missing

1. **iOS Favorites Screen**: Currently shows "Not implemented" placeholder
   - The ViewModel logic exists in shared code
   - Needs SwiftUI implementation and ViewModel wrapper

2. **Comprehensive Test Coverage**:
   - More ViewModel tests (error scenarios, edge cases)
   - Use case tests for all use cases
   - Integration tests for full flows

3. **Error Handling Enhancements**:
   - More specific error messages
   - Retry strategies for different error types
   - Better offline state indication

4. **UI/UX Improvements**:
   - Image caching and optimization
   - Skeleton loaders instead of simple progress indicators
   - Better empty states
   - Pull-to-refresh (currently only refresh button)

5. **Code Quality**:
   - Replace `println` with proper logging framework
   - Add more documentation
   - Code formatting/linting automation

6. **Performance Optimizations**:
   - Image loading optimization
   - Database query optimization
   - Pagination improvements

7. **Cache Management UI**:
   - Settings screen to manually trigger cache cleanup
   - Display cache size information
   - Allow users to configure cache limits

## What Could Be Done Better

1. **Testing**:
   - More comprehensive unit test coverage
   - Integration tests for end-to-end flows
   - UI tests for critical user journeys

2. **Error Handling**:
   - More granular error types
   - Better error recovery strategies
   - User-friendly error messages with actionable suggestions

3. **Code Organization**:
   - Extract more reusable components
   - Better separation of concerns in some ViewModels
   - More consistent error handling patterns

4. **Documentation**:
   - More comprehensive KDoc comments
   - Architecture decision records (ADRs)
   - Setup and contribution guidelines

5. **Performance**:
   - Image caching strategy
   - Database query optimization
   - Pagination improvements (virtual scrolling for large lists)

6. **Platform Parity**:
   - Complete iOS favorites screen
   - Ensure feature parity between Android and iOS
   - Platform-specific optimizations

7. **Developer Experience**:
   - CI/CD pipeline
   - Automated testing
   - Code formatting/linting automation
   - Better build scripts

## Setup Instructions

### Prerequisites
- Android Studio (for Android development)
- Xcode (for iOS development)
- JDK 11+
- Gradle 8.0+

### Building the Project

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd comics
   ```

2. **Run Android app**
   - Open project in Android Studio
   - Run `androidApp` configuration

3. **Run iOS app**
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Select a simulator or device
   - Build and run (Cmd+R)

### Running Tests

```bash
./gradlew :shared:test
```

## Technologies Used

- **Kotlin Multiplatform**: Shared business logic
- **Jetpack Compose**: Android UI
- **SwiftUI**: iOS UI
- **Koin**: Dependency injection
- **SQLDelight**: Local database with automatic cache management
- **Ktor**: HTTP client
- **Kotlin Coroutines**: Asynchronous programming
- **StateFlow**: Reactive state management
- **MockK**: Testing/mocking
- **JUnit**: Testing framework

## Cache Management Strategy

The app implements an automatic cache cleanup strategy to prevent unbounded database growth while preserving user favorites:

### Configuration
- **Max Cache Age**: 30 days (configurable via `CacheConfig.MAX_CACHE_AGE_DAYS`)
- **Max Non-Favorite Comics**: 500 comics (configurable via `CacheConfig.MAX_NON_FAVORITE_COMICS`)

### How It Works
1. **Automatic Cleanup**: Runs automatically when the app starts (in `ComicsViewModel.init`)
2. **Time-Based Expiration**: Removes comics older than the configured age limit (favorites preserved)
3. **Size-Based Limiting**: When non-favorite comics exceed the limit, oldest ones are removed (favorites preserved)
4. **Background Execution**: Cleanup runs in the background without blocking the UI

### Implementation Details
- `PerformCacheCleanupUseCase`: Encapsulates cache cleanup logic
- `ComicRepository.performAutomaticCacheCleanup()`: Combines time-based and size-based clearing
- Private helper methods handle the actual deletion logic
- Platform abstraction `TimeProvider` (expect/actual) ensures consistent timestamp handling across platforms


