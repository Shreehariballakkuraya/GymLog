# GymLog

## Overview
GymLog is an Android application designed to assist users in tracking their fitness journey. It provides a comprehensive solution for logging workouts, monitoring nutrition, tracking daily activity, and recording weight progress. The application is built using modern Android development practices, including Kotlin, MVVM architecture, and Room Database for robust local data persistence.

## Key Features

### Workout Tracking
*   Log exercises with customizable sets, reps, and weights.
*   Support for both repetition-based and duration-based (timed) exercises.
*   Built-in exercise presets with the ability to add and persist custom exercises.

### Automated Exercise Reminders
*   Configurable reminder intervals using Android WorkManager for reliable background execution.
*   Support for two distinct exercise modes:
    *   **Repetition Mode:** Guides users through sets with a predefined pace and audible cues.
    *   **Hold Timer Mode:** Provides a countdown timer for static holds (e.g., Planks, Wall Sits).
*   Interactive reminder session activity that automatically logs completed exercises to the user's history.

### Nutrition and Macro Tracking
*   Log daily meals and monitor caloric intake.
*   Visual macro tracking (Protein, Carbohydrates, Fat) using custom progress indicators.

### Progress and Activity
*   Track daily steps and active minutes.
*   Record and visualize body weight changes over time.
*   Maintain a streak counter to encourage consistent application usage.

## Technical Architecture
*   **Language:** Kotlin
*   **Architecture Pattern:** Model-View-ViewModel (MVVM)
*   **Database:** Room Database (SQLite abstraction)
*   **Background Tasks:** WorkManager for reliable scheduling of periodic notifications.
*   **UI Components:** ViewBinding, Material Design Components.

## Minimum Requirements
*   Android Minimum SDK: API Level 26 (Android 8.0 Oreo)
*   Target SDK: Latest Android API version.
