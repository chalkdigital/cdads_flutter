#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint cdads.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'cdads_flutter'
  s.version          = '0.0.1'
  s.summary          = 'ChalkDigital Library for device tracking and display mobile adverisement'
  s.description      = <<-DESC
A new Flutter plugin project.
                       DESC
  s.homepage         = 'https://www.chalkdigital.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Chalkdigital Inc' => 'chandra@chalkdigital.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  # s.module_map = 'Classes/**/module.modulemap'
  s.dependency 'Flutter'
  s.platform = :ios, '13.0'

  # Flutter.framework does not contain a i386 slice.
  # s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386' }
  s.swift_version = '5.9'
  s.dependency 'Chalkdigital-Mobile-Ads-SDK', '~> 3.0.9'

  # 4. Ensure CocoaPods treats it as dynamic
  #    (default is dynamic if s.static_framework is not true)
  s.static_framework = true

  s.xcconfig = {
    'LIBRARY_SEARCH_PATHS' => '$(inherited) $(TOOLCHAIN_DIR)/usr/lib/swift/$(PLATFORM_NAME)/ $(SDKROOT)/usr/lib/swift',
    'LD_RUNPATH_SEARCH_PATHS' => '$(inherited) /usr/lib/swift',
  }


  # If your plugin requires a privacy manifest, for example if it uses any
  # required reason APIs, update the PrivacyInfo.xcprivacy file to describe your
  # plugin's privacy impact, and then uncomment this line. For more information,
  # see https://developer.apple.com/documentation/bundleresources/privacy_manifest_files
  # s.resource_bundles = {'cdads_privacy' => ['Resources/PrivacyInfo.xcprivacy']}
end
