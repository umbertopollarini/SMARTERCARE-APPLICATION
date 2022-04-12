package com.example.uiapplication;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.google.android.material.navigation.NavigationView;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WatchConfiguration extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;
    private Intent intent;
    private BluetoothDevice bledisp;
    private TextView blename;
    private TextView nameconf;
    private TextView MACconf;
    private TextView UUIDconf;
    private TextView command_out;

    private LinearLayout oxygen;
    private LinearLayout battery;
    private LinearLayout temperature;
    private LinearLayout buzzer1;
    private LinearLayout buzzer2;

    private LinearLayout disconnect_ble;
    private LinearLayout container_button;

    private MyDatabaseHelper myDB;
    private ArrayList<String> device_id, device_name, device_mac, device_flag_info;
    private Button bt;
    private LinearLayout LLbt;

    private ManageDeviceConnectionWatch connection_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_configuration);

        // PRENDO I DATI MANDATI
        intent = getIntent();
        bledisp = intent.getParcelableExtra("bledevice");

        oxygen = findViewById(R.id.oxygen);
        battery = findViewById(R.id.battery);
        temperature = findViewById(R.id.temperature);
        buzzer1 = findViewById(R.id.buzzer1);
        buzzer2 = findViewById(R.id.buzzer2);

        container_button = findViewById(R.id.container_butt);
        container_button.setVisibility(View.INVISIBLE);

        connection_device = new ManageDeviceConnectionWatch();
        connection_device.connectToDevice(bledisp, WatchConfiguration.this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(connection_device.getConnectionStatus() == 1){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            container_button.setVisibility(View.VISIBLE);
                            timer.cancel();
                        }
                    });
                }
            }
        }, 0, 500);//wait 0 ms before doing the action and do it every 500ms

        blename = (TextView)findViewById(R.id.bledevicename);
        nameconf = (TextView)findViewById(R.id.namedevice);
        blename.setText(bledisp.getName());
        nameconf.setText(bledisp.getName());

        MACconf = (TextView)findViewById(R.id.macdevice);
        MACconf.setText(bledisp.getAddress());



        // bottone disconnessione
        disconnect_ble = findViewById(R.id.disconnect);
        disconnect_ble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connection_device.disconnect();
                Toast.makeText(WatchConfiguration.this, "Disconnesso dal Dispositivo", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        command_out = findViewById(R.id.command_output);

        oxygen.setOnClickListener(sendcommand);
        battery.setOnClickListener(sendcommand);
        temperature.setOnClickListener(sendcommand);
        buzzer1.setOnClickListener(sendcommand);
        buzzer2.setOnClickListener(sendcommand);

        // CHECK PER BOTTONE AGG AI PREFERITI
        myDB = new MyDatabaseHelper(WatchConfiguration.this);
        device_id = new ArrayList<>();
        device_name = new ArrayList<>();
        device_mac = new ArrayList<>();
        device_flag_info = new ArrayList<>();

        loadDatafromDB();

        if(!device_mac.contains(bledisp.getAddress().toString())) {
            LLbt = (LinearLayout)findViewById(R.id.buttonDisplay);
            LinearLayout bt = (LinearLayout)getLayoutInflater().inflate(R.layout.button_add_device,null, false);
            bt.setOnClickListener(bleaddtodb);
            LLbt.addView(bt);
        }else{
            LLbt = (LinearLayout)findViewById(R.id.buttonDisplay);
            LinearLayout bt = (LinearLayout)getLayoutInflater().inflate(R.layout.red_alert,null, false);
            LLbt.addView(bt);
        }

        // NAVIGAZIONE IMPOSTAZIONI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationview = findViewById(R.id.nav_view);
        navigationview.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Timer timer_disconnection = new Timer();
        timer_disconnection.schedule(new TimerTask() {
            @Override
            public void run() {
                if(connection_device.getConnectionStatus() == 0){
                    timer_disconnection.cancel();
                    finish();
                }
            }
        }, 0, 200);//wait 0 ms before doing the action and do it every 200ms
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        connection_device.disconnect();
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(WatchConfiguration.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(WatchConfiguration.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(WatchConfiguration.this, MainActivity.class));
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
    // - - - - - - - - - - - - ADD TO DB - - - - - - -
    LinearLayout.OnClickListener bleaddtodb = new ImageView.OnClickListener(){
        public void onClick(View v){
            System.out.println("AGGIUNGO DISPOSITIVO " + bledisp.getName().toString().trim());
            MyDatabaseHelper myDB = new MyDatabaseHelper(WatchConfiguration.this);
            myDB.addDeviceToDb(bledisp.getName().toString().trim(), bledisp.getAddress().toString().trim(), "false");
            connection_device.disconnect();
            finish();
        }
    };

    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void loadDatafromDB(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No Data.", Toast.LENGTH_SHORT).show();
        } else{
            while(cursor.moveToNext()){
                device_id.add(cursor.getString(0));
                device_name.add(cursor.getString(1));
                device_mac.add(cursor.getString(2));
                device_flag_info.add(cursor.getString(3));
            }
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    View.OnClickListener sendcommand = new View.OnClickListener(){
        public void onClick(View v){
            command_out.setText("");
            connection_device.setBuffer_reader("");
            if(v.getId() == R.id.oxygen) {
                connection_device.sendCommand("set_cfg accel_sh 1\n");

            }
            if(v.getId() == R.id.battery) {
                connection_device.sendCommand("set_cfg stream bin\n");

            }
            if(v.getId() == R.id.temperature) {
                connection_device.sendCommand("read ppg 9\n");

            }
            if(v.getId() == R.id.buzzer1) {
                connection_device.sendCommand("stop sensors\n");

            }
            if(v.getId() == R.id.buzzer2) {
                connection_device.sendCommand("get_reg ppg 0\n");
            }
            try {
                Thread.sleep(1000); // aspetto che il messaggio del comando completo sia arrivato
                command_out.setText(connection_device.getBuffer_reader());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    public String toHex(String arg) {
        try {
            return String.format("%040x", new BigInteger(1, arg.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
