package com.beaconinside.proximityserviceexample;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.beaconinside.proximitysdk.ProximityService;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initServiceWithToken();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProximityService.INTENT_BEACON_REGION_ENTER);
        intentFilter.addAction(ProximityService.INTENT_BEACON_REGION_EXIT);
        intentFilter.addAction(ProximityService.INTENT_BEACON_REGION_UPDATE);
        intentFilter.addAction(ProximityService.INTENT_GEOFENCE_ENTER);
        intentFilter.addAction(ProximityService.INTENT_GEOFENCE_EXIT);
        intentFilter.addAction(ProximityService.INTENT_CAMPAIGN_NOTIFICATION);
        intentFilter.addAction(ProximityService.INTENT_CAMPAIGN_CONVERSION);
        this.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(broadcastReceiver);
    }

    @AfterPermissionGranted(LOCATION_PERMISSION)
    private void initServiceWithToken() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            ProximityService.init(this, "YOUR_APP_TOKEN");
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_rationale_location),
                    LOCATION_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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

                    if (ProximityService.CAMPAIGN_TYPE_CUSTOM.equals(campaignType)) {
                        // Implementation of notification handling required
                        // Get the campaign data specified in the Beaconinside DMP Campaign interface
                        String data = intent.getStringExtra("MY_CUSTOM_KEY");
                        // Show notification
                        // Get the notification ID for conversion tracking
                        String notificationId = intent.getStringExtra(ProximityService.INTENT_EXTRA_NOTIFICATION_ID);
                        // Track the conversion for this custom campaign event after the intended action has been performed (e.g. user clicked on the notification)
                        // The broadcast event INTENT_CAMPAIGN_CONVERSION will not be send after calling ProximityService.sendConversion().
                        ProximityService.sendConversion(MainActivity.this, notificationId);
                    }
                case ProximityService.INTENT_CAMPAIGN_CONVERSION:
                    Log.d("BI", "CAMPAIGN CONVERSION: " + intent.getStringExtra(ProximityService.INTENT_EXTRA_CAMPAIGN_ID));
            }
        }
    };
}
