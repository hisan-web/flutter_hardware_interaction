#import "FlutterHardwareInteractionPlugin.h"
#if __has_include(<flutter_hardware_interaction/flutter_hardware_interaction-Swift.h>)
#import <flutter_hardware_interaction/flutter_hardware_interaction-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_hardware_interaction-Swift.h"
#endif

@implementation FlutterHardwareInteractionPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterHardwareInteractionPlugin registerWithRegistrar:registrar];
}
@end
