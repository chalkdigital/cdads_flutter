Pod::Spec.new do |s|
  s.name             = 'cdads_flutter'
  s.version          = '4.0.0'
  s.summary          = 'ChalkDigital Flutter plugin for device tracking and mobile advertising'
  s.homepage         = 'https://www.chalkdigital.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Chalkdigital Inc' => 'chandra@chalkdigital.com' }
  s.source           = { :path => '.' }

  s.platform         = :ios, '14.0'
  s.swift_version    = '5.9'

  s.source_files     = 'Classes/**/*.swift'

  s.dependency 'Flutter'

  # CDAds Swift SDK. CocoaPods doesn't allow `:path` inside a podspec (only in a
  # Podfile), so the consuming app's Podfile must supply it during development:
  #   pod 'CDAds', :path => '../../../CDAds-iOS-SDK'
  # Change to a versioned constraint once the SDK is published, e.g. '~> 4.0'.
  s.dependency 'CDAds'

  # Required: Privacy Manifest (Apple required since May 2024 for third-party SDKs)
  s.resource_bundles = { 'cdads_flutter_privacy' => ['Resources/PrivacyInfo.xcprivacy'] }

  s.pod_target_xcconfig = {
    'DEFINES_MODULE' => 'YES',
    'SWIFT_VERSION'  => '5.9',
  }
end
