# RILDefender-App

## Introduction

This folder hosts the source code of the RILDefender app. This app has been tested from [Android API level](https://developer.android.com/studio/releases/platforms) 25 (Android 7.1) to API level 33 (Android 13). The major functions of this app are: 

- Configure security level for each attack, including:
	- Block and Notify
	- Block without Notify 
	- Notify only
	- Allow
- Receive real-time alerts for attack events 
- Configure user-defined attack signatures

## Compilation and Installation

The RILDefender app source code is built using the [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html). You can either directly import this folder with [Android Studio](https://developer.android.com/studio) or [build from the command line](https://developer.android.com/studio/build/building-cmdline).

After compilation, you can easily use the Android Studio UI to install it to your Android device or use tools such as [adb](https://developer.android.com/studio/command-line/adb).


## Required Permissions

The RILDefender app is root-free, and requires the following permissions (declared in [AndroidManifest.xml](./app/src/main/AndroidManifest.xml)) with justifications:

- `android.permission.FOREGROUND_SERVICE` to run the RILDefender notification service
- `android.permission.POST_NOTIFICATIONS` to post notifications
- `android.permission.WRITE_EXTERNAL_STORAGE` to store reported SMS events in local storage
- `android.permission.READ_EXTERNAL_STORAGE` to read stored SMS events and user-defined signatures
