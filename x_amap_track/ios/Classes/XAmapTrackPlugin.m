#import "XAmapTrackPlugin.h"
#if __has_include(<x_amap_track/x_amap_track-Swift.h>)
#import <x_amap_track/x_amap_track-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "x_amap_track-Swift.h"
#endif

@implementation XAmapTrackPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftXAmapTrackPlugin registerWithRegistrar:registrar];
}
@end
