package com.chalkdigital.ads.resource;

public class ImaHtml {

    public static String IMA_HTML = "\n" +
            "    <html>\n" +
            "    <head>\n" +
            "      <style type=\"text/css\">\n" +
            "      #mainContainer {\n" +
            "        position: relative;\n" +
            "        padding: 0px;\n" +
            "        margin: 0px;\n" +
            "        width: ad_width;\n" +
            "        height: ad_height;\n" +
            "        background-color: #000000;\n" +
            "      }\n" +
            "\n" +
            "      #contentElement {\n" +
            "        width: 1px;\n" +
            "        height: 1px;\n" +
            "        overflow: hidden;\n" +
            "      }\n" +
            "\n" +
            "      #content, #adContainer {\n" +
            "        position: absolute;\n" +
            "        padding: 0px;\n" +
            "        width: ad_width;\n" +
            "        height: ad_height;\n" +
            "      }\n" +
            "    </style>\n" +
            "  </head>\n" +
            "\n" +
            "  <body>\n" +
            "    <script type=\"text/javascript\" src=\"http://imasdk.googleapis.com/js/sdkloader/ima3.js\"></script>\n" +
            "    <script type=\"text/javascript\">\n" +
            "\n" +
            "\n" +
            "      var adsRequest;\n" +
            "      var adsLoader;\n" +
            "      var volume = 0;\n" +
            "      var adsManager;\n" +
            "      var clickThroughUrl;"  +
            "      function initilizePlayer() {\n" +
            "\n" +
            "    var videoContent = document.getElementById('contentElement');\n" +
            "    var adDisplayContainer =\n" +
            "    new google.ima.AdDisplayContainer(\n" +
            "      document.getElementById('adContainer'),\n" +
            "      videoContent);\n" +
            "    // Must be done as the result of a user action on mobile\n" +
            "    adDisplayContainer.initialize();\n" +
            "\n" +
            "    // Re-use this AdsLoader instance for the entire lifecycle of your page.\n" +
            "    adsLoader = new google.ima.AdsLoader(adDisplayContainer);\n" +
            "\n" +
            "    // Add event listeners\n" +
            "    adsLoader.addEventListener(\n" +
            "      google.ima.AdsManagerLoadedEvent.Type.ADS_MANAGER_LOADED,\n" +
            "      onAdsManagerLoaded,\n" +
            "      false);\n" +
            "    adsLoader.addEventListener(\n" +
            "      google.ima.AdErrorEvent.Type.AD_ERROR,\n" +
            "      onAdError,\n" +
            "      false);\n" +
            "\n" +
            "    function onAdsManagerLoaded(adsManagerLoadedEvent) {\n" +
            "      console.log(\"adManagerLoaded\");\n" +
            "      // Get the ads manager.\n" +
            "      adsManager = adsManagerLoadedEvent.getAdsManager(\n" +
            "          videoContent);  // See API reference for contentPlayback\n" +
            "      adsManager.setVolume(volume);\n" +
            "      console.log(volume);\n" +
            "      // Add listeners to the required events.\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdErrorEvent.Type.AD_ERROR,\n" +
            "        onAdError);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.CONTENT_PAUSE_REQUESTED,\n" +
            "        onContentPauseRequested);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.CONTENT_RESUME_REQUESTED,\n" +
            "        onContentResumeRequested);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.ALL_ADS_COMPLETED,\n" +
            "        onAllAdsCompleted);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.CLICK,\n" +
            "        onClick);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.COMPLETE,\n" +
            "        onComplete);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.FIRST_QUARTILE,\n" +
            "        onFirstQuartile);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.IMPRESSION,\n" +
            "        onImpression);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.LOADED,\n" +
            "        onLoaded);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.LOG,\n" +
            "        onLog);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.MIDPOINT,\n" +
            "        onMidpoint);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.STARTED,\n" +
            "        onStarted);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.THIRD_QUARTILE,\n" +
            "        onThirdQuartile);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.USER_CLOSE,\n" +
            "        onClose);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.VOLUME_CHANGED,\n" +
            "        onVolumeChanged);\n" +
            "      adsManager.addEventListener(\n" +
            "        google.ima.AdEvent.Type.VOLUME_MUTED,\n" +
            "        onMute);\n" +
            "\n" +
            "\n" +
            "      try {\n" +
            "        // Initialize the ads manager. Ad rules playlist will start at this time.\n" +
            "        adsManager.init(ad_width, ad_height, google.ima.ViewMode.NORMAL);\n" +
            "        // Call start to show ads. Single video and overlay ads will\n" +
            "        // start at this time; this call will be ignored for ad rules, as ad rules\n" +
            "        // ads start when the adsManager is initialized.\n" +
            "        adsManager.start();\n" +
            "      } catch (adError) {\n" +
            "        // An error may be thrown if there was a problem with the VAST response.\n" +
            "        // Play content here, because we won't be getting an ad.\n" +
            "        // videoContent.play();\n" +
            "        fireEvent(\"error?\"+adError);\n" +
            "      }\n" +
            "    }\n" +
            "\n" +
            "    function onAllAdsCompleted() {\n" +
            "      fireEvent(\"allAdsCompleted\");\n" +
            "    }\n" +
            "    function onClick() {\n" +
            "      fireEvent(\"click\");\n" +
            "    }\n" +
            "    function onComplete() {\n" +
            "      fireEvent(\"completed\");\n" +
            "    }\n" +
            "    function onFirstQuartile() {\n" +
            "      fireEvent(\"firstQuartile\");\n" +
            "    }\n" +
            "    function onImpression() {\n" +
            "      fireEvent(\"impression\");\n" +
            "    }\n" +
            "    function onLoaded() {\n" +
            "      fireEvent(\"loaded\");\n" +
            "    }\n" +
            "    function onLog(log) {\n" +
            "      console.log(log);\n" +
            "    }\n" +
            "    function onMidpoint() {\n" +
            "      fireEvent(\"midPoint\");\n" +
            "    }\n" +
            "    function onStarted(event) {\n" +
            "var ad = event.getAd(),\n" +
            "             key = Object.keys(ad)[0] || null;\n" +
            "\n" +
            "    if (key && ad[key]) {\n" +
            "        clickThroughUrl = ad[key].clickThroughUrl;\n" +
            "        console.log('ClickThroughUrl: ' + clickThroughUrl);\n" +
            "    }\n" +
            "      fireEvent(\"started?clickthroughurl=\"+clickThroughUrl);"+
            "    }\n" +
            "    function onThirdQuartile() {\n" +
            "      fireEvent(\"thirdQuartile\");\n" +
            "    }\n" +
            "    function onClose() {\n" +
            "      fireEvent(\"close\");\n" +
            "    }\n" +
            "    function onVolumeChanged() {\n" +
            "      fireEvent(\"volumeChanged\");\n" +
            "    }\n" +
            "    function onMute() {\n" +
            "      fireEvent(\"mute\");\n" +
            "    }\n" +
            "    function fireEvent(event){\n" +
            "      console.log(event);\n" +
            "       window.location = 'imacd://' + event;\n" +
            "    }\n" +
            "    function onContentPauseRequested() {\n" +
            "      // This function is where you should setup UI for showing ads (e.g.\n" +
            "      // display ad timer countdown, disable seeking, etc.)\n" +
            "      videoContent.removeEventListener('ended', contentEndedListener);\n" +
            "    }\n" +
            "    function onContentResumeRequested() {\n" +
            "      // This function is where you should ensure that your UI is ready\n" +
            "      // to play content.\n" +
            "      videoContent.addEventListener('ended', contentEndedListener);\n" +
            "    }\n" +
            "    function onAdError(adErrorEvent) {\n" +
            "      fireEvent(\"error?\"+adErrorEvent.getError());\n" +
            "      console.log(adErrorEvent.getError());\n" +
            "    }\n" +
            "    // An event listener to tell the SDK that our content video\n" +
            "    // is completed so the SDK can play any post-roll ads.\n" +
            "    var contentEndedListener = function() {adsLoader.contentComplete();};\n" +
            "    videoContent.onended = contentEndedListener;\n" +
            "\n" +
            "    // Request video ads.\n" +
            "    adsRequest = new google.ima.AdsRequest();\n" +
            "    adsRequest.adsResponse = 'vast_response';\n" +
            "\n" +
            "    adsRequest.vastLoadTimeout = 30000;\n" +
            "    adsLoader.requestAds(adsRequest);\n" +
            "  }\n" +
            "\n" +
            "    function muteAds(muteValue) {\n" +
            "      if (adsManager.getVolume()!=muteValue) {\n" +
            "        adsManager.setVolume(muteValue);\n" +
            "        volume = muteValue;\n" +
            "        console.log('muteValue' +muteValue);\n" +
            "      }" +
            "    }\n" +
            "  function requestAds() {\n" +
            "    adsLoader.requestAds(adsRequest);\n" +
            "  }\n" +
            "\n" +
            "  function pause() {\n" +
            "    adsManager.pause();\n" +
            "  }\n" +
            "\n" +
            "  function resume() {\n" +
            "    adsManager.resume();\n" +
            "  }\n" +
            "\n" +
            "  function start(){\n" +
            "    adsLoader.requestAds(adsRequest);\n" +
            "  }\n" +
            "\n" +
            "  window.onload = function() {\n" +
            "    setTimeout(initilizePlayer(), 0);\n" +
            "  }\n" +
            "var mute_var = volume===1?'muted':'';"+
            "</script>\n" +
            "    <div id=\"mainContainer\">\n" +
            "      <div id=\"content\">\n" +
            "        <video id=\"contentElement\" preload=\"none\" playsinline autoplay mute_var>\n" +
            "          <source src=\"http://avng-sp-q1media.s3.amazonaws.com/vslider/blank.mp4\" type=\"video/mp4\"></source>\n" +
            "        </video>\n" +
            "      </div>\n" +
            "      <div id=\"adContainer\"></div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>\n";
}
