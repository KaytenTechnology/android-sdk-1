
Proximity Service SDK for Android
==========

# Summary

The Beaconinside SDK simplifies the integration of beacons, geofences and proximity services into your mobile app. A Beaconinside application token is required, [sign up for free][dmp].

## Table of contents

  * [Integrate the SDK](#integrate-the-sdk)
    + [Upgrade from Beaconinside SDK 2.x.x](#upgrade-from-beaconinside-sdk-2xx)
    + [Permissions](#permissions)
    + [Behaviour](#behaviour)
    + [Account setup](#account-setup)
  * [FAQs & Guides](#faqs---guides)
    + [Usage Guides](#usage-guides)
  * [Advanced features](#advanced-features)
    + [Custom user identifiers](#custom-user-identifiers)
    + [Broadcast intents with meta data](#broadcast-intents-with-meta-data)
    + [Webhooks](#webhooks)
    + [Access the API](#access-the-api)
  * [Support](#support)
  * [License](#license)


## Integrate the SDK

The minimum Android version for the SDK is Android 4.0.3 (API level 15).
However beacon detection will only work on devices which have at least Android 4.3 Jelly Bean (API level 18)
and support Bluetooth Low Energy (Bluetooth 4.0).

1. Add the Beaconinside Maven repository to your project `build.gradle` file
```
allprojects {
	repositories {
		jcenter()
		google()
		// Beaconinside Maven repository
		maven {
	        url "https://maven.beaconinside.com"
		}
	}
}
```

2. Add the dependency to your `app.gradle` file
```xml
dependencies {
	...
	implementation 'com.beaconinside:proximity-sdk:3.0.0'
}
```

3. Import the ProximityService in your Main Activity:
```java
	import com.beaconinside.proximitysdk.ProximityService;
```


4. Copy this code to your Main Activity `onCreate()`, replace API_TOKEN with the token from https://dmp.beaconinside.com
```java
	ProximityService.init(this, API_TOKEN);
```

### Upgrade from Beaconinside SDK 2.x.x

    - Remove the 'beaconinside-androidsdk' from your gradle file.
    - Follow the "Integrate the SDK" section
    - Change all calls to the 'BeaconService' class to 'ProximityService'

### Permissions

Importing the library will automatically add all needed permissions to your app if not already used.

    android.permission.ACCESS_FINE_LOCATION - Geofence and beacon scanning
    android.permission.RECEIVE_BOOT_COMPLETED - Start the SDK when the device is rebooted
    android.permission.BLUETOOTH - Detecting beacons
    android.permission.BLUETOOTH_ADMIN - Detecting beacons
    android.permission.INTERNET - Fetch remote content and capture events
    com.google.android.gms.permission.ACTIVITY_RECOGNITION - Detect smartphone movements

On Android 6.0 and above you need to request the android.permission.ACCESS_FINE_LOCATION permission during runtime.

### Behaviour

The SDK starts when the ProximityService.init() is called and runs in the
background till ProximityService.terminate() is called.

### Account setup

* Sign up for a [Beaconinside Account][dmp] to access the web and mobile dashboards to manage all beacons, geofences and proximity services.

* Add nearby beacons and/or geofences in *MANAGE* for initial testing. For beacons you should select the right vendor and UUID, Major and Minor values.

* Create a campaign with _All Beacons_ group selected and a notification text. Make sure the scheduling criteria are valid for today and the status is *Published*.

* Get your `Application Token` under `Account -> Applications`. It should be kept secret as it uniquely identifies your mobile application.


## FAQs & Guides

### Usage Guides

* [Campaign Demo Guide](http://developers.beaconinside.com/docs/demoing-beacons-and-geofences)
* [Setting up a virtual beacon](http://developers.beaconinside.com/docs/virtual-ibeacon)
* [Getting started with geofencing](http://developers.beaconinside.com/docs/geofencing-getting-started)
* [Advantages SDK over API](http://developers.beaconinside.com/docs/sdk-vs-api-integration)
* [SDK battery drain analysis](http://developers.beaconinside.com/docs/sdk-battery-drain)

## Advanced features

Once the SDK is integrated you can use the following functionalities and customize the SDK behavior.

### Custom user identifiers

You can pass 2 custom user identifiers to the library to ensure data interoperability with your CRM, analytics or marketing systems. All personal identifiable information (PII) should be hashed.

You have to use the `ServiceConfig` class to set custom identifiers upon initialization.

```java

    ServiceConfig config = new ServiceConfig();
    config.setCustomID1(ID1);
    config.setCustomID2(ID2);
    ProximityService.init(this, API_KEY, config);
```

By default the identifier for advertising on Android (AAID) is collected if the Google's Play services 'ads' dependency is linked to your application.

If you want to include the advertising identifier add the following line to your build.gradle file.

```
implementation 'com.google.android.gms:play-services-ads:15.0.1'
```
### Broadcast intents with meta data

The Proximity Service SDK broadcasts by default entry, exit and update events for beacon zones via the broadcast system.

To get the broadcasts you have to register a BroadcastReceiver with an IntentFilter for the events you want to receive:

	ProximityService.INTENT_BEACON_REGION_ENTER
	ProximityService.INTENT_BEACON_REGION_UPDATE
	ProximityService.INTENT_BEACON_REGION_EXIT

	ProximityService.INTENT_GEOFENCE_ENTER
	ProximityService.INTENT_GEOFENCE_EXIT

	ProximityService.INTENT_CAMPAIGN_NOTIFICATION
	ProximityService.INTENT_CAMPAIGN_CONVERSION

Additionally you get meta information about the beacon e.g. uuid, major, minor rssi and proximity.
You will also get the beacon meta data you entered the [infrastructure management](https://dmp.beaconinside.com)

- ProximityServiceBroadcastReceiver.java

```java

public class ProximityServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case ProximityService.INTENT_BEACON_REGION_ENTER:
                Log.d("BI", "REGION ENTER " + intent.getExtras().toString());
                break;
            case ProximityService.INTENT_BEACON_REGION_EXIT:
                Log.d("BI", "REGION EXIT " + intent.getStringExtra(ProximityService.INTENT_EXTRA_BEACON_ID));
                break;
            case ProximityService.INTENT_BEACON_REGION_UPDATE:
                Log.d("BI", "REGION UPDATE rssi: " + intent.getIntExtra(ProximityService.INTENT_EXTRA_RSSI, 0)
                        + " proximity: " + intent.getStringExtra(ProximityService.INTENT_EXTRA_PROXIMITY));
                break;
            case ProximityService.INTENT_CAMPAIGN_NOTIFICATION:
                Log.d("BI", "CAMPAIGN NOTIFICATION: " + intent.getExtras());

                String campaignType = intent.getStringExtra(ProximityService.INTENT_EXTRA_CAMPAIGN_TYPE);
                if (ProximityService.CAMPAIGN_TYPE_NOTIFICATION.equals(campaignType)) {
                    // Notification was shown to the user, no action required. Conversion tracking will be automatically reported.
                    // The broadcast event INTENT_CAMPAIGN_CONVERSION will be send after a successful conversion.
                } else if (ProximityService.CAMPAIGN_TYPE_CUSTOM.equals(campaignType)) {
                    // Implementation of notification handling required

                    // Get the campaign data specified in the Beaconinside DMP Campaign interface
                    String data = intent.getStringExtra(MY_CUSTOM_KEY);

                    // Show notification
                    ...

                    // Get the notification ID for conversion tracking
                    String notificationId = intent.getStringExtra(ProximityService.INTENT_EXTRA_NOTIFICATION_ID);

                    // Track the conversion for this custom campaign event after the intended action has been performed (e.g. user clicked on the notification)
                    // The broadcast event INTENT_CAMPAIGN_CONVERSION will not be send after calling ProximityService.sendConversion().

                    // ProximityService.sendConversion(context, notificationId);
                }
            case ProximityService.INTENT_CAMPAIGN_CONVERSION:
                Log.d("BI", "CAMPAIGN CONVERSION: " + intent.getStringExtra(ProximityService.INTENT_EXTRA_CAMPAIGN_ID));
        }
    }
}

```


- AndroidManifest.xml

```xml

<receiver
	android:name=".ProximityServiceBroadcastReceiver"
	android:exported="false">
	<intent-filter>
		<action android:name="BeaconServiceRegionEnter"/>
		<action android:name="BeaconServiceRegionUpdate"/>
		<action android:name="BeaconServiceRegionExit"/>

		<action android:name="GeofenceServiceRegionEnter"/>
		<action android:name="GeofenceServiceRegionExit"/>

		<action android:name="CampaignNotification"/>
		<action android:name="CampaignConversion"/>
	</intent-filter>
</receiver>

```


### Webhooks

In the web panel you can set up webhooks to get server-side user interaction events in near real-time. This feature is enabled by default.

### Access the API

All data can be accessed via server-side APIs. Take a look at the [Beaconinside Developer Hub][dev-hub] for the public Manager and Analytics API reference.

## Support

Just [drop us](mailto:support@beaconinside.com) a message if there are any issues or questions.

## License

Copyright (c) 2014-2018 Beaconinside GmbH. All rights reserved.

[dev-hub]: http://developers.beaconinside.com
[beaconinside]: https://www.beaconinside.com
[dmp]: https://dmp.beaconinside.com
[releases]: https://github.com/beaconinside/android-sdk/releases
