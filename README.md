# PackageInstallerSample
Package installer sample.

---

## Device owner

### Set device owner

```
$ adb shell dpm set-device-owner ${applicationId}/${DeviceAdminReceiver::class.java.name}
```

### List owners

```
$ adb shell dpm list-owners
```

### Unset device owner

```
$ adb shell dpm remove-active-admin ${applicationId}/${DeviceAdminReceiver::class.java.name}
```

---
