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

## License
[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

This application is Free Software: You can use, study share and improve it at your will.
Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License v3](https://www.gnu.org/licenses/gpl.html) as published by the Free
Software Foundation.
