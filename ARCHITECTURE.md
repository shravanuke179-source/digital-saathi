# Digital Saathi architecture and integration audit

## Existing application

This AI Studio export is a **native Android application**, not a web frontend. The original README's AI Studio banner does not imply React or an existing HTTP server.

| Area | Existing implementation |
| --- | --- |
| Build | Gradle Kotlin DSL, Android Gradle Plugin 9.1.1, compile SDK 36.1, min SDK 24 |
| UI | Kotlin, Jetpack Compose, Material 3; shared colors, typography and light/dark themes |
| Entry point | `app/src/main/java/com/example/MainActivity.kt` |
| Navigation | `NavDestination` enum and a Compose `when` switch, with top and bottom navigation |
| State | `DigitalSaathiRepository`, Kotlin `StateFlow`, repository coroutine scope |
| Models | `data/model/Models.kt` |
| Network | Retrofit, Moshi, OkHttp; Open-Meteo weather; unused Supabase DTO/service scaffolding |
| Existing server | None |

Routes: Home, Services, Schemes, Kheti, Digital Shiksha, Village SOS, Profile, Sell Produce, Farm Equipment, My Farm Sales, Delivery Tracking, Equipment Bookings. Services combines schemes, agriculture, education and village information into tabs. These destinations and screen components remain in place.

Shared components include search, location selection, scheme details/application forms, village notices, grievances, voice/TTS assistance, a UPI practice simulator and a safety quiz. English, Hindi and Marathi content is retained.

## Original mock behavior

| Feature | Original behavior | Integration |
| --- | --- | --- |
| Login/register | Delays followed by local authentication boolean; no password verification | Firebase Authentication email/password |
| Google sign-in | Hardcoded account/custom email dialog | Credential Manager Google ID token exchanged with Firebase |
| Guest/demo | Immediate dashboard bypass | Firebase anonymous user with an isolated UID |
| Profile | Fictional citizen, bank, ration card and verified identity | UID-owned Firestore profile; identity information remains empty/unverified unless set by a trusted service |
| Cloud sync | Simulated Supabase success | Actual Firestore listeners and write acknowledgement |
| Applications/grievances | Random local IDs and false official-submission success | UUID records saved privately; official submission is explicitly separate |
| Bookmarks/settings | In-memory | Owner-scoped documents |
| Produce | Sample listings, in-memory creation/deletion/buyer selection | Firestore creation, deletion and validated buyer selection |
| Delivery | Sample requests and a button fabricating transport progress | Atomic listing/request writes; callable server operation for authorized status updates |
| Equipment | Sample catalog/bookings and local cancellation | Admin-published catalog, private requests and cancellation |
| Village notices | Sample data; anyone could impersonate an authority locally | Authenticated reads; publication restricted to trusted admin claims |
| Mandi prices | Random sample generator, despite a “live” name | Published Firestore quotes; no fabricated production rates |
| Weather | Real API with falsely live sample fallback | Existing API retained; unavailable/stale readings labelled |
| Schemes, citizen links, lessons, quiz, advisory text, SOS directory | Bundled reference content | Retained reference content; not converted into invented live integrations |
| UPI simulator | Educational simulation | Preserved simulation; never charges money |

Sample fixtures remain in the repository for reference, but private records, providers, market rates and notices no longer start from these fixtures. A new database has empty catalogs until real content is published. Bundled scheme eligibility, emergency contacts and advisories require editorial verification before launch.

## Added services

`FirebaseSession.kt` implements the Firebase task bridge and native Google sign-in. Firebase manages password storage, tokens and persistent sessions; passwords are never written to Firestore. The existing app-level auth gate hides all dashboards until a Firebase user exists. The Guest button creates a Firebase anonymous identity, not an unauthenticated bypass.

`CloudStore.kt` serializes existing UI models to Firestore maps using Moshi, including existing string-pair fields. Snapshot listeners update screens. Forms await server acknowledgement before their success callbacks run. Session changes detach listeners and clear private data; the repository is disposed with its composition.

Database paths:

```text
users/{firebaseUid}                         editable profile
users/{firebaseUid}/preferences/settings   language and location
users/{firebaseUid}/bookmarks/{schemeId}
users/{firebaseUid}/applications/{uuid}
users/{firebaseUid}/grievances/{uuid}
users/{firebaseUid}/produce/{uuid}
users/{firebaseUid}/deliveries/{uuid}
users/{firebaseUid}/bookings/{uuid}
notices/{id}                               admin published
buyers/{id}                                admin published
deliveryPartners/{id}                      admin published
equipment/{id}                             admin published
marketPrices/{id}                          admin published
auditEvents/{id}                           server only
```

The export had no server runtime to reuse. The added `functions/` directory is a small Firebase Cloud Functions backend using managed Node.js 22. Ordinary owner CRUD goes directly to Firestore under security rules. The callable `updateDeliveryStatus` verifies authentication, an admin claim and App Check, validates inputs, enforces sequential transitions, atomically updates both records and writes an audit event. It does not simulate a transport company API.

The app has no new HTTP server, web framework, replacement navigation or redesigned screens. Minimal text changes remove false verification/submission claims; password reset, loading/error feedback and Google's real account picker are necessary authentication/integration changes.

## Boundaries still requiring real configuration/services

Firebase must be created and configured. Government applications, Aadhaar verification, bank linking, actual dispatch, equipment availability and payment processing are not supplied by Firebase. The app saves requests and displays authorized published data; integration with those providers requires their APIs and credentials. An operator interface is not part of the original project; admin publishing is through trusted tooling/Firebase Console. The existing tracking action works only for an authorized operator after backend deployment and App Check setup.

The existing `DigitalSaathi.apk` is an old artifact and has not been rebuilt.
