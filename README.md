# GWS APP

GWS APP is a mood logging app that helps users to log their moods, view their mood history, gain insights from mood data, as well as get appropriate activity recommendations. The app is designed to improve users' mental well-being by providing useful insights.

## Key Features
1. **Save Mood**: Users can easily log their moods using various mood options.
2. **Mood History**: View the history of previously logged moods.
3. **Mood Insights**: Analyze mood patterns based on the user's historical data.
4. **Activity Recommendations**: Based on the mood, the app provides activity recommendations that can improve the user's mood.
5. **Chatbot**: Equipped with a machine learning-based chatbot feature that uses the TensorFlow Lite (TFLite) model.

## Technology Used
- **Programming Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Data Storage**: Local Storage (SharedPreferences or Room Database)
- **UI**: Material UI & XML
- **Machine Learning**: TensorFlow Lite (TFLite) for chatbot feature
- **Cloud**: Data synchronization with Google Cloud

## How to use
1. Download and install the app.
2. Enter **USER_ID_KEY** to access the app (no login required at this time).
3. Start logging your moods and explore other features.

## Installation
1. Clone this repository:
   ``bash
   git clone https://github.com/gws-app/gws-mobile.git
   ```
2. Open the project using Android Studio.
3. Make sure the SDK and dependencies are installed.
4. Run the app on an emulator or physical device.

## Future Enhancements
- **Login with Account**: Implement login using email or social media for a more personalized experience.
- **Gamification**: Add points or badges based on positive mood achievements.
- **Automatic Reminders**: Reminder feature to record moods regularly.
- Wearable Device Integration**: Synchronize with wearable devices to automatically track mental health.
- **Backup and Restore**: Features to easily backup and restore user data.
