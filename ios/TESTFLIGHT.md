# Synd: signed IPA and TestFlight from Codemagic

## Status and workflows

Branch: `feature/native-ble-foundation`.
`synd-ios-compile` remains the unsigned Debug compile check; its .app cannot be uploaded to TestFlight.
`synd-ios-testflight` is a manually started Release archive/export and App Store Connect upload.
No push or pull-request release trigger is enabled.

Repository configuration alone is not evidence of signing, an IPA, successful upload, or a processed TestFlight build.
The Apple listing, signing identities and integration must be configured outside GitHub.

## One-time Apple setup

1. Confirm active Apple Developer Program membership and accepted Apple agreements.
2. In App Store Connect, open Synd > General > App Information. Record the numeric **Apple ID** and exact **Bundle ID**. The repository currently uses `org.synd.ios`; the listing, explicit Developer Portal App ID, provisioning profile and built app must all use the same identifier.
   If the listing uses another ID, update `PRODUCT_BUNDLE_IDENTIFIER` in `ios/project.yml` and both `BUNDLE_ID` and `ios_signing.bundle_identifier` in `codemagic.yaml` before running.
3. In Users and Access > Integrations > App Store Connect API, create a dedicated **team API key** with **App Manager** access. The account holder may need to request API access first. Download its .p8 once; record its Key ID and Issuer ID.
4. In Codemagic Team settings > Team integrations > Developer Portal > Manage keys, add the key as **synd-app-store-connect** using the Issuer ID, Key ID and .p8. This exact integration name is referenced by YAML. Use the Apple team that owns the Synd listing.
5. In Team settings > codemagic.yaml settings > Code signing identities, generate or securely upload an **Apple Distribution** certificate with its private key (.p12 and password). If generating through Codemagic, follow its download/upload steps and retain a secure backup. An API key alone is not a distribution signing identity.
6. Create/fetch an **App Store** provisioning profile for the exact Synd App ID, same team and distribution certificate. Add/fetch it in Codemagic Code signing identities; verify its matching certificate indicator and expiration. Development and Ad Hoc profiles are unsuitable for this workflow.
   The YAML `ios_signing` selection installs matching identities stored in Codemagic; it does not create missing identities.
7. In Codemagic, create environment group **synd_ios_release**, available to this app, containing `APP_STORE_APPLE_ID` with the listing's numeric Apple ID. This is not the bundle ID, Team ID or your Apple login. API credentials belong in the integration, not this YAML.
8. Confirm the release marketing version in `ios/project.yml` (initially `1.0.0`) matches the intended release. Team signing settings are applied to the generated Xcode project by `xcode-project use-profiles`; keep credentials out of the XcodeGen spec.

Never commit .p8/.p12/private keys, passwords, provisioning profiles, .env files or Codemagic tokens. Do not paste them into chat or logs. Use Codemagic's protected storage, and restrict who can edit the release branch or start credential-bearing builds.

## App metadata to finish before upload

The inspected iOS project contains Swift sources and a generated Info.plist, but no asset catalog.
Add approved app artwork in an AppIcon asset set, including the required 1024px App Store icon, include the catalog in the XcodeGen sources and set `ASSETCATALOG_COMPILER_APPICON_NAME: AppIcon`.
Apple may reject a binary without the required icons. A successful unsigned compile does not check this.
Review the final app's privacy disclosures, export compliance and TestFlight test information in App Store Connect. Answer encryption questions based on actual app behavior; this workflow makes no encryption declaration for you.

## Run in the cloud

1. In Codemagic, open Synd, select `feature/native-ble-foundation`, and rescan/check `codemagic.yaml`.
2. Select **Synd iOS - signed IPA and TestFlight upload** (`synd-ios-testflight`) and start the build.
3. Run only one release build at a time; wait for Apple's processing before retrying. The workflow reads the highest version's App Store/TestFlight build number and uses the larger of that number plus one and Codemagic's app-wide counter. API errors stop the build. Empty successful results start from zero; existing non-integer build numbers stop with a clear error and require adapting the numbering policy.
4. Check provisioning, Release archive, IPA export and signature-verification steps. Download `build/ios/ipa/*.ipa` from artifacts. The verification step checks codesign validity, embedded provisioning-profile presence and bundle ID; Apple's upload validation remains authoritative.
5. Check the **publishing** log for Apple's accepted upload. Then open App Store Connect > Synd > TestFlight and wait for processing. A green compile/archive alone is not upload evidence.
6. Resolve any Missing Compliance prompt. Create an internal testing group, add eligible App Store Connect users (including yourself), assign the processed build or enable automatic distribution, and accept the TestFlight invitation on your iPhone.

`submit_to_testflight: false` still uploads the IPA; it disables automated **external beta review** submission.
The default leaves tester selection in App Store Connect. It does not submit an App Store release.
For external testers, finish Test Information/review contact details, create the external group, then set `submit_to_testflight: true` and `beta_groups` to real group names when ready. Check Codemagic's asynchronous post-processing and Apple's beta review outcome.

## Diagnose the failing stage

- Missing integration/group: verify exact names and that the app belongs to the configured Codemagic team.
- No signing identity/profile: install a valid matching App Store profile and its Apple Distribution certificate/private key. A public certificate without its private key is insufficient.
- Authentication/403: verify the team API key role, membership, agreements and access; do not hide lookup failures by forcing build 1.
- Bundle mismatch: compare the listing, Developer Portal App ID and all three repository bundle ID settings.
- Duplicate build number: wait for processing, stop overlapping builds and rerun after the previous upload appears.
- Icon/metadata rejection: finish the asset catalog and address Apple's exact validation message.
- IPA exists but TestFlight is empty: inspect publishing, confirm the correct app/team and build processing or rejection; capture the build URL and non-secret error message.

## References

- [Codemagic native iOS signing and builds](https://docs.codemagic.io/yaml-quick-start/building-a-native-ios-app/)
- [Codemagic iOS signing identities](https://docs.codemagic.io/yaml-code-signing/signing-ios/)
- [App Store Connect upload and beta review](https://docs.codemagic.io/yaml-publishing/app-store-connect/)
- [Codemagic build numbering](https://docs.codemagic.io/knowledge-codemagic/build-versioning/)
