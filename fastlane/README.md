fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android build_and_screengrab

```sh
[bundle exec] fastlane android build_and_screengrab
```

Build debug and test APK for screenshots

### android do_screengrab

```sh
[bundle exec] fastlane android do_screengrab
```

Take screenshots

### android process_screenshots

```sh
[bundle exec] fastlane android process_screenshots
```

Add a device frame to the screenshots and copy them to the phoneScreenshots and media folders

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
