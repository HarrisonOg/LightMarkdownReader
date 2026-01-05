# LightMarkdownReader

A lightweight, fast, and modern markdown reader for Android. LightMarkdownReader provides a clean interface for reading markdown files on your Android device, with support for persistent file access and recent file tracking.

## Features

- **Markdown Rendering**: Rich markdown rendering using Markwon library
- **Recent Files**: Automatic tracking of recently opened files with validation and cleanup
- **Persistent Permissions**: Maintains access to files across app restarts
- **File Sharing**: Share markdown files directly from the app
- **Error Handling**: Comprehensive error handling with user-friendly error messages
- **Material Design 3**: Modern UI following Material Design 3 guidelines
- **Edge-to-Edge Display**: Immersive full-screen experience

## Architecture

LightMarkdownReader follows the **MVVM (Model-View-ViewModel)** architecture pattern with a clean separation of concerns:

### Architecture Layers

```
┌─────────────────────────────────────────────────┐
│                 UI Layer                         │
│  - ReaderScreen (Jetpack Compose)               │
│  - MarkdownRenderer                              │
│  - TopBar & Components                           │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│              ViewModel Layer                     │
│  - ReaderViewModel                               │
│  - UI State Management (StateFlow)               │
│  - Recent Files State                            │
└─────────────────┬───────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────┐
│               Data Layer                         │
│  - FileRepository (File I/O)                    │
│  - RecentFilesRepository (Persistence)          │
│  - Data Models (MarkdownFile, RecentFile)       │
└─────────────────────────────────────────────────┘
```

### Key Components

#### UI Layer (`ui/`)
- **ReaderScreen**: Main composable screen that orchestrates the UI
- **MarkdownRenderer**: Handles rendering markdown content using Markwon
- **TopBar**: Navigation and action bar component
- **Theme**: Material Design 3 theming (Color, Type, Theme)

#### ViewModel Layer (`viewmodel/`)
- **ReaderViewModel**: Manages UI state and business logic
  - Handles file loading with proper error states
  - Manages recent files list
  - Exposes `StateFlow` for reactive UI updates

#### Data Layer (`data/`)
- **FileRepository**: Handles file system operations
  - Reads markdown files using Android ContentResolver
  - Validates file size (10MB limit)
  - Provides file metadata (name, size)
- **RecentFilesRepository**: Manages recent files persistence
  - Stores recent files using SharedPreferences
  - Validates file accessibility
  - Handles migration from legacy formats
- **Models**: Data classes for MarkdownFile and RecentFile

#### Utils (`util/`)
- **TimeFormatter**: Formats timestamps for recent files display

### State Management

The app uses **Kotlin StateFlow** for reactive state management:
- `ReaderUiState`: Represents the current state (Empty, Loading, Success, Error)
- `recentFiles`: Flow of recent files list

### File Access

Uses Android's **Storage Access Framework (SAF)** for secure file access:
- Persistent URI permissions for ongoing access
- Content resolver for reading files
- Handles permission revocation gracefully

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Architecture**: MVVM
- **Dependency Injection**: Manual DI (lightweight)
- **Markdown Library**: Markwon
- **Persistence**: SharedPreferences (JSON via Gson)
- **Testing**: JUnit, MockK, Turbine, Coroutines Test

## Project Structure

```
app/src/main/java/com/harrisonog/lightmarkdownreader/
├── MainActivity.kt              # App entry point
├── ui/
│   ├── screens/
│   │   └── ReaderScreen.kt     # Main screen composable
│   ├── components/
│   │   ├── MarkdownRenderer.kt # Markdown rendering
│   │   └── TopBar.kt           # Top app bar
│   └── theme/                  # Material Design theme
├── viewmodel/
│   └── ReaderViewModel.kt      # Main view model
├── data/
│   ├── FileRepository.kt       # File operations
│   ├── RecentFilesRepository.kt# Recent files management
│   ├── MarkdownFile.kt         # Data model
│   └── RecentFile.kt           # Recent file model
└── util/
    └── TimeFormatter.kt        # Time formatting utilities
```

## Building

```bash
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test
```
