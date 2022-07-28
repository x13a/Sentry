# Sentry

Enforce security policies.

[<img
     src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/me.lucky.sentry/)
[<img
      src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
      alt="Get it on Google Play"
      height="80">](https://play.google.com/store/apps/details?id=me.lucky.sentry)

<img 
     src="https://raw.githubusercontent.com/x13a/Sentry/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png" 
     width="30%" 
     height="30%">

Tiny app to enforce security policies of your device.

It can:
* limit the maximum number of failed password attempts
* disable USB data connections (Android 12, USB HAL 1.3, Device Owner)
* notify on failed password attempt
* notify when an app without Internet permission got it after an update

Also you can grant it device & app notifications permission to turn off USB data connections 
automatically on screen off.

## Permissions

* DEVICE_ADMIN - limit the maximum number of failed password attempts
* DEVICE_OWNER - disable USB data connections
* NOTIFICATION_LISTENER - receive lock/package events

## Example

To set as device owner:
```sh
$ adb shell dpm set-device-owner me.lucky.sentry/.DeviceAdminReceiver
```

## Localization

[<img 
      src="https://user-images.githubusercontent.com/53379023/153461055-50169c86-b187-40c7-8ec8-97d5e93660b8.png" 
      alt="Crowdin" 
      height="51">](https://crwd.in/me-lucky-sentry)

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)
