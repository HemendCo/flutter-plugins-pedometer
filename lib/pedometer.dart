
import 'dart:async';
import 'dart:collection';
import 'dart:convert';

import 'package:flutter/services.dart';

class Pedometer {
  static const MethodChannel _channel = const MethodChannel('pedometer');
  static OnSensorChanged onSensorChangedCallback;

  Pedometer() {
    _channel.setMethodCallHandler((MethodCall call) async {
      Map result = HashMap();

      if(call.arguments != null) {
        result.addAll(json.decode(call.arguments));
      }

      switch(call.method) {
        case 'onSensorChanged':
          if (onSensorChangedCallback != null) {
            String date = result.containsKey('date') ? result['date'] : "";
            int step = result.containsKey('step') ? result['step'] : 0;
            onSensorChangedCallback(StepData(date, step));
          }
          break;
      }
    });
  }

  Future<bool> start() async {
    final bool isStarted = await _channel.invokeMethod('start');
    return isStarted;
  }

  Future<bool> stop() async {
    final bool isStoped = await _channel.invokeMethod('stop');
    return isStoped;
  }

  Future<bool> isStarted() async {
    final bool isStoped = await _channel.invokeMethod('isStarted');
    return isStoped;
  }

  Future<StepData> getToday() async {
    final String data = await _channel.invokeMethod('getToday');
    Map map = json.decode(data);
    return StepData(map['date'], map['step']);
  }

  Future<List<StepData>> getAll() async {
    final String data = await _channel.invokeMethod('getAll');
    List<dynamic> map = json.decode(data);
    List<StepData> list = List<StepData>();
    if(map.isNotEmpty) {
      for (var v in map) {
        list.add(StepData(v['date'], v['step']));
      }
    }

    return list;
  }

  Future<bool> reset() async {
    final bool isReseted = await _channel.invokeMethod('clear');
    return isReseted;
  }

  events({OnSensorChanged onSensorChanged}) async {
    if (onSensorChanged != null) {
      onSensorChangedCallback = onSensorChanged;
    }
  }
}

typedef void OnSensorChanged(StepData sd);

class StepData {
  final String date;
  final int step;

  StepData(this.date, this.step);
}