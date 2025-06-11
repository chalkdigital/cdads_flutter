import Flutter
import Foundation
import UIKit

class CDAdPlatformViewFactory: NSObject, FlutterPlatformViewFactory {
    private var messenger: FlutterBinaryMessenger

    init(messenger: FlutterBinaryMessenger) {
        self.messenger = messenger
        super.init()
    }

    func create(
        withFrame frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?
    ) -> FlutterPlatformView {
        var customFrame = frame

        if let dict = args as? [String: Any] {
            if let adSizeMap = args["adSize"] as? [String: Any],
                let width = adSizeMap["width"] as? Double,
                let height = adSizeMap["height"] as? Double
            {
                customFrame = CGRect(x: 0, y: 0, width: width, height: height)
            }
        }
        return CDAdPlatformView(
            frame: customFrame, viewIdentifier: viewId, arguments: args, messenger: messenger)
    }

    func createArgsCodec() -> FlutterMessageCodec & NSObjectProtocol {
        return FlutterStandardMessageCodec.sharedInstance()
    }
}
