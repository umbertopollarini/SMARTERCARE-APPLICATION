package com.example.uiapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Device_Synchronize extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;

    private LinearLayout create_sincro;

    private TextView device;
    private DeviceInformationDatabase myDB;

    private ArrayList device_id, device_name, device_mac, device_temp, device_hum, device_date;

    private Intent intent;

    private BluetoothDevice bledisp;

    private String device_data, temperature="", humidity="";

    private RecyclerView recycleView;

    private CustomAdapterDeviceData customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_synchronize);

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

        intent = getIntent();
        bledisp = intent.getParcelableExtra("bledevice");
        device_data = intent.getStringExtra("device_data");

        System.out.println("DATI IN INGRESSO : " + device_data);

        device = findViewById(R.id.device_name);
        device.setText(bledisp.getName());

        temperature = device_data.split(",")[0];
        humidity = device_data.split(",")[1];

        temperature = temperature.replace("Temp=", "");
        humidity = humidity.replace(" Humidity=", "");

        System.out.println("TEMP " + temperature);
        System.out.println("HUM " + humidity);

        // CHECK PER BOTTONE AGG AI PREFERITI
        myDB = new DeviceInformationDatabase(Device_Synchronize.this, bledisp.getAddress());
        device_id = new ArrayList<>();
        device_name = new ArrayList<>();
        device_mac = new ArrayList<>();
        device_temp = new ArrayList<>();
        device_hum = new ArrayList<>();
        device_date = new ArrayList<>();
        loadDatafromDB();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        create_sincro = findViewById(R.id.create_sincro);
        create_sincro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                System.out.println("AGGIUNGO DISPOSITIVO " + bledisp.getName().toString().trim());
                // aggiungo la riga nel database
                DeviceInformationDatabase myDB = new DeviceInformationDatabase(Device_Synchronize.this, bledisp.getAddress());
                myDB.addDeviceToDb(bledisp.getName().toString().trim(), bledisp.getAddress().toString().trim(),  temperature, humidity, formatter.format(date));
                // resetto gli arraylist per le info
                device_id.clear();
                device_name.clear();
                device_mac.clear();
                device_temp.clear();
                device_hum.clear();
                device_date.clear();
                // ricarico i dati aggiornati
                loadDatafromDB();

                // ricarico la recycle view
                customAdapter = new CustomAdapterDeviceData(Device_Synchronize.this, Device_Synchronize.this, bledisp.getAddress(), device_id, device_name, device_mac, device_temp, device_hum, device_date);
                recycleView.setAdapter(customAdapter);
                recycleView.setLayoutManager(new LinearLayoutManager(Device_Synchronize.this));
            }
        });
        recycleView = findViewById(R.id.ShowDataStorage);
        customAdapter = new CustomAdapterDeviceData(Device_Synchronize.this, this, bledisp.getAddress(), device_id, device_name, device_mac, device_temp, device_hum, device_date);
        recycleView.setAdapter(customAdapter);
        recycleView.setLayoutManager(new LinearLayoutManager(Device_Synchronize.this));
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(Device_Synchronize.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(Device_Synchronize.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(Device_Synchronize.this, MainActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else super.onBackPressed();
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void loadDatafromDB(){
        Cursor cursor = myDB.readDeviceData(bledisp.getAddress());
        if(cursor.getCount() == 0){
            Toast.makeText(this, "Nessun dato salvato", Toast.LENGTH_SHORT).show();
        } else{
            while(cursor.moveToNext()){
                device_id.add(cursor.getString(0));
                device_name.add(cursor.getString(1));
                device_mac.add(cursor.getString(2));
                device_temp.add(cursor.getString(3));
                device_hum.add(cursor.getString(4));
                device_date.add(cursor.getString(5));
            }
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
}
