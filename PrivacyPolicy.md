<!--
SPDX-FileCopyrightText: Saalim Quadri <danascape@gmail.com>
SPDX-License-Identifier: GPL-3.0-or-later
-->

# Privacy Policy — Compass

_Effective 30 October 2026_

Compass (package `org.prauga.compass`) is a compass app published by Saalim Quadri for PVOT-OSS.
This policy explains what the app does with your data.

**In short: the app doesn't collect, store on a server, share or sell any personal data. It has no
accounts, no ads, no analytics and no tracking, and it has no permission to use the internet.**

## What the app uses

- **The motion sensors** (rotation vector), to show your heading. Readings are used live and never
  stored.
- **Your location**, only if you allow it, to show your coordinates, the name of the place you are
  in and your elevation. The compass works without it. The version distributed outside Google Play
  without Google services (the `fdroid` build) does not use location at all.

## What the app keeps on your device

- **Your last known location** (coordinates, elevation and place name), so the home screen widget
  can show it. It is replaced each time the app gets a new location, is excluded from Android
  backups and device transfers, and is deleted when you clear the app's storage or uninstall it.
- **Widget state**: a counter the widget uses to know when to refresh. It contains no personal data.

## What other services see

The app itself sends nothing anywhere. Two Android system services it relies on may contact a
server, under their own terms:

| Service | What for | What it may send |
|---|---|---|
| Google Play services location (Google Play build only) | Getting your location | Handled by Google Play services under the [Google Privacy Policy](https://policies.google.com/privacy), including Google Location Accuracy if you have it turned on |
| Android's geocoder | Turning your coordinates into a place name | Your coordinates, to the geocoding service your device uses (on most devices, Google's) |

The home screen widget shows your last known location on your home screen, where anyone who can
see your screen can read it. Remove the widget if you don't want that.

## Permissions

- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`: to show where you are, as described above. The
  app asks for them when it starts; you can refuse.
- `WAKE_LOCK`, `ACCESS_NETWORK_STATE`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`: added by
  Android's WorkManager library, which the home screen widget uses to refresh. They don't give the
  app access to any of your data.

The app does not have the `INTERNET` permission.

## Children

The app is not directed at children and collects no data from anyone, children included.

## Changes

If the app starts handling data differently, this policy will be updated before that version is
released, with a new effective date at the top.

## Contact

Questions about this policy: danascape@gmail.com, or open an issue at
https://github.com/PVOT-OSS/PvotCompass/issues.
