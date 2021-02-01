import Flutter
import UIKit

public class SwiftFlutterHardwareInteractionPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "flutter_hardware_interaction", binaryMessenger: registrar.messenger())
    let instance = SwiftFlutterHardwareInteractionPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
