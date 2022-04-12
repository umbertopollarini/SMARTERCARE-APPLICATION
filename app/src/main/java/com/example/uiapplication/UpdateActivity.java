package com.example.uiapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawer;
    private TextView name, mac;
    private LinearLayout buttonSave, buttonDelete;
    private String name_value, mac_value, id;

    public static final int LOCATION_PERMISSION = 9001;

    ScanSettings scanSettings;
    List<ScanFilter> scanFilters;
    ArrayList<BluetoothDevice> foundDevices = new ArrayList<>();

    public BluetoothAdapter bluetoothAdapter;
    public android.bluetooth.BluetoothManager bluetoothManager;
    public BluetoothLeScanner bluetoothLeScanner;

    private ImageView ble_img;

    private String [] items = {"Dispositivi Nexter", "Tutti Dispositivi"};

    private BluetoothDevice btDevice;
    private BluetoothDevice device_conf;

    private ArrayList<TextView> ble_device_ui;

    private ManageDeviceConnection connection_device;

    private TextView status_connection;
    private TextView dbm_device;

    private int dbm_distance_value;
    private boolean flag_conn;
    private String flag_sincronizzazione;

    private LinearLayout container_routine;
    private LinearLayout mex_sincronizzazione;
    private LinearLayout bt_routine;
    private LinearLayout circle;
    private LinearLayout container_all;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // NAVIGAZIONE IMPOSTAZIONI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationview = findViewById(R.id.nav_view);
        navigationview.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // - - - - - -

        bt_routine = (LinearLayout)getLayoutInflater().inflate(R.layout.button_create_routine,null, false);
        bt_routine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {connection_device.sendCommand("t");}
                }, 2000);

                // BARRA DI CARICAMENTO
                LoadingDialog loadingDialog = new LoadingDialog(UpdateActivity.this);
                loadingDialog.startLoadingDialog();

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("STATUS : " + connection_device.getBuffer_Status());
                        if(connection_device.getBuffer_Status() == 1){
                            loadingDialog.dismissDialog();
                            Intent i = new Intent(UpdateActivity.this, Device_Synchronize.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("bledevice", device_conf); // mando device
                            i.putExtra("device_data", connection_device.getBuffer_reader());
                            startActivity(i);
                            timer.cancel();
                        }
                    }
                }, 0, 500); //wait 0 ms before doing the action and do it every 200ms
            }
        });

        name = findViewById(R.id.namedevice);
        mac = findViewById(R.id.macdevice);
        buttonSave = findViewById(R.id.buttonSave);
        buttonDelete = findViewById(R.id.buttonDelete);

        ble_device_ui = new ArrayList<>();
        connection_device = new ManageDeviceConnection();

        status_connection = findViewById(R.id.status_connection);
        dbm_device = findViewById(R.id.dbm_ble);
        dbm_device.setVisibility(View.INVISIBLE);

        flag_conn = true;

        getIntentData();
        checkLocationPermission();

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyDatabaseHelper myDB =  new MyDatabaseHelper(UpdateActivity.this);
                myDB.updateData(id, name.getText().toString(), mac.getText().toString(), "false");
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDialog();
            }
        });
        Timer timer = new Timer();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run(){
                if(foundDevices.contains(device_conf)){
                    System.out.println("DISPOSITIVO DAL DB TROVATO NEi DINTORNI, DB : " + dbm_distance_value);
                    if(flag_conn == true){
                        connection_device.connectToDevice(device_conf, UpdateActivity.this);
                        //bluetoothLeScanner.stopScan(mScanCallback);

                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if(connection_device.getConnectionStatus() == 1){
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            bluetoothLeScanner.stopScan(mScanCallback);
                                            status_connection.setText("Connesso");
                                            status_connection.setTextColor(Color.parseColor("#31D515"));
                                            container_routine = findViewById(R.id.routine_button);

                                            container_routine.addView(bt_routine);

                                            // se non è mai stata effettuata una sincronizzazione allora aggiungo un messaggio di avviso
                                            if(flag_sincronizzazione.equals("false")) {
                                                mex_sincronizzazione = (LinearLayout)getLayoutInflater().inflate(R.layout.alert_no_sincronizzazione,null, false);
                                                container_routine.addView(mex_sincronizzazione);
                                            }
                                            timer.cancel();
                                        }
                                    });
                                }
                            }
                        }, 0, 500);//wait 0 ms before doing the action and do it every 500ms
                    }
                    else{
                        timer.cancel();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(UpdateActivity.this, "Impossibile da connettere, dispositivo distante", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }, 10000);
        Timer timer_disconnection = new Timer();
        timer_disconnection.schedule(new TimerTask() {
            @Override
            public void run() {
                if(connection_device.getConnectionStatus() == 0){
                    bluetoothLeScanner.stopScan(mScanCallback);
                    timer_disconnection.cancel();
                    finish();
                }
            }
        }, 0, 200); //wait 0 ms before doing the action and do it every 200ms
    }

    void confirmDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Eliminare " + name_value + " ?");
        builder.setMessage("Sei sicuro di eliminare questo dispositivo ?");
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MyDatabaseHelper myDB =  new MyDatabaseHelper(UpdateActivity.this);
                myDB.deleteOneRow(id);
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // - - - --
            }
        });
        builder.create().show();
    }

    void getIntentData(){
        if(getIntent().hasExtra("id") && getIntent().hasExtra("name") && getIntent().hasExtra("mac")){
            id = getIntent().getStringExtra("id");
            name_value = getIntent().getStringExtra("name");
            mac_value = getIntent().getStringExtra("mac");
            flag_sincronizzazione = getIntent().getStringExtra("flag_sincro");
            name.setText(name_value);
            mac.setText(mac_value);
        }else{
            Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        connection_device.disconnect();
        bluetoothLeScanner.stopScan(mScanCallback);
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(UpdateActivity.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(UpdateActivity.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(UpdateActivity.this, MainActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void onBackPressed() {
        connection_device.disconnect();
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else super.onBackPressed();
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

            if(btDevice.getAddress().toString().equals(mac_value)) {
                device_conf = btDevice;
                dbm_device.setVisibility(View.VISIBLE);
                System.out.println(device_conf.getName() + " " + resultData.get("rssi").toString() + " dBm");
                dbm_device.setText(resultData.get("rssi").toString() + " dBm");
                dbm_distance_value = result.getRssi();
                if(dbm_distance_value < -80) flag_conn = false;
            }
            if (!foundDevices.contains(btDevice)) {
                Log.d("DISPOSITIVO TROVATO ",  btDevice.getName() + " " + btDevice.getAddress() + " " + resultData.get("rssi").toString());
                foundDevices.add(btDevice);
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
        System.out.println("CHECK PERMESSI");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        } else {
            initBluetooth();
        }
    }
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
}