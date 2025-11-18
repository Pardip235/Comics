# Technical Interview Walkthrough Guide

## Pre-Interview Preparation (5 minutes before)

1. **Open the project** in your IDE
2. **Verify the app runs** (Android/iOS)
3. **Have these files ready to show**:
   - `README.md` - Overview
   - `ComicRepositoryImpl.kt` - Repository pattern
   - `ComicsViewModel.kt` - ViewModel with error handling
   - `FavoritesEventManager.kt` - Cross-screen communication
   - `ComicRepository.kt` - Repository interface
   - A Use Case (e.g., `GetInitialComicsUseCase.kt`)
   - Test file (e.g., `ComicRepositoryImplTest.kt`)

---

## Step 1: Introduction & Overview (2-3 minutes)

### What to Say:
> "I've built a Kotlin Multiplatform app for browsing XKCD comics. The key highlight is that I share about 70-80% of the business logic between Android and iOS, while keeping native UIs for the best user experience on each platform."

### Show:
- Open `README.md` - Point to the architecture diagram
- Mention tech stack: **KMP, Jetpack Compose, SwiftUI, SQLDelight, Ktor, Koin**

### Key Points to Mention:
- ✅ Cross-platform code sharing
- ✅ Clean Architecture
- ✅ Native UI on both platforms
- ✅ Offline support with caching

---

## Step 2: Architecture Overview (3-4 minutes)

### What to Say:
> "I followed Clean Architecture with three main layers: Data, Domain, and Presentation. This separation makes the code testable and maintainable."

### Show:
1. **Project Structure** (in IDE):
   ```
   shared/src/commonMain/
   ├── data/          # Repository implementations, API
   ├── domain/        # Use cases, repository interfaces
   ├── presentation/  # ViewModels, UI state
   └── platform/      # Platform abstractions
   ```

2. **Walk through a feature flow** (loading comics):
   - **UI** → Calls `ComicsViewModel.loadInitialComics()` (auto-called in init)
   - **ViewModel** → Uses `GetInitialComicsUseCase`
   - **Use Case** → Calls `ComicRepository.getInitialComics()`
   - **Repository** → Fetches from API, saves to database, returns List
   - **ViewModel** → Updates StateFlow, UI reacts automatically

### Key Points:
- ✅ Dependency rule: Outer layers depend on inner layers
- ✅ Domain layer is platform-agnostic
- ✅ Easy to test each layer independently

---

## Step 3: Data Layer Deep Dive (4-5 minutes)

### Show: `ComicRepositoryImpl.kt`

**What to Highlight:**

1. **API-first with cache fallback**:
   ```kotlin
   override suspend fun getInitialComics(count: Int): List<Comic> {
       return try {
           val comics = apiService.getRecentComics(count)
           insertComics(comics) // Cache locally
           comics.map { comic ->
               val isFavorite = isFavorite(comic.num)
               comic.copy(isFavorite = isFavorite)
           }
       } catch (e: Exception) {
           // Fallback to cached comics if API fails
           getAllCachedComics().take(count)
       }
   }
   ```
   - Explain: "Repository tries API first, falls back to cache on failure. This provides offline support."

2. **Model conversion**:
   ```kotlin
   private fun ComicEntity.toComic(): Comic {
       return Comic(
           num = num.toInt(),
           title = title,
           // ... other fields
           isFavorite = is_favorite > 0 // Convert database value to boolean
       )
   }
   ```
   - Explain: "Database entities are converted to domain models using extension functions. Favorite status is preserved from database."

3. **Cache management**:
   ```kotlin
   override suspend fun performAutomaticCacheCleanup(
       maxAgeDays: Long,
       maxNonFavoriteComics: Int
   ): Int {
       val deletedByAge = clearOldComics(ageThreshold)
       val deletedBySize = clearCacheExceedingLimit(maxNonFavoriteComics)
       return deletedByAge + deletedBySize
   }
   ```
   - Explain: "Automatic cleanup preserves favorites while managing cache size. Runs on app start in the background."

### Key Points:
- ✅ API-first with cache fallback for offline support
- ✅ Model conversion from database entities to domain models
- ✅ Smart cache management (time-based and size-based)
- ✅ Platform-agnostic repository interface

---

## Step 4: Domain Layer - Use Cases (2-3 minutes)

### Show: `GetInitialComicsUseCase.kt`

**What to Highlight:**

```kotlin
class GetInitialComicsUseCase(
    private val repository: ComicRepository
) {
    suspend operator fun invoke(count: Int = 10): List<Comic> {
        return repository.getInitialComics(count)
    }
}
```

