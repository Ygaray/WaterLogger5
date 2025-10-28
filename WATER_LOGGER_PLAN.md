# Water Logger App - Complete Development Plan

**Project Type:** Personal Android App
**Purpose:** Track daily water intake with home screen widget and automatic daily email reporting
**Development Environment:** Android Studio
**Last Updated:** 2025-10-24

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Architecture & Project Structure](#architecture--project-structure)
4. [Database Design](#database-design)
5. [Implementation Phases](#implementation-phases)
6. [Key Components](#key-components)
7. [Dependencies](#dependencies)
8. [Best Practices](#best-practices)
9. [Getting Started in Android Studio](#getting-started-in-android-studio)

---

## Project Overview

### Core Requirements
- Personal water intake tracking app for Android
- **Home screen widget** with three quick-add buttons: 500ml, 300ml, 1000ml
- Store all historical data in local database
- **Automatic daily email** sent at 5:00 AM with previous day's total
- Settings screen to configure email preferences
- History view to browse past entries

### Key Features
1. Main app showing today's total water intake
2. Quick-add buttons in both app and widget
3. Room database storing complete history
4. Automatic email at 5 AM with yesterday's total
5. History screen with daily breakdown
6. Settings screen for email configuration

---

## Technology Stack

### Core Technologies
- **Language:** Kotlin
- **Min SDK:** 26 (Android 8.0) - for Widget and WorkManager support
- **Target SDK:** 34 (Android 14)

### UI & Architecture
- **UI Framework:** Jetpack Compose + Material3
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Navigation:** Jetpack Navigation Compose
- **Dependency Injection:** Hilt

### Data & Storage
- **Database:** Room Database
- **Preferences:** DataStore (for settings)
- **Reactive Streams:** Kotlin Flow + StateFlow

### Background & Scheduling
- **Background Work:** WorkManager (for daily email)
- **Widget:** Android App Widget API

### Email
- **Email Service:** JavaMail API (automatic sending)
- **Protocol:** SMTP (configure with your email provider)

### Additional Libraries
- **Coroutines:** Kotlin Coroutines for async operations
- **Lifecycle:** AndroidX Lifecycle (ViewModel, LiveData)
- **Date/Time:** Java Time API (available on Android API 26+)

---

## Architecture & Project Structure

### MVVM + Clean Architecture

```
app/src/main/java/com/yourname/waterlogger/
│
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── WaterDatabase.kt          # Room Database
│   │   │   ├── WaterDao.kt               # Data Access Object
│   │   │   └── entity/
│   │   │       ├── WaterEntry.kt         # Entry entity
│   │   │       └── DailySummary.kt       # Daily summary entity
│   │   └── datastore/
│   │       └── SettingsManager.kt        # DataStore for settings
│   └── repository/
│       └── WaterRepository.kt            # Repository interface + impl
│
├── domain/
│   ├── model/
│   │   └── WaterIntake.kt                # UI model (if needed)
│   └── usecase/
│       ├── AddWaterUseCase.kt            # Add water entry
│       ├── GetTodayTotalUseCase.kt       # Get today's total
│       ├── GetYesterdayTotalUseCase.kt   # For email
│       └── GetHistoryUseCase.kt          # Get history data
│
├── di/
│   ├── AppModule.kt                      # Hilt app-level dependencies
│   ├── DatabaseModule.kt                 # Room + DAO injection
│   └── RepositoryModule.kt               # Repository injection
│
├── ui/
│   ├── MainActivity.kt                   # Single activity
│   ├── navigation/
│   │   └── NavGraph.kt                   # Navigation setup
│   ├── main/
│   │   ├── MainScreen.kt                 # Home screen UI
│   │   └── MainViewModel.kt              # Home screen logic
│   ├── history/
│   │   ├── HistoryScreen.kt              # History UI
│   │   └── HistoryViewModel.kt           # History logic
│   ├── settings/
│   │   ├── SettingsScreen.kt             # Settings UI
│   │   └── SettingsViewModel.kt          # Settings logic
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── widget/
│   ├── WaterWidgetProvider.kt            # Widget provider
│   ├── WaterWidgetReceiver.kt            # Handle button clicks
│   └── WaterWidgetUpdater.kt             # Update widget UI
│
├── worker/
│   └── DailyEmailWorker.kt               # WorkManager for 5 AM email
│
├── util/
│   ├── DateUtils.kt                      # Date formatting helpers
│   ├── EmailHelper.kt                    # JavaMail wrapper
│   └── Constants.kt                      # App constants
│
└── WaterLoggerApplication.kt             # Application class (Hilt entry point)
```

---

## Database Design

### Entity: WaterEntry

```kotlin
@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "amount_ml")
    val amountMl: Int,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "date", index = true) // Index for faster queries
    val date: String, // Format: YYYY-MM-DD

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

### Entity: DailySummary (Optional but Recommended)

```kotlin
@Entity(tableName = "daily_summary")
data class DailySummary(
    @PrimaryKey
    val date: String, // Format: YYYY-MM-DD

    @ColumnInfo(name = "total_ml")
    val totalMl: Int,

    @ColumnInfo(name = "entry_count")
    val entryCount: Int,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long
)
```

### DAO Interface

```kotlin
@Dao
interface WaterDao {
    // Insert operations
    @Insert
    suspend fun insertEntry(entry: WaterEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailySummary(summary: DailySummary)

    // Query today's total (reactive)
    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entries WHERE date = :date")
    fun getTotalForDate(date: String): Flow<Int>

    // Query entries for specific date
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getEntriesForDate(date: String): List<WaterEntry>

    // Query all daily summaries (for history)
    @Query("SELECT * FROM daily_summary ORDER BY date DESC")
    fun getAllDailySummaries(): Flow<List<DailySummary>>

    // Get yesterday's total (for email)
    @Query("SELECT COALESCE(SUM(amount_ml), 0) FROM water_entries WHERE date = :date")
    suspend fun getTotalForDateSync(date: String): Int

    // Transaction: Add entry and update summary
    @Transaction
    suspend fun addWaterIntakeTransaction(entry: WaterEntry) {
        insertEntry(entry)
        // Update daily summary
        val total = getTotalForDateSync(entry.date)
        val count = getEntriesForDate(entry.date).size
        insertDailySummary(
            DailySummary(
                date = entry.date,
                totalMl = total,
                entryCount = count,
                lastUpdated = System.currentTimeMillis()
            )
        )
    }
}
```

### Database Class

```kotlin
@Database(
    entities = [WaterEntry::class, DailySummary::class],
    version = 1,
    exportSchema = false
)
abstract class WaterDatabase : RoomDatabase() {
    abstract fun waterDao(): WaterDao

    companion object {
        const val DATABASE_NAME = "water_logger_db"
    }
}
```

---

## Implementation Phases

### Phase 1: Project Setup & Database (2-3 hours)

**Tasks:**
1. Create new Android Studio project
   - Template: Empty Activity
   - Language: Kotlin
   - Minimum SDK: API 26
   - Build configuration: Kotlin DSL (build.gradle.kts)

2. Add all dependencies to `build.gradle.kts`

3. Set up Hilt
   - Create `WaterLoggerApplication.kt` with `@HiltAndroidApp`
   - Add Hilt modules

4. Implement Room Database
   - Create entities: `WaterEntry`, `DailySummary`
   - Create DAO: `WaterDao`
   - Create Database: `WaterDatabase`
   - Set up Hilt module for database injection

5. Create Repository layer
   - `WaterRepository` interface
   - Implementation with proper Flow handling

**Validation:** Successfully insert and query data from database

---

### Phase 2: Main App UI (3-4 hours)

**Tasks:**
1. Set up Jetpack Compose with Material3 theme
2. Create `MainScreen.kt` with:
   - Large display showing today's total
   - Three quick-add buttons (500ml, 300ml, 1000ml)
   - Navigation to History and Settings
3. Create `MainViewModel.kt`
   - StateFlow for UI state
   - Functions to add water intake
   - Collect today's total from repository
4. Wire up dependency injection
5. Test adding water and seeing total update

**Validation:** Can add water via buttons, see total update in real-time

---

### Phase 3: Settings Screen (2-3 hours)

**Tasks:**
1. Set up DataStore for preferences
2. Create `SettingsManager.kt` to handle:
   - Email address storage
   - Email time preference (default 5:00 AM)
   - SMTP configuration
3. Create `SettingsScreen.kt` with:
   - Email input field
   - SMTP server configuration
   - Test email button
4. Create `SettingsViewModel.kt`
5. Add navigation from main screen

**Validation:** Can save and retrieve settings

---

### Phase 4: Home Screen Widget (4-5 hours)

**Tasks:**
1. Create widget layout XML (`widget_layout.xml`)
   - Three buttons: 500ml, 300ml, 1000ml
   - TextView showing today's total
2. Create `WaterWidgetProvider.kt`
   - Extend `AppWidgetProvider`
   - Handle widget updates
3. Create `WaterWidgetReceiver.kt`
   - Handle button click broadcasts
   - Add water to database
   - Trigger widget update
4. Add widget to manifest
5. Implement widget update mechanism
   - Update when data changes
   - Use `RemoteViews` properly
6. Test widget installation and button clicks

**Important Widget Notes:**
- Widgets run in a separate process
- Use `PendingIntent` with `FLAG_IMMUTABLE`
- Keep widget logic minimal
- Widget should update when app adds water too

**Validation:** Widget buttons add water, total updates, survives device reboot

---

### Phase 5: History Screen (2-3 hours)

**Tasks:**
1. Create `HistoryScreen.kt` with:
   - LazyColumn showing daily summaries
   - Date, total ml, entry count
   - Click to expand and see detailed entries
2. Create `HistoryViewModel.kt`
   - Collect daily summaries from repository
3. Add navigation from main screen
4. Optional: Add filtering/sorting

**Validation:** Can view past days and their totals

---

### Phase 6: Email Automation (4-5 hours)

**Tasks:**
1. Add JavaMail API dependencies
2. Create `EmailHelper.kt`
   - Function to send email with JavaMail
   - Email template: "Total water consumed on [date]: [X]ml"
   - Handle SMTP authentication
3. Create `DailyEmailWorker.kt`
   - Extend `Worker` or `CoroutineWorker`
   - Get yesterday's total from repository
   - Format and send email
   - Handle errors and retry
4. Set up WorkManager scheduling
   - Calculate delay to next 5:00 AM
   - Use `PeriodicWorkRequest` with 24-hour interval
   - Add network constraint
   - Set up exponential backoff for retries
5. Initialize WorkManager in `WaterLoggerApplication.kt`
6. Add notification for successful email send

**Email Format Example:**
```
Subject: Water Intake - October 23, 2025

Total water consumed on October 23, 2025: 2300ml

---
Sent from Water Logger App
```

**WorkManager Setup:**
```kotlin
// Calculate initial delay to next 5:00 AM
fun calculateDelayToNext5AM(): Long {
    val now = LocalDateTime.now()
    var next5AM = now.with(LocalTime.of(5, 0))

    if (now.isAfter(next5AM)) {
        next5AM = next5AM.plusDays(1)
    }

    return Duration.between(now, next5AM).toMillis()
}

// Schedule work
val constraints = Constraints.Builder()
    .setRequiredNetworkType(NetworkType.CONNECTED)
    .build()

val dailyEmailWork = PeriodicWorkRequestBuilder<DailyEmailWorker>(
    repeatInterval = 1,
    repeatIntervalTimeUnit = TimeUnit.DAYS
)
    .setInitialDelay(calculateDelayToNext5AM(), TimeUnit.MILLISECONDS)
    .setConstraints(constraints)
    .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        WorkRequest.MIN_BACKOFF_MILLIS,
        TimeUnit.MILLISECONDS
    )
    .addTag("daily_email")
    .build()

WorkManager.getInstance(context)
    .enqueueUniquePeriodicWork(
        "daily_email",
        ExistingPeriodicWorkPolicy.KEEP,
        dailyEmailWork
    )
```

**Validation:** Email sends at 5 AM with correct total, retries on failure

---

### Phase 7: Testing & Polish (2-3 hours)

**Tasks:**
1. End-to-end testing
   - Add water via app → widget updates
   - Add water via widget → app updates
   - Check email scheduling works
2. Edge case testing
   - What happens at midnight (date change)
   - Email send failure handling
   - Widget behavior on reboot
3. Performance optimization
   - Check database query performance
   - Ensure widget updates are efficient
4. UI polish
   - Consistent theming
   - Loading states
   - Error messages
5. Add app icon
6. Write basic README

**Validation:** All features work smoothly, no crashes

---

## Key Components

### 1. Repository Pattern

```kotlin
class WaterRepositoryImpl @Inject constructor(
    private val waterDao: WaterDao
) : WaterRepository {

    override fun getTodayTotal(): Flow<Int> {
        val today = LocalDate.now().toString()
        return waterDao.getTotalForDate(today)
    }

    override suspend fun addWaterIntake(amountMl: Int) {
        val entry = WaterEntry(
            amountMl = amountMl,
            timestamp = System.currentTimeMillis(),
            date = LocalDate.now().toString()
        )
        waterDao.addWaterIntakeTransaction(entry)
    }

    override suspend fun getYesterdayTotal(): Int {
        val yesterday = LocalDate.now().minusDays(1).toString()
        return waterDao.getTotalForDateSync(yesterday)
    }

    override fun getDailySummaries(): Flow<List<DailySummary>> {
        return waterDao.getAllDailySummaries()
    }
}
```

### 2. ViewModel Example

```kotlin
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WaterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTodayTotal().collect { total ->
                _uiState.update { it.copy(todayTotal = total) }
            }
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            try {
                repository.addWaterIntake(amountMl)
                // Widget will be updated via broadcast
                updateWidget()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to add water: ${e.message}")
                }
            }
        }
    }

    private fun updateWidget() {
        // Trigger widget update
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
        // Send broadcast to widget
    }
}

data class MainUiState(
    val todayTotal: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 3. Widget Implementation

```kotlin
class WaterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)

        // Get today's total (need to do this in background)
        // For now, we'll show a placeholder or use WorkManager

        // Set up button click intents
        val intent500 = Intent(context, WaterWidgetReceiver::class.java).apply {
            action = ACTION_ADD_WATER
            putExtra(EXTRA_AMOUNT, 500)
        }
        val pendingIntent500 = PendingIntent.getBroadcast(
            context, 0, intent500,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.button_500ml, pendingIntent500)

        // Repeat for 300ml and 1000ml buttons

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_ADD_WATER = "com.yourname.waterlogger.ADD_WATER"
        const val EXTRA_AMOUNT = "amount"
    }
}

class WaterWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WaterWidgetProvider.ACTION_ADD_WATER) {
            val amount = intent.getIntExtra(WaterWidgetProvider.EXTRA_AMOUNT, 0)

            // Add water to database (use WorkManager or CoroutineScope)
            // Then update widget

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, WaterWidgetProvider::class.java)
            )

            // Trigger widget update
            val updateIntent = Intent(context, WaterWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(updateIntent)
        }
    }
}
```

### 4. Email Worker

```kotlin
class DailyEmailWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get repository (inject via Hilt or WorkManager's DelegatingWorkerFactory)
            val repository = // ... get repository instance
            val settingsManager = // ... get settings

            // Get yesterday's total
            val yesterday = LocalDate.now().minusDays(1)
            val total = repository.getYesterdayTotal()

            // Get email settings
            val emailAddress = settingsManager.getEmailAddress()
            val smtpConfig = settingsManager.getSmtpConfig()

            // Send email
            EmailHelper.sendDailyReport(
                to = emailAddress,
                date = yesterday,
                totalMl = total,
                smtpConfig = smtpConfig
            )

            // Show success notification
            showNotification("Daily email sent successfully")

            Result.success()
        } catch (e: Exception) {
            Log.e("DailyEmailWorker", "Failed to send email", e)
            Result.retry()
        }
    }

    private fun showNotification(message: String) {
        // Create and show notification
    }
}
```

---

## Dependencies

### build.gradle.kts (Project level)

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

### build.gradle.kts (App level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.yourname.waterlogger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourname.waterlogger"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Compose tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt Dependency Injection
    val hiltVersion = "2.48"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore (for settings)
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // JavaMail for email
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Optional: Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

### AndroidManifest.xml additions

```xml
<manifest>
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application>
        <!-- Widget -->
        <receiver
            android:name=".widget.WaterWidgetProvider"
            android:exported="true">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
            </intent-filter>
            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/water_widget_info" />
        </receiver>

        <receiver
            android:name=".widget.WaterWidgetReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.yourname.waterlogger.ADD_WATER" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

---

## Best Practices

### Code Organization
- **Single Responsibility:** Each class should have one clear purpose
- **Dependency Injection:** Use Hilt for all dependencies
- **Separation of Concerns:** UI, business logic, and data layers are separate

### Database
- Use `suspend` functions for one-shot operations
- Use `Flow` for reactive/observable data
- Always use transactions for multi-step operations
- Index frequently queried columns (like `date`)

### UI (Jetpack Compose)
- Use `remember` to avoid unnecessary recomposition
- Hoist state to ViewModels, not in Composables
- Use `LaunchedEffect` for side effects
- Collect flows with `collectAsStateWithLifecycle()`

### Background Work
- WorkManager for guaranteed execution (email)
- Use appropriate constraints (network for email)
- Implement proper retry logic
- Test behavior across device reboots

### Widget
- Keep widget code minimal and fast
- Use `PendingIntent.FLAG_IMMUTABLE` (required for API 31+)
- Test widget across different launcher apps
- Widget updates should be lightweight

### Error Handling
- Always wrap network/email operations in try-catch
- Provide user-friendly error messages
- Log errors for debugging (use Timber)
- Implement retry mechanisms for critical operations

### Testing Checklist
- [ ] Add water via app → widget updates
- [ ] Add water via widget → app updates
- [ ] Email sends at correct time with correct data
- [ ] Email retries on failure
- [ ] Date changes at midnight correctly
- [ ] Widget survives device reboot
- [ ] Settings are saved and loaded correctly
- [ ] History displays all past entries
- [ ] App doesn't crash on edge cases (no internet, etc.)

---

## Getting Started in Android Studio

### Step 1: Create Project
1. Open Android Studio
2. File → New → New Project
3. Select "Empty Activity"
4. Configure:
   - Name: Water Logger
   - Package: com.yourname.waterlogger
   - Language: Kotlin
   - Minimum SDK: API 26
   - Build configuration: Kotlin DSL
5. Click Finish

### Step 2: Set Up Dependencies
1. Open `build.gradle.kts` (Project level)
2. Add plugin versions (see Dependencies section)
3. Open `build.gradle.kts` (App level)
4. Add all dependencies (see Dependencies section)
5. Sync Gradle

### Step 3: Enable Hilt
1. Create `WaterLoggerApplication.kt`
```kotlin
@HiltAndroidApp
class WaterLoggerApplication : Application()
```
2. Update `AndroidManifest.xml`
```xml
<application
    android:name=".WaterLoggerApplication"
    ...>
```

### Step 4: Follow Implementation Phases
Work through phases 1-7 in order (see Implementation Phases section)

### Step 5: Testing
- Use Android Emulator or physical device
- Test widget: Long-press home screen → Add widget
- Test email: Temporarily change time to 1 minute from now for testing
- Check logs: Android Studio → Logcat

---

## Important Notes

### SMTP Configuration (for JavaMail)
You'll need to configure SMTP settings for your email provider:

**Gmail Example:**
- SMTP Server: smtp.gmail.com
- Port: 587 (TLS) or 465 (SSL)
- Authentication: Yes
- Username: your-email@gmail.com
- Password: App-specific password (not your regular password)

**Note:** Gmail requires "App Passwords" if you have 2FA enabled. Generate one at: https://myaccount.google.com/apppasswords

### Widget Testing Tips
- Widgets can be tricky to debug
- Use `Log.d()` extensively in widget code
- Test on multiple launchers (stock Android, Samsung, etc.)
- Always test after device reboot

### WorkManager Testing
- To test email scheduling without waiting 24 hours:
  - Change initial delay to 1-2 minutes
  - Run app, wait for worker to execute
  - Check Logcat for worker execution logs
- Use WorkManager's testing library for unit tests

### Date/Time Handling
- Always use `LocalDate.now()` for current date
- Store dates as strings in ISO format (YYYY-MM-DD)
- Handle timezone considerations if needed
- Be careful around midnight transitions

---

## Next Steps After Implementation

### Future Enhancements (Optional)
1. **Goal Setting:** Set daily water intake goal (e.g., 2000ml)
2. **Progress Bar:** Show progress toward daily goal
3. **Notifications:** Remind user to drink water
4. **Charts:** Visualize weekly/monthly intake
5. **Custom Amounts:** Allow user to add custom amounts
6. **Dark Mode:** Support system dark mode
7. **Undo Feature:** Undo last entry if added by mistake
8. **Export Data:** Export history to CSV
9. **Multiple Beverages:** Track different types of drinks
10. **Cloud Backup:** Optional backup to cloud

### Deployment
1. Generate signed APK/AAB
2. Test on multiple devices
3. Create app icon and screenshots
4. Optionally publish to Play Store (or keep personal)

---

## Troubleshooting

### Common Issues

**Issue:** Widget buttons not responding
- **Solution:** Check PendingIntent flags (must include FLAG_IMMUTABLE)
- **Solution:** Verify BroadcastReceiver is registered in manifest
- **Solution:** Check Logcat for errors

**Issue:** Email not sending
- **Solution:** Verify internet permission in manifest
- **Solution:** Check SMTP credentials are correct
- **Solution:** Use app-specific password for Gmail
- **Solution:** Check WorkManager constraints (network required)

**Issue:** Database queries slow
- **Solution:** Add index to `date` column in WaterEntry
- **Solution:** Use transactions for bulk operations
- **Solution:** Profile queries with Database Inspector

**Issue:** Widget not updating when app adds water
- **Solution:** Send broadcast to widget after adding data
- **Solution:** Use `AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged()`

---

## Resources

### Official Documentation
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [App Widgets](https://developer.android.com/guide/topics/appwidgets)

### JavaMail Resources
- [JavaMail API Guide](https://javaee.github.io/javamail/)
- [Android JavaMail Example](https://github.com/jaredrummler/AndroidMail)

---

## Summary

This is a comprehensive personal water tracking app with:
- ✅ Home screen widget with quick-add buttons (500ml, 300ml, 1000ml)
- ✅ Local Room database for complete history
- ✅ Automatic daily email at 5:00 AM with previous day's total
- ✅ Settings screen for email configuration
- ✅ History view of past entries
- ✅ Clean architecture with Hilt DI
- ✅ Modern Android development practices

**Estimated Development Time:** 20-25 hours

**Good luck with your project! Take it one phase at a time, and don't hesitate to test frequently.**
