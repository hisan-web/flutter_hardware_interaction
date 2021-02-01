
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterHardwareInteraction {
  static const MethodChannel _channel =
      const MethodChannel('flutter/plugin/hardware_interaction');

  static Future<String> get platformVersion async {
    return "";
  }


  Future<bool> msPrinterWrite() async {
    try {
      return await _channel.invokeMethod('msPrinterWrite');
    } catch(e) {
      return false;
    }
  }

  Future<bool> msPrinterOpen() async {
    try {
      return await _channel.invokeMethod('msPrinterOpen');
    } catch(e) {
      return false;
    }
  }

  Future<bool> msPrinterGetStatus() async {
    try {
      return await _channel.invokeMethod('msPrinterGetStatus');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemShutdown() async {
    try {
      return await _channel.invokeMethod('systemShutdown');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemReboot() async {
    try {
      return await _channel.invokeMethod('systemReboot');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemHideUI() async {
    try {
      return await _channel.invokeMethod('systemHideUI');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemShowUI() async {
    try {
      return await _channel.invokeMethod('systemShowUI');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemOpenGestureUI() async {
    try {
      return await _channel.invokeMethod('systemOpenGestureUI');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemCloseGestureUI() async {
    try {
      return await _channel.invokeMethod('systemCloseGestureUI');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemReadModel() async {
    try {
      return await _channel.invokeMethod('systemReadModel');
    } catch(e) {
      return false;
    }
  }

  Future<bool> systemReadSerial() async {
    try {
      return await _channel.invokeMethod('systemReadSerial');
    } catch(e) {
      return false;
    }
  }

}
