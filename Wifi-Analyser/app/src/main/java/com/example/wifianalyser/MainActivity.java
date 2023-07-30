package com.example.wifianalyser;


import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 123;
    private WifiManager wifiManager;
    private ListView listViewWifiNetworks;
    private ArrayAdapter<String> wifiListAdapter;
    private List<ScanResult> scanResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize WiFiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Initialize WiFiManager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Initialize UI elements
        listViewWifiNetworks = findViewById(R.id.listViewWifiNetworks);
        Button btnScan = findViewById(R.id.btnScan);

        // Set a click listener for the "Scan WiFi Networks" button
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndRequestPermissions();
            }
        });

        // Set a click listener for the WiFi network in the ListView to show signal strength
        listViewWifiNetworks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (scanResults != null && position >= 0 && position < scanResults.size()) {
                    int signalStrength = scanResults.get(position).level;
                    updateSignalStrengthText(signalStrength);
                }
            }
        });
    }

    // Check and request required permissions
    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CHANGE_WIFI_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startWiFiScan();
        }
    }

    // Handle permission request results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call superclass method

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                startWiFiScan();
            } else {
                Toast.makeText(this, "Permissions are required to scan WiFi networks.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Perform WiFi scanning
    private void startWiFiScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (wifiManager.isWifiEnabled()) {
                wifiManager.startScan();
                scanResults = wifiManager.getScanResults();
                List<String> wifiNetworks = new ArrayList<>();
                for (ScanResult scanResult : scanResults) {
                    wifiNetworks.add(scanResult.SSID);
                }

                // Display the list of WiFi networks in the ListView
                wifiListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, wifiNetworks);
                listViewWifiNetworks.setAdapter(wifiListAdapter);
            } else {
                Toast.makeText(this, "Please enable WiFi to scan networks.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Request the ACCESS_FINE_LOCATION permission explicitly if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_CODE);
        }
    }

    // Update the signal strength text view with the given signal strength
    private void updateSignalStrengthText(int signalStrength) {
        TextView tvSignalStrength = findViewById(R.id.tvSignalStrength);
        tvSignalStrength.setText("Signal Strength: " + signalStrength + " dBm");
    }
}