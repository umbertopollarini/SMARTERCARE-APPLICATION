package com.example.uiapplication;

import android.annotation.SuppressLint;
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

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
    private Context context;
    Activity activity;
    private ArrayList device_id, device_name, device_mac, flag_sincro;
    CustomAdapter(Activity activity, Context context, ArrayList device_id, ArrayList device_name, ArrayList device_mac, ArrayList flag_sincro){
        this.activity = activity;
        this.context = context;
        this.device_id = device_id;
        this.device_name = device_name;
        this.device_mac = device_mac;
        this.flag_sincro = flag_sincro;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_row_device, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.device_id_txt.setText(String.valueOf(device_id.get(position)));
        holder.device_name_txt.setText(String.valueOf(device_name.get(position)));
        holder.device_mac_txt.setText(String.valueOf(device_mac.get(position)));
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UpdateActivity.class);
                intent.putExtra("id", String.valueOf(device_id.get(holder.getAdapterPosition())));
                intent.putExtra("name", String.valueOf(device_name.get(holder.getAdapterPosition())));
                intent.putExtra("mac", String.valueOf(device_mac.get(holder.getAdapterPosition())));
                intent.putExtra("flag_sincro", String.valueOf(flag_sincro.get(holder.getAdapterPosition())));
                activity.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public int getItemCount() { return device_id.size(); }
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView device_id_txt, device_name_txt, device_mac_txt;
        LinearLayout mainLayout;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            device_id_txt = itemView.findViewById(R.id.device_id_txt);
            device_name_txt = itemView.findViewById(R.id.textView1);
            device_mac_txt = itemView.findViewById(R.id.textView2);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
