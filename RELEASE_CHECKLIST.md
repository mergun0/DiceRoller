# Release Checklist

- Build debug APK
- Run unit tests
- Run lint and review warnings
- Verify no internet permission exists in `AndroidManifest.xml`
- Verify app name and localized strings
- Test single and double dice on a physical device
- Test sound, vibration, and shake-to-roll toggles
- Test light, dark, and system theme modes
- Clear history and verify empty state
- Generate signed release bundle in Android Studio
- Complete Play Console privacy and data safety forms using `PRIVACY_POLICY.md`
