package com.example.uiapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private LinearLayout benessere;
    private LinearLayout amici_affetti;
    private LinearLayout diario;
    private LinearLayout svaghi;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // START & IMPOSTAZIONI VIEW
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // NAVIGAZIONE IMPOSTAZIONI
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationview = findViewById(R.id.nav_view);
        navigationview.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        // - - - - - - - - - -
        // ORARIO E DATA
        String [] numGiorno = {"Sabato", "Domenica", "Lunedì", "Martedì", "Mercoledì", "Giovedì", "Venerdì"};
        TextView clock = findViewById(R.id.clock);
        SimpleDateFormat dateform = new SimpleDateFormat("HH:mm");
        String dateTime = dateform.format(new Date());
        clock.setText(dateTime);

        TextView day = findViewById(R.id.day);
        Calendar calendar = Calendar.getInstance();
        int dayn = calendar.get(Calendar.DAY_OF_WEEK);
        System.out.println("GIORNO " + dayn);
        //day.setText(numGiorno[dayn]);
        //
        // Implement click listener for BENESSERE
        benessere = findViewById(R.id.benessere_section);
        benessere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Salute_Benessere.class);
                startActivity(intent);
            }
        });
        //
        // Implement click listener for AMICI AFFETTI
        amici_affetti = findViewById(R.id.Amici_Affetti);
        amici_affetti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Amici_Affetti.class);
                startActivity(intent);
            }
        });
        //
        // Implement click listener for Diario
        diario = findViewById(R.id.diario);
        diario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Diario.class);
                startActivity(intent);
            }
        });
        //
        // Implement click listener for svaghi
        svaghi = findViewById(R.id.svaghi);
        svaghi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Svaghi.class);
                startActivity(intent);
            }
        });
        //
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_device:
                startActivity(new Intent(MainActivity.this, Device_Fragment.class));
                break;
            case R.id.info_person:
                Toast.makeText(this, "Info", Toast.LENGTH_SHORT).show();
                break;
            case R.id.ble_configured:
                startActivity(new Intent(MainActivity.this, Configured_Devices.class));
                break;
            case R.id.nav_home:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
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