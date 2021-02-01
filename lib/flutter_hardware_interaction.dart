
import 'dart:async';

import 'package:flutter/services.dart';

class FlutterHardwareInteraction {
  static const MethodChannel _channel =
      const MethodChannel('flutter/plugin/hardware_interaction');

  static Future<String> get platformVersion async {
    return "";
  }


  /**
   * [{"type":"String", "data":"店号：8888","lineFeed":0}, {"type":"QrCode","data":""},{"type":"ImageBase64","data":""},"type":"FeedLine","data":5,"type":"FeedDot","data":5]
   */

  /// 打印
  static Future<bool> msPrinterWrite(String data) async {
    try {
      return await _channel.invokeMethod('msPrinterWrite',{'data': data});
    } catch(e) {
      return false;
    }
  }

  /// 打开打印机纸仓
  static Future<bool> msPrinterOpen() async {
    try {
      return await _channel.invokeMethod('msPrinterOpen');
    } catch(e) {
      return false;
    }
  }

  /// 获取打印机状态
  static Future<bool> msPrinterGetStatus() async {
    try {
      return await _channel.invokeMethod('msPrinterGetStatus');
    } catch(e) {
      return false;
    }
  }

  /// 关机
  static Future<bool> systemShutdown() async {
    try {
      return await _channel.invokeMethod('systemShutdown');
    } catch(e) {
      return false;
    }
  }

  /// 重启
  static Future<bool> systemReboot() async {
    try {
      return await _channel.invokeMethod('systemReboot');
    } catch(e) {
      return false;
    }
  }

  /// 隐藏导航栏/任务栏
  static Future<bool> systemHideUI() async {
    try {
      return await _channel.invokeMethod('systemHideUI');
    } catch(e) {
      return false;
    }
  }

  /// 显示导航栏/任务栏
  static Future<bool> systemShowUI() async {
    try {
      return await _channel.invokeMethod('systemShowUI');
    } catch(e) {
      return false;
    }
  }

  /// 开启划出状态栏
  static Future<bool> systemOpenGestureUI() async {
    try {
      return await _channel.invokeMethod('systemOpenGestureUI');
    } catch(e) {
      return false;
    }
  }

  /// 关闭划出导航栏
  static Future<bool> systemCloseGestureUI() async {
    try {
      return await _channel.invokeMethod('systemCloseGestureUI');
    } catch(e) {
      return false;
    }
  }

  /// 读取主板型号
  static Future<bool> systemReadModel() async {
    try {
      return await _channel.invokeMethod('systemReadModel');
    } catch(e) {
      return false;
    }
  }

  /// 读取主板序列号
  static Future<bool> systemReadSerial() async {
    try {
      return await _channel.invokeMethod('systemReadSerial');
    } catch(e) {
      return false;
    }
  }

}
