# Sentry

> [!WARNING]
> Because Google has already shifted the DevicePolicyManager API, the app architecture will be 
redesigned. The Device Policy features will become a standalone app acting as the Device Owner 
core, and the other apps will request the required functionality from it.

Enforce security policies.

[<img
     src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/me.lucky.sentry/)

<img 
     src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" 
     width="30%" 
     height="30%">

Tiny app to enforce security policies of your device.

It can:
* limit the maximum number of failed password attempts
* disable USB data connections (Android 12, USB HAL 1.3, Device Owner)
* disable safe boot mode (Android 7, Device Owner)
* notify on failed password attempt
* notify when an app without Internet permission got it after an update

Be aware that the app may not work in _safe_ mode.

## Permissions

* DEVICE_ADMIN - limit the maximum number of failed password attempts
* DEVICE_OWNER - disable USB data connections
* NOTIFICATION_LISTENER - receive lock/package events
* QUERY_ALL_PACKAGES - receive all package events

## Example

To set as Device Owner:

```sh
adb shell dpm set-device-owner me.lucky.sentry/.admin.DeviceAdminReceiver
```

## Localization

[<img 
     height="51" 
     src="https://badges.crowdin.net/badge/dark/crowdin-on-light@2x.png" 
     alt="Crowdin">](https://crwd.in/me-lucky-sentry)

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)
