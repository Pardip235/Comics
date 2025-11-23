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
- **Data Layer**: `ComicRepositoryImpl`, `XkcdApiService`, SQLDelight database (Flow-based, cache-first with stale-while-revalidate)
  - Uses `ComicDto` for API responses (internal to data layer)
  - Database as single source of truth with reactive Flow streams
  - TTL-based cache refresh (1h for list, 24h for detail)
- **Domain Layer**: Use cases (`ObserveComicsUseCase`, `ObserveFavoriteComicsUseCase`, `LoadInitialComicsUseCase`, `LoadMoreComicsUseCase`, `ToggleFavoriteUseCase`, etc.)
  - `Observe*UseCase`: Expose reactive Flow streams
  - `Load*UseCase`: Trigger API fetches and cache updates
- **Presentation Layer**: ViewModels (`ComicsViewModel`, `ComicDetailViewModel`, `FavoritesViewModel`) using extension functions
  - `ViewModelExtensions`: Common error handling and Flow observation utilities
  - Composition over inheritance pattern
- **Platform Layer**: Platform-specific implementations (Android/iOS drivers, HTTP clients, logging)

### Features

1. **Comics List Screen**
   - Infinite scroll pagination (auto-loads when scrolling near bottom)
   - Error handling with retry mechanism
   - Loading states (initial load, pagination, refresh)
   - Stale-while-revalidate: Shows cached data while refreshing in background
   - Smart initial loading: Shows loading indicator first, then data or error

2. **Comic Detail Screen**
   - Displays full comic details
   - Favorite toggle functionality
   - Navigation from list screen

3. **Favorites Screen**
   - Lists all favorited comics (Android - fully implemented, iOS - placeholder)
   - Reactive updates when favorites change

4. **Offline Support & Caching Strategy**
   - **Cache-First Approach**: Always shows cached data immediately
   - **Stale-While-Revalidate**: Displays cached data even if stale, refreshes in background
   - **TTL-Based Refresh**: 
     - Comics list: Refreshes if cache is older than 1 hour
     - Comic detail: Refreshes if cache is older than 24 hours
   - **Automatic Background Refresh**: Triggers API calls when cache is empty or stale
   - Falls back to cached data when network fails
   - Favorite status persists locally

5. **Automatic Cache Management**
   - Smart cache cleanup that preserves favorites
   - Time-based expiration: Removes comics older than 30 days (configurable)
   - Size-based limiting: Keeps maximum 500 non-favorite comics (configurable)
   - Automatic cleanup runs on app start in the background
   - Favorites are always preserved regardless of age or cache size
   - Database versioning: Uses `comics_v2.db` to handle schema changes

6. **Reactive Architecture**
   - **Flow-Based Repository**: All data operations expose reactive Flow streams
   - **Database as Single Source of Truth**: ViewModels observe database changes via Flow
   - **Automatic UI Updates**: UI automatically reflects data changes without manual synchronization
   - **No Event Managers**: Reactive Flow eliminates need for event dispatchers
   - **Cache-First with Background Refresh**: Immediate data display with smart background updates

## Thought Process

### Why Kotlin Multiplatform?

I chose KMP to maximize code sharing while maintaining native UI experiences. This approach:
- Reduces code duplication for business logic
- Allows platform-specific optimizations where needed
- Provides a single source of truth for data operations

### Architecture Decisions

1. **Clean Architecture**: Separates concerns and makes the codebase testable and maintainable
2. **Reactive Flow-Based Architecture**: 
   - Repository exposes Flow streams for all data operations
   - Database is the single source of truth
   - ViewModels observe Flows for automatic UI updates
3. **MVVM Pattern**: ViewModels manage UI state and business logic, making UI reactive
4. **Use Cases**: Encapsulate business rules and make the domain layer independent of data sources
   - `Observe*UseCase`: For reactive data observation
   - `Load*UseCase`: For triggering data fetches
5. **Repository Pattern**: Abstracts data sources (API + Database) behind a single interface
   - Cache-first strategy with stale-while-revalidate
   - TTL-based automatic refresh
6. **Dependency Injection (Koin)**: Simplifies dependency management and testing
7. **Composition over Inheritance**: ViewModels use extension functions instead of BaseViewModel
8. **Model Separation**: 
   - `ComicDto`: Data layer model (API responses)
   - `Comic`: Domain model (includes business logic like `isFavorite`)
   - Extension functions convert between layers
9. **Logging**: Napier library for cross-platform logging (Android Log / iOS NSLog)
10. **Cache Management**: Automatic cache cleanup prevents unbounded growth while preserving user favorites

### Testing Strategy

The project currently includes the shared testing infrastructure (in-memory SQLDelight driver, coroutine test utilities) but only minimal unit tests. The priority has been stabilizing the reactive data flow; expanding end-to-end and ViewModel tests is tracked as a follow-up task.

### iOS Integration

- **StateFlow Extensions**: Helper functions to observe Kotlin `StateFlow` from Swift
- **ViewModel Wrappers**: `ObservableObject` wrappers that bridge Kotlin ViewModels to SwiftUI
- **Koin iOS**: Platform-specific Koin initialization for iOS
- **Napier Logging**: Logs visible in Xcode Console (View > Debug Area > Activate Console)
- **iOS Deployment Target**: 16.0 (required for NavigationStack)
- **Error Tracking**: OSLog integration for error logging in Xcode

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
│       └── androidUnitTest/        # JVM-only unit tests (MockK, SQLDelight)
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

Test coverage is intentionally lean at the moment. The shared module ships with coroutine test utilities and in-memory SQLDelight drivers so that repository/use-case tests can be added quickly. Expanding the `androidUnitTest` source set with MockK-powered tests is on the roadmap.

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
   - ✅ Napier logging integrated
   - ✅ Comprehensive KDoc documentation added
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
   - ✅ Comprehensive KDoc comments added
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
- **Kotlin Flow**: Reactive streams for data observation
- **StateFlow**: Reactive state management
- **Napier**: Cross-platform logging library (KMM-friendly)
- **MockK**: Testing/mocking
- **JUnit**: Testing framework

## Cache Management Strategy

The app implements a sophisticated caching strategy with automatic cleanup and smart refresh:

### Cache-First with Stale-While-Revalidate

1. **Immediate Data Display**: Always shows cached data immediately (if available)
2. **Background Refresh**: Automatically refreshes stale data in the background
3. **TTL-Based Refresh**:
   - **Comics List**: Refreshes if latest comic is older than 1 hour (`CACHE_FRESHNESS_TTL_SECONDS`)
   - **Comic Detail**: Refreshes if comic is older than 24 hours (`COMIC_DETAIL_CACHE_TTL_SECONDS`)
4. **Smart Refresh Logic**: Only triggers API calls when cache is actually stale
5. **Error Resilience**: On API failure, cached data remains available

### Automatic Cache Cleanup

**Configuration:**
- **Max Cache Age**: 30 days (configurable via `CacheConfig.MAX_CACHE_AGE_DAYS`)
- **Max Non-Favorite Comics**: 500 comics (configurable via `CacheConfig.MAX_NON_FAVORITE_COMICS`)

**How It Works:**
1. **Automatic Cleanup**: Runs automatically when the app starts (in `ComicsViewModel.init`)
2. **Time-Based Expiration**: Removes comics older than the configured age limit (favorites preserved)
3. **Size-Based Limiting**: When non-favorite comics exceed the limit, oldest ones are removed (favorites preserved)
4. **Background Execution**: Cleanup runs in the background without blocking the UI

### Implementation Details
- `PerformCacheCleanupUseCase`: Encapsulates cache cleanup logic
- `ComicRepository.performAutomaticCacheCleanup()`: Combines time-based and size-based clearing
- `last_refreshed_at` column tracks cache freshness for TTL-based refresh
- Platform abstraction `TimeProvider` (expect/actual) ensures consistent timestamp handling across platforms
- Database versioning: Uses `comics_v2.db` to handle schema changes (replaces old database)

## Logging & Error Tracking

### Napier Logging (KMM-Friendly)

The app uses **Napier** for cross-platform logging:

- **Android**: Logs appear in Logcat with tag "ComicsApp"
- **iOS**: Logs appear in Xcode Console (View > Debug Area > Activate Console)
- **Error Logging**: All errors are logged with full stack traces
- **Usage**: `Logger.e(tag, message, throwable)` for errors, `Logger.d/i/w` for other levels

### Error Tracking

- **Unknown Errors**: Enhanced error messages guide users to check console
- **OSLog Integration**: iOS uses OSLog for structured error logging
- **Automatic Error Logging**: ViewModels automatically log all errors with full details
- **Xcode Console**: Filter by "ComicsApp" or search for specific tags to find errors


