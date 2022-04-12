package com.example.uiapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class Configured_Devices extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, NavigationView.OnNavigationItemSelectedListener{
    private DrawerLayout drawer;
    private MyDatabaseHelper myDB;
    private ArrayList<String> device_id, device_name, device_mac, device_flag_sincro;
    private CustomAdapter customAdapter;
    private RecyclerView recycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.configured_devices);

        // NAVIGAZIONE IMPOSTAZIONI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationview = findViewById(R.id.nav_view);
        navigationview.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // LETTURA DATI DA DB
        myDB = new MyDatabaseHelper(Configured_Devices.this);
        device_id = new ArrayList<>();
        device_name = new ArrayList<>();
        device_mac = new ArrayList<>();
        device_flag_sincro = new ArrayList<>();

        displayDatafromDB();

        recycleView = findViewById(R.id.recycleView);
        customAdapter = new CustomAdapter(Configured_Devices.this, this, device_id, device_name, device_mac, device_flag_sincro);
        recycleView.setAdapter(customAdapter);
        recycleView.setLayoutManager(new LinearLayoutManager(Configured_Devices.this));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            recreate();
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void displayDatafromDB(){
        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No Data.", Toast.LENGTH_SHORT).show();
        } else{
            while(cursor.moveToNext()){
                device_id.add(cursor.getString(0));
                device_name.add(cursor.getString(1));
                device_mac.add(cursor.getString(2));
                device_flag_sincro.add(cursor.getString(3));
            }
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(Configured_Devices.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(Configured_Devices.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(Configured_Devices.this, MainActivity.class));
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
}
