package com.example.uiapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class CustomAdapterDeviceData extends RecyclerView.Adapter<CustomAdapterDeviceData.MyViewHolder>{
    private Context context;
    Activity activity;
    private ArrayList device_id, device_name, device_mac, temperature, humidity, date;
    private String mac_add;

    CustomAdapterDeviceData(Activity activity, Context context, String mac_add, ArrayList device_id, ArrayList device_name, ArrayList device_mac, ArrayList temperature, ArrayList humidity, ArrayList date){
        this.activity = activity;
        this.context = context;
        this.device_id = device_id;
        this.device_name = device_name;
        this.device_mac = device_mac;
        this.temperature = temperature;
        this.humidity = humidity;
        this.date = date;
        this.mac_add = mac_add;
        Collections.reverse(device_id);
        Collections.reverse(device_name);
        Collections.reverse(device_mac);
        Collections.reverse(temperature);
        Collections.reverse(humidity);
        Collections.reverse(date);
    }
    @NonNull
    @Override
    public CustomAdapterDeviceData.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_device_info, parent, false);
        return new CustomAdapterDeviceData.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapterDeviceData.MyViewHolder holder, int position) {
        if(mac_add.equals(device_mac.get(position))){
            holder.device_id_txt.setText(String.valueOf(device_id.get(position)));
            holder.device_temperature_txt.setText("Temperatura = " + String.valueOf(temperature.get(position)));
            holder.device_humidity_txt.setText("Umidità = " + String.valueOf(humidity.get(position)));
            holder.device_date_info_txt.setText(String.valueOf(date.get(position)));
        }
        /*holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", String.valueOf(device_id.get(holder.getAdapterPosition())));
                intent.putExtra("mac", String.valueOf(device_mac.get(holder.getAdapterPosition())));
                intent.putExtra("flag_sincro", String.valueOf(flag_sincro.get(holder.getAdapterPosition())));
                intent.putExtra("flag_sincro", String.valueOf(flag_sincro.get(holder.getAdapterPosition())));
                activity.startActivityForResult(intent, 1);
            }
        });*/
    }

    @Override
    public int getItemCount() { return device_id.size(); }
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView device_id_txt, device_temperature_txt, device_humidity_txt, device_date_info_txt;
        LinearLayout mainLayout;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            device_id_txt = itemView.findViewById(R.id.device_id_txt);
            device_temperature_txt = itemView.findViewById(R.id.temperature_info);
            device_humidity_txt = itemView.findViewById(R.id.humidity_info);
            device_date_info_txt = itemView.findViewById(R.id.date_info);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
