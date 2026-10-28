<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

Firebase integration: see [architecture and mock-data audit](ARCHITECTURE.md) and the [Firebase setup guide](FIREBASE_SETUP.md). This is a native Kotlin/Compose Android project. The existing APK predates the integration.

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/5cb9b73b-dd20-4d6c-9c9d-5b480af69481

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a Firebase project and add its `google-services.json` to `app/`, following `FIREBASE_SETUP.md`. Private API keys must remain in server Secret Manager, never the APK.
5. Install the required Android SDK and Gradle toolchain. Standard local debug signing is already configured.
6. Run the app on an emulator or physical device
7. If you have already published your app in AI Studio, please [request upload key reset](https://support.google.com/googleplay/android-developer/answer/9842756#zippy=%2Crequest-an-upload-key-reset) in Google Play Console.
