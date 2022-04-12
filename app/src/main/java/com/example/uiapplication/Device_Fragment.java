package com.example.uiapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.view.Gravity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Device_Fragment extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {
    public static final int LOCATION_PERMISSION = 9001;

    ScanSettings scanSettings;
    List<ScanFilter> scanFilters;
    ArrayList<BluetoothDevice> foundDevices = new ArrayList<>();

    public BluetoothAdapter bluetoothAdapter;
    public android.bluetooth.BluetoothManager bluetoothManager;
    public BluetoothLeScanner bluetoothLeScanner;

    private DrawerLayout drawer;
    private ImageView ble_img;

    private String [] items = {"Dispositivi Nexter", "Tutti Dispositivi"};
    private AutoCompleteTextView autoCompletetxt;
    private ArrayAdapter<String> adapterItems;
    private BluetoothDevice btDevice;

    private ArrayList<TextView> ble_device_ui;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_device);

        // DROPDOWN
        autoCompletetxt = findViewById(R.id.auto_complete_txt);
        adapterItems = new ArrayAdapter<String>(this, R.layout.list_item, items);
        autoCompletetxt.setAdapter(adapterItems);
        autoCompletetxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(getApplicationContext(), "Item: "+ item, Toast.LENGTH_SHORT).show();
            }
        });

        ble_device_ui = new ArrayList<>();

        // ACTION LISTENER
        ble_img = (ImageView)findViewById(R.id.img_ble);
        ble_img.setOnClickListener(k);

        // CHECK SE SI È COLLEGATI AD UN DISPOSITIVO
        // DA CAPIRRE COME FARE

        // NAVIGAZIONE IMPOSTAZIONI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationview = findViewById(R.id.nav_view);
        navigationview.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // AVVIO RICERCA DISPOSITIVI
        checkLocationPermission();
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void initBluetooth(){
        bluetoothManager = null;
        bluetoothAdapter = null;
        bluetoothLeScanner = null;
        scanSettings = null;

        bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
            scanFilters = new ArrayList<>();
            ScanFilter sc = new ScanFilter.Builder().build();
            scanFilters.add(sc);

            //inizia lo scan
            Toast.makeText(this, "Ricerca dispositivi...", Toast.LENGTH_LONG).show();
            bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            btDevice = result.getDevice();
            Bundle resultData = new Bundle();
            resultData.putParcelable("device", btDevice);
            resultData.putInt("rssi", result.getRssi());
            resultData.putParcelable("scanResult", result);
            if (!foundDevices.contains(btDevice)) {
                Log.d("DISPOSITIVO TROVATO ",  btDevice.getName() + " " + btDevice.getAddress() + " " + resultData.get("rssi").toString());
                foundDevices.add(btDevice);
                displaydeviceinapp(foundDevices.size()-1, resultData.get("rssi").toString(), btDevice.getName());
            }
            else{
                ble_device_ui.get(foundDevices.indexOf(btDevice)).setText(resultData.get("rssi").toString() + " dBm");
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };
    // - - - - - - - - - - - - REFRESH DEVICE - - - - - - -
    ImageView.OnClickListener k = new ImageView.OnClickListener(){
        public void onClick(View v){
            // RIAVVIO RICERCA DISPOSITIVI
            foundDevices.clear();
            LinearLayout displaydevice = (LinearLayout)findViewById(R.id.fragment_container);
            displaydevice.removeAllViews();
            ble_device_ui.clear();
            checkLocationPermission();
        }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void checkLocationPermission() {
        // timer che ferma lo scan dopo 5 secondi e stampa i dispositivi trovati
        /*new Timer().schedule(new TimerTask() {
            @Override
            public void run(){
                bluetoothLeScanner.stopScan(mScanCallback);
                ble_img.setVisibility(View.VISIBLE);
            }
        }, 10000);*/
        // - - - - - - - - -
        ble_img.setVisibility(View.INVISIBLE);
        System.out.println("CHECK PERMESSI");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            initBluetooth();
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void displaydeviceinapp(int i, String db, String ble_name){
        LinearLayout displaydevice = (LinearLayout)findViewById(R.id.fragment_container);
        CardView cv;
        LinearLayout disp;
        ImageView iv;
        TextView tx, tx2, txdb;

        System.out.println("[ " + i + " ]" + foundDevices.get(i).getName() + " " + foundDevices.get(i).getAddress());

        cv = new CardView(this);
        disp = new LinearLayout(this);
        iv = new ImageView(this);
        tx = new TextView(this);
        txdb = new TextView(this);
        tx2 = new TextView(this);

        // CARDVIEW
        LinearLayout.LayoutParams cardviewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardviewParams.setMargins(25, 15, 25, 15);
        cv.setId(i);
        cv.setOnClickListener(l);
        cv.setLayoutParams(cardviewParams);
        cv.setBackgroundResource(R.drawable.not_device);

        // LINEARLAYOUT
        disp.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        disp.setOrientation(LinearLayout.VERTICAL);

        // IMAGEVIEW
        LinearLayout.LayoutParams imageviewParams = new LinearLayout.LayoutParams(100, 80);
        imageviewParams.setMargins(0, 15, 0, 15);
        imageviewParams.gravity = Gravity.CENTER_HORIZONTAL;
        iv.setBackgroundResource(R.drawable.ic_search_disp);
        iv.setLayoutParams(imageviewParams);

        // TEXTVIEW
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        textParams.setMargins(25, 0, 25, 10);
        
        tx.setGravity(Gravity.CENTER);
        tx.setTypeface(null, Typeface.BOLD);
        tx.setLayoutParams(textParams);
        tx.setTextColor(Color.BLACK);
        if(foundDevices.get(i).getName() != null) tx.setText(foundDevices.get(i).getName());
        else tx.setText("Dispositivo Sconosciuto");

        tx2.setLayoutParams(textParams);
        tx2.setTextColor(Color.BLACK);
        tx2.setGravity(Gravity.CENTER);
        if(foundDevices.get(i).getAddress() != null) tx2.setText(foundDevices.get(i).getAddress());
        else tx2.setText("Dispositivo Sconosciuto");

        txdb.setTypeface(null, Typeface.BOLD);
        txdb.setLayoutParams(textParams);
        txdb.setId(10000 - i);
        txdb.setTextColor(Color.BLACK);
        txdb.setGravity(Gravity.CENTER);
        txdb.setText(db + " dBm");

        CardView finalcv = cv;
        LinearLayout finaldp = disp;
        ImageView finaliv = iv;
        TextView finaltx = tx;
        TextView finaltx2 = tx2;
        TextView finaltxdb = txdb;
        ble_device_ui.add(txdb);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                finaldp.addView(finaliv);
                finaldp.addView(finaltx);
                finaldp.addView(finaltx2);
                finaldp.addView(finaltxdb);
                finalcv.addView(finaldp);
                displaydevice.addView(finalcv);
            }
        });
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    // FINE MODIFICHE
    View.OnClickListener l = new View.OnClickListener(){
        public void onClick(View v){
            System.out.println("\n- - - - - - - - - - - - - \nCONNESSIONE DISP. " + foundDevices.get(v.getId()).getName() + "\n- - - - - - - - - - - - -");
            // START ACTIVITY DEVICE CONFIGURATION SENA KILLARE DEVICE_FRAGMENT
            bluetoothLeScanner.stopScan(mScanCallback);
            if(foundDevices.get(v.getId()).getAddress().equals("60:77:71:BF:69:CD")){
                System.out.println("OROLOGIO NEXTER RILEVATO");
                Intent i = new Intent(Device_Fragment.this, WatchConfiguration.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("bledevice", foundDevices.get(v.getId())); // mando device
                startActivity(i);
            }
            else{
                Intent i = new Intent(Device_Fragment.this, Device_Configuration.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("bledevice", foundDevices.get(v.getId())); // mando device
                startActivity(i);
            }

        }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initBluetooth();
                }
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        bluetoothLeScanner.stopScan(mScanCallback);
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(Device_Fragment.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(Device_Fragment.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(Device_Fragment.this, MainActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void onBackPressed() {
        bluetoothLeScanner.stopScan(mScanCallback);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else super.onBackPressed();
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
}