**Explain:**
- "Use cases encapsulate business logic"
- "They're simple - just coordinate between ViewModel and Repository"
- "Returns data directly - ViewModel manages state updates"
- "Easy to test in isolation"

### Show: `ComicRepository.kt` (interface)

**Explain:**
- "Repository interface is in the domain layer - platform-agnostic"
- "Implementation is in the data layer"
- "This allows swapping implementations (e.g., different API)"

### Key Points:
- ✅ Single Responsibility Principle
- ✅ Business logic independent of data sources
- ✅ Easy to mock for testing

---

## Step 5: Presentation Layer - ViewModels (4-5 minutes)

### Show: `ComicsViewModel.kt`

**What to Highlight:**

1. **Error handling pattern**:
   ```kotlin
   try {
       val comics = getInitialComicsUseCase(PaginationConfig.INITIAL_LOAD_COUNT)
       _uiState.update { it.copy(comics = comics, isLoading = false) }
   } catch (e: Exception) {
       val errorType = if (isKtorNetworkException(e) || isIOException(e)) {
           ErrorType.NETWORK_ERROR
       } else {
           ErrorType.UNKNOWN_ERROR
       }
       _uiState.update { it.copy(isLoading = false, errorType = errorType) }
   }
   ```
   - Explain: "Each ViewModel handles errors consistently. We classify errors as network or unknown."

2. **Optimistic UI updates**:
   ```kotlin
   fun toggleFavorite(comicNumber: Int) {
       // Optimistic UI update - toggle immediately
       val optimisticFavoriteStatus = !currentComic.isFavorite
       _uiState.update { /* update UI immediately */ }
       
       viewModelScope.launch {
           try {
               val newFavoriteStatus = toggleFavoriteUseCase(comicNumber)
               // Update with actual result
               _uiState.update { /* update with real status */ }
               favoritesEventManager.notifyFavoritesChanged()
           } catch (e: Exception) {
               // Revert optimistic update on error
               _uiState.update { /* revert to original */ }
           }
       }
   }
   ```
   - Explain: "Optimistic updates provide instant feedback. If the operation fails, we revert the UI."

3. **State management with StateFlow**:
   ```kotlin
   private val _uiState = MutableStateFlow(ComicsUiState())
   val uiState: StateFlow<ComicsUiState> = _uiState.asStateFlow()
   ```
   - Explain: "StateFlow provides reactive state. UI automatically updates when state changes."

4. **FavoritesEventManager integration**:
   ```kotlin
   favoritesEventManager.notifyFavoritesChanged()
   ```
   - Explain: "When favorites change, we notify other screens via SharedFlow. This keeps screens in sync."

### Show: `FavoritesEventManager.kt`

**What to Highlight:**

```kotlin
class FavoritesEventManager {
    private val _favoritesChanged = MutableSharedFlow<Unit>(replay = 0)
    val favoritesChanged: SharedFlow<Unit> = _favoritesChanged.asSharedFlow()
    
    fun notifyFavoritesChanged() {
        _favoritesChanged.tryEmit(Unit)
    }
}
```

- Explain: "SharedFlow enables cross-screen communication. When favorites change in one screen, other screens can react."

### Key Points:
- ✅ Consistent error handling pattern across ViewModels
- ✅ Optimistic UI updates for better UX
- ✅ StateFlow for reactive state management
- ✅ FavoritesEventManager for cross-screen synchronization

---

## Step 6: Testing Strategy (3-4 minutes)

### Show: `ComicRepositoryImplTest.kt`

**What to Highlight:**

1. **In-memory database**:
   ```kotlin
   testDatabase = TestDatabaseFactory.createTestDatabase()
   ```
   - Explain: "SQLDelight provides in-memory driver for fast, isolated tests"

2. **MockK for API**:
   ```kotlin
   val mockApiService = mockk<XkcdApiServiceInterface>()
   coEvery { mockApiService.getComicByNumber(999) } returns testComicDto
   ```
   - Explain: "MockK allows us to test repository logic without real API calls"

3. **Repository testing**:
   ```kotlin
   val result = repository.getInitialComics(3)
   assertEquals(3, result.size)
   val cachedComics = repository.getAllCachedComics()
   assertEquals(3, cachedComics.size)
   ```
   - Explain: "We test repository methods directly, verifying both API calls and database operations"

### Show: `MainTest.kt`

**What to Highlight:**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainTest {
    @get:org.junit.Rule
    val mainDispatcherRule = MainDispatcherRule()
}
```

- Explain: "Base test class handles coroutine dispatcher setup for ViewModel tests"

### Key Points:
- ✅ In-memory database for fast tests
- ✅ MockK for dependency mocking
- ✅ Test infrastructure for coroutines
- ✅ Isolated, repeatable tests

---

## Step 7: Platform-Specific Code (2-3 minutes)

### Show: Platform Abstractions

**Show: `platform/TimeProvider.kt`** (expect declaration)
**Show: `platform/TimeProvider.android.kt`** (actual implementation)
**Show: `platform/TimeProvider.ios.kt`** (actual implementation)

**Explain:**
- "`expect`/`actual` mechanism for platform-specific code"
- "Common code uses `expect`, platforms provide `actual`"
- "Compile-time safety - missing implementation causes build failure"

### Show: Database Driver Factory

**Explain:**
- "SQLDelight needs different drivers per platform"
- "Android: AndroidSqliteDriver"
- "iOS: NativeSqliteDriver"
- "JVM (tests): JdbcSqliteDriver"

### Key Points:
- ✅ Platform abstractions for multiplatform code
- ✅ Compile-time safety
- ✅ Easy to add new platforms

---

## Step 8: iOS Integration (2-3 minutes)

### Show: `StateFlowExtensions.kt`

**What to Highlight:**

```kotlin
fun <T> StateFlow<T>.observe(
    scope: CoroutineScope,
    callback: (T) -> Unit
): Cancellable
```

**Explain:**
- "Kotlin StateFlow isn't directly observable from Swift"
- "Extension function bridges Kotlin Flow to Swift callbacks"
- "Returns Cancellable for cleanup"

### Show: `ComicsViewModelWrapper.swift`

**What to Highlight:**

```swift
@MainActor
class ComicsViewModelWrapper: ObservableObject {
    @Published var uiState: ComicsUiState
    private let viewModel: ComicsViewModel
    
    init() {
        viewModel = KoinIOS.shared.getComicsViewModel()
        // Observe state changes
        cancellable = StateFlowExtensionsKt.observe(viewModel.uiState, scope: scope) { [weak self] newState in
            Task { @MainActor in
                self?.uiState = newState as! ComicsUiState
            }
        }
    }
}
```

**Explain:**
- "Swift wrapper bridges Kotlin ViewModel to SwiftUI"
- "Uses ObservableObject for reactive UI updates"
- "Koin provides ViewModel instances"

### Key Points:
- ✅ Clean Swift/Kotlin interop
- ✅ Reactive UI updates
- ✅ Native SwiftUI experience

---

## Step 9: Key Features Demo (3-4 minutes)

### Feature 1: Cross-Screen Favorites Synchronization

**What to Show:**
1. Open the app
2. Toggle a favorite in the comics list (optimistic update - instant feedback)
3. Navigate to detail screen - favorite status is updated
4. Go to favorites screen - comic appears automatically

**Explain:**
- "FavoritesEventManager uses SharedFlow to notify all screens when favorites change"
- "Optimistic UI updates provide instant feedback"
- "Database is single source of truth - all screens stay in sync"
- "FavoritesViewModel observes the event manager and refreshes automatically"

### Feature 2: Offline Support

**What to Show:**
1. Load some comics
2. Turn off network (or show cached data)
3. App still works with cached data

**Explain:**
- "SQLDelight caches all comics"
- "Repository falls back to cache on network errors"
- "Favorites persist offline"

### Feature 3: Cache Management

**Explain:**
- "Automatic cleanup runs on app start"
- "Preserves favorites regardless of age"
- "Configurable limits (30 days, 500 comics)"

---

## Step 10: Code Quality Highlights (2-3 minutes)

### Show: Error Handling

**Point out:**
- Consistent error classification pattern in each ViewModel
- Try-catch blocks with proper error type mapping
- Optimistic updates with error rollback
- No `!!` operators - safe null handling

### Show: Code Organization

**Point out:**
- Feature-based folder structure
- Clear separation of concerns
- Consistent naming conventions
- KDoc comments where needed

### Show: Testing

**Point out:**
- Test coverage for repository
- ViewModel tests with mocked dependencies
- Base test class for common setup

---

## Step 11: Challenges & Solutions (2-3 minutes)

### Challenge 1: Testing ViewModels with viewModelScope

**Problem:**
- `viewModelScope` requires Main dispatcher
- Tests fail without proper setup

**Solution:**
- `MainDispatcherRule` with `StandardTestDispatcher`
- `MainTest` base class for common setup

### Challenge 2: Platform-Specific Code

**Problem:**
- Need different implementations for Android/iOS
- Want type safety

**Solution:**
- `expect`/`actual` mechanism
- Compile-time safety

### Challenge 3: Cache Management

**Problem:**
- Database can grow unbounded
- Need to preserve favorites

**Solution:**
- Automatic cleanup with configurable limits
- Separate logic for time-based and size-based cleanup
- Always preserve favorites

---

## Step 12: What Could Be Improved (1-2 minutes)

**Be Honest About:**

1. **Test Coverage**:
   - "More ViewModel tests for edge cases"
   - "Integration tests for full flows"

2. **iOS Favorites Screen**:
   - "Currently a placeholder - needs full implementation"

3. **Error Handling**:
   - "More granular error types"
   - "Better user-facing error messages"

4. **Performance**:
   - "Image caching strategy"
   - "Database query optimization"

**Why This Matters:**
- Shows self-awareness
- Demonstrates understanding of production concerns
- Shows you think beyond "making it work"

---

## Step 13: Questions & Discussion (remaining time)

### Common Questions & Answers

**Q: Why KMP over Flutter/React Native?**
> "KMP allows us to share business logic while keeping native UIs. This gives us the performance and platform features of native apps, with the code sharing benefits of cross-platform frameworks. Also, since we're using Kotlin, we can leverage existing Android knowledge."

**Q: How do you handle platform differences?**
> "We use `expect`/`actual` for platform-specific code. For example, `TimeProvider` has different implementations for Android and iOS, but the common code just calls `getCurrentTimestampSeconds()`. This keeps platform differences isolated."

**Q: How do you test multiplatform code?**
> "We use in-memory SQLDelight driver for database tests, MockK for API mocking, and `MainDispatcherRule` for coroutine testing. The test infrastructure is shared, but we run tests on JVM for speed."

**Q: What about performance?**
> "We cache comics locally to reduce API calls. StateFlow provides efficient reactive updates. We use optimistic UI updates for instant feedback. For images, we rely on platform-specific libraries (Coil for Android, AsyncImage for iOS). Cache cleanup runs automatically to manage database size. There's room for improvement with image caching strategies."

**Q: How do you handle errors?**
> "Each ViewModel has consistent error handling. We classify errors as network or unknown using utility functions. We use optimistic UI updates where appropriate, and revert them if operations fail. Error states are part of the UI state, so users see clear error messages with retry options."

**Q: What would you do differently?**
> "I'd add more comprehensive test coverage, especially for edge cases. I'd also implement a proper logging framework instead of `println`. For production, I'd add analytics and crash reporting."

---

## Time Management Tips

- **Total time: 30-45 minutes**
- **Introduction: 2-3 min** (keep it brief)
- **Architecture: 3-4 min** (high-level overview)
- **Code walkthrough: 10-12 min** (main focus)
- **Demo: 3-4 min** (show it works)
- **Testing: 3-4 min** (show quality)
- **Q&A: 10-15 min** (be ready for questions)

**Pro Tips:**
- ✅ Start with the big picture, then dive into details
- ✅ Show actual code, don't just talk about it
- ✅ Be ready to jump to any part if asked
- ✅ If running short on time, skip "What Could Be Improved"
- ✅ If you have extra time, show more code examples

---

## Red Flags to Avoid

❌ **Don't**: Read code line-by-line
❌ **Don't**: Apologize for missing features excessively
❌ **Don't**: Get defensive about code choices
❌ **Don't**: Skip the demo (shows you didn't test it)
❌ **Don't**: Memorize - understand and explain naturally

---

## Green Flags to Show

✅ **Do**: Explain the "why" behind decisions
✅ **Do**: Show problem-solving (challenges & solutions)
✅ **Do**: Demonstrate understanding of trade-offs
✅ **Do**: Be honest about limitations
✅ **Do**: Connect code to real-world scenarios
✅ **Do**: Show enthusiasm for the project

---

## Quick Reference Checklist

Before the interview, make sure you can:
- [ ] Run the app on Android/iOS
- [ ] Explain the architecture in 2 minutes
- [ ] Walk through a feature from UI to database
- [ ] Show a test and explain the testing strategy
- [ ] Demonstrate cross-screen synchronization (favorites)
- [ ] Explain platform-specific code (`expect`/`actual`)
- [ ] Answer "Why KMP?" confidently
- [ ] Discuss trade-offs and improvements

---

## Final Tips

1. **Practice the flow** - Walk through it once before the interview
2. **Have code ready** - Open key files in your IDE
3. **Be flexible** - Interviewers may want to dive deep into specific areas
4. **Show enthusiasm** - This is your project, be proud of it!
5. **Ask questions** - Show interest in their tech stack and challenges

Good luck! 🚀

