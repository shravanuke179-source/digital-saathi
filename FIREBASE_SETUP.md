# Firebase setup for Digital Saathi

No Firebase project has been created or deployed by this change. These steps connect the existing native Android UI to your new project.

## 1. Create and register the application

1. Open [Firebase Console](https://console.firebase.google.com/) and create a project. Choose your project ID and database region deliberately; there is no project ID hardcoded in this repository.
2. Add an Android app with package **`com.aistudio.digitalsaathi.vlgxyz`** (the `applicationId`, not the Kotlin namespace `com.example`).
3. Register the SHA-1 and SHA-256 fingerprints of the debug/release signing certificates. For Google Play releases, register the Play App Signing certificate as well. Obtain local fingerprints with the Gradle `signingReport` task once the toolchain is installed.
4. Download the generated configuration to **`app/google-services.json`**. Do not invent this file or handwrite OAuth IDs. It is ignored by Git to avoid connecting another developer to the wrong project.
5. In Authentication → Sign-in method, enable **Email/Password**, **Google**, and **Anonymous** (for the preserved Guest feature). Configure Google support email. Set a password policy of at least eight characters.
6. Download `google-services.json` again after enabling Google. It must include the web OAuth client used by Credential Manager (`default_web_client_id`). A native Google account picker requires a device/emulator with Google Play services.
7. Customize Authentication email templates for password reset. Persistent sign-in is provided by Firebase Auth; logout calls `signOut` and clears private screen state. Guest accounts are separate from named accounts; automatic guest-to-permanent-account data migration is not implemented.

See [Firebase Android Google authentication](https://firebase.google.com/docs/auth/android/google-signin) for certificate and OAuth details.

## 2. Create Firestore with restricted access

Create Cloud Firestore in **production/locked mode**. Do not enable public test-mode access. Install the repository's development tools with Node 22 or newer:

```powershell
npm install
npx firebase login
npx firebase use --add
npx firebase deploy --only firestore:rules,firestore:indexes --project YOUR_PROJECT_ID
```

The supplied rules deny all unlisted paths, isolate records by Firebase UID, validate supported data shapes, reject client-written passwords/roles/verification and reserve authoritative statuses for server code. Profile email and sign-in provider come from Firebase Auth. Applications retain only optional Aadhaar **last four digits**, never a full identity number. The UID in the document path provides profile association.

Catalogs initially remain empty. Publish real buyers, equipment, delivery partners, village notices and dated market prices using trusted server tooling or Firebase Console. Use the matching data classes in `app/src/main/java/com/example/data/model/Models.kt` as the field schema. Enum values must use their Kotlin names (e.g. `TRACTORS`); string pairs use `{ "first": "...", "second": "..." }`. Optional null fields may be omitted. Document IDs must match each record's `id` field. Use actual providers and verified prices; do not promote the bundled demo fixtures to production data.

Profile identity fields (`isVerified`, `rationCardNo`, `bankLinked`) are optional trusted fields set only after a real verification process. A successful Google sign-in is not Aadhaar verification. Ordinary users cannot publish official notices; the existing form reports permission errors when the account lacks an admin role.

Rules are the authorization boundary for Android clients: [Firestore security conditions](https://firebase.google.com/docs/firestore/security/rules-conditions).

## 3. Trusted backend and App Check

The Android export contained no server runtime. A Firebase-managed Node.js 22 function has been added only for trusted delivery transitions. Deploying Cloud Functions requires the appropriate Firebase billing plan and project permissions; nothing has been purchased or deployed here.

```powershell
npm --prefix functions install
npm --prefix functions test
npx firebase deploy --only functions --project YOUR_PROJECT_ID
```

`updateDeliveryStatus` is deployed in `asia-south1`. Keep the Kotlin `FirebaseFunctions.getInstance("asia-south1")` region consistent if you change it. Its payload is `{ uid, deliveryId, status }`. It requires a Firebase-authenticated operator with `admin: true` custom claim, and a valid App Check token. An ordinary customer cannot advance delivery status. Claims must be provisioned by trusted Admin SDK tooling, never from a document editable by the user. Sign in again after claims change.

Register the Android app with App Check → Play Integrity, including release SHA-256. Debug builds install Firebase's debug App Check provider; register the debug token shown in Android Logcat in the Firebase Console. Never use debug tokens in release builds. The callable enforces App Check. Enable Firestore/Auth App Check enforcement only after you have confirmed valid tokens from your supported devices.

The function uses the managed service account and Firebase Admin SDK. There is no service-account key or third-party API secret in Android code. The old Secrets Gradle plugin integration has been removed so `.env` secrets cannot accidentally become APK `BuildConfig` fields. Firebase's client API key/OAuth identifiers in `google-services.json` are public app configuration, not privileged server credentials; they do not replace security rules. Any future private provider API keys belong in server Secret Manager and must be explicitly bound to the functions that use them.

The Android tracking action invokes the callable; all screens receive the resulting changes through Firestore listeners. Booking requests/costs remain requests/estimates, not paid or provider-confirmed reservations. Official scheme/grievance submission needs a separate real provider API or the official portal.

See [Firebase callable functions](https://firebase.google.com/docs/functions/callable) for token verification and deployment behavior.

## 4. Android build requirements

Use Android Studio with JDK 17+ and toolchain support for the existing AGP 9.1.1 configuration. The project expects Gradle **9.3.1** and Android SDK **36.1**. The available local SDK discovered during this task contains only API 34. Do not downgrade the application merely to fit that SDK.

The source export was missing `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar`. Generate the official wrapper with an installed compatible Gradle distribution, or let Android Studio provision the required Gradle version. Do not download wrapper JARs from unofficial sources.

```powershell
gradle wrapper --gradle-version 9.3.1
.\gradlew.bat :app:signingReport
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Install Android SDK 36.1 and the requested build tools from Android Studio's SDK Manager. Set your local SDK path through Android Studio or ignored `local.properties`. Debug builds use the standard local debug keystore rather than the absent AI Studio `debug.keystore`. Release signing still uses `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` from your release environment. The pre-existing APK is not the updated app.

## 5. Verification

```powershell
node --test functions/workflow.test.js
npm test
```

`npm test` starts local Auth and Firestore emulators using **`demo-digital-saathi`**, a demo ID that cannot write to a production project. Java 21+ is needed by current Firestore emulator versions; select a compatible installed Java version if your system default differs. Tests cover registration, sign-in, wrong passwords, logout, password-reset requests, profile create/read/update, produce and bookmark CRUD, cross-account/unauthenticated denial, status/role forgery, atomic deliveries, and booking cancellation. They do not prove native Credential Manager or Android process-restoration behavior.

On a configured Android device, additionally verify:

1. Register, confirm the UID-owned profile, edit it, and relaunch the app to check persistent authentication/data.
2. Sign out, confirm private dashboards disappear, sign in with another account and confirm no former user's data appears.
3. Sign in with Google, cancel its picker, try a wrong password, request/reset a password using the received email, then sign in with the new password.
4. Create/delete a listing, select a real published buyer, request delivery, request/cancel equipment, bookmark a scheme and change language/location.
5. Disconnect/reconnect the network and confirm loading/error handling; writes wait for Firestore acknowledgement. Never treat a pending offline write as a completed official submission.
6. Confirm unauthorized notice publication/tracking changes fail. Test the operator transition with an admin claim and App Check token.
7. Compare every original screen and navigation destination against the existing build, including English/Hindi/Marathi, dark mode, voice, location, quiz and UPI practice.

The final chat response reports which of these tests were actually run. Emulator tests do not certify production Firebase setup or external government/provider integrations.
