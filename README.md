# pedometer

This plugin gives pedometer and step detection for **Android** and saves daily.

# Install the plugin into project
## iOS
> *Currently No Support*

## Android
1. Edit `pubspec.yaml`
```csharp
dependencies:
  pedometer:
    git:
      url: https://github.com/HemendCo/flutter-plugins-pedometer.git
      ref: main
```
2. Import the package on your source code
```csharp
import 'package:pedometer/pedometer.dart';
```

# Public functions
Pedometer Sensor
* start()
* stop()
* isStarted()
* getToday()
* getAll()
* reset()
* events()

# Usage

## Init

```dart
Pedometer ped = Pedometer();
```

## Start

```dart
bool isStarted = await ped.isStarted();

if(!isStarted) {
    await ped.start();
}
```

## Stop

```dart
bool isStarted = await ped.isStarted();

if(isStarted) {
    await ped.stop();
}
```


## Get today steps

```dart
StepData today = await ped.getToday();

print(today.date);
print(today.step);
```


## Get all steps
```dart
List<StepData> all = await ped.getAll();

for(var i = 0; all != null && i < all.length; i++) {
    StepData sd = all[i];
    
    print(sd.date);
    print(sd.step);
}
```

## Reset - delete all data

```dart
bool isReseted = await ped.reset();

print("Is reseted? " + (isReseted ? "Yes" : "No"));
```

## Event - when sensor changed

```dart
ped.events(onSensorChanged: (StepData sd) async {
    print(sd.date);
    print(sd.step);
});
```