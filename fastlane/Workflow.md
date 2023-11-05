# Fastlane Workflow

> This assumes that you have installed [Fastlane](https://docs.fastlane.tools/) using Bundler.

If you're getting 'Permission denied' errors from adb, restart the adb daemon as root: `adb root`

## Capture Screenshots
1. Make sure the emulator is running (preferably Pixel 5 with API 32+)
2. Make sure System UI demo mode is enabled in developer settings
3. Run `bundle exec fastlane android build_and_screengrab` to build the debug/test apks and run the instrumentation test on the device

## Process Screenshots
1. This actions assumes that the screenshots have been created in the `fastlane/screenshots` directory
2. Run `bundle exec fastlane android process_screenshots` to add a device frame to each screenshot and copy the framed screenshots to the `media` folder
