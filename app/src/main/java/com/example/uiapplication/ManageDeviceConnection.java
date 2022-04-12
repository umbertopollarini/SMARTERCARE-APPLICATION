package com.example.uiapplication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import android.bluetooth.BluetoothProfile;


import android.content.Context;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class ManageDeviceConnection extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    public BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;
    private BluetoothDevice bledisp;
    private String returnedValue;

    private BluetoothGattCharacteristic notifyCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;

    private Context context_app;
    public int device_status = 2;
    public int buffer_status = 0;
    private String buffer_reader = "";

    private String CHARACTERISTIC_NOTIFY_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    private String CHARACTERISTIC_WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    private String DESCRIPTOR_NOTIFY_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void sendNotifyAct(BluetoothGattCharacteristic c, boolean enabled){
        bluetoothGatt.setCharacteristicNotification(c, enabled);
        BluetoothGattDescriptor descriptor = notifyCharacteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFY_UUID));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (gatt != bluetoothGatt || bluetoothGatt == null) {
                return;
            }

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.d("MainActivity", "STATE_CONNECTED");
                    bluetoothGatt.discoverServices();
                    device_status = 1;
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.d("MainActivity", "STATE_DISCONNECTED");
                    device_status = 0;
                    try {
                        Thread.sleep(600);
                        if (bluetoothGatt != null) bluetoothGatt.close();
                        bluetoothGatt = null;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            bluetoothGatt.requestMtu(512);
            bluetoothGatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_BALANCED);

            for (BluetoothGattService bluetoothGattService : gatt.getServices()) {
                Log.d("Main Activity", "Service: " + bluetoothGattService.getUuid().toString());
                if(bluetoothGattService.getUuid().toString().equals(SERVICE_UUID)){ // CONTROLLO SIA IL SERVIZIO A NOI UTILE
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattService.getCharacteristics()) {
                        Log.d("Main Activity", " - Characteristic: " + bluetoothGattCharacteristic.getUuid().toString());
                        if(bluetoothGattCharacteristic.getUuid().toString().equals(CHARACTERISTIC_NOTIFY_UUID)){ // ATTIVARE NOT.
                            notifyCharacteristic = bluetoothGattCharacteristic;
                            Log.d("notifyCharacteristic", notifyCharacteristic.toString());
                        }
                        if(bluetoothGattCharacteristic.getUuid().toString().equals(CHARACTERISTIC_WRITE_UUID)){ // WRITE CARATT.
                            writeCharacteristic = bluetoothGattCharacteristic;
                            Log.d("writeCharacteristic", writeCharacteristic.toString());
                        }
                    }
                }
            }
            try {
                Thread.sleep(500);
                sendNotifyAct(notifyCharacteristic, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d("Device_Fragment", "Characteristic Value Read " + new String(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d("Device_Fragment", "Characteristic Value Write " + new String(characteristic.getValue()));
        }

        // VISUALIZZO RISPOSTA DEL COMANDO
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            returnedValue = new String(characteristic.getValue());
            buffer_reader += returnedValue;
            buffer_status = 1;
            Log.d("Device_Fragment", "VALORE : " + returnedValue);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d("Device_Fragment", "Ur Read");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d("Device_Fragment", "Descriptor Write");
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d("Device_Fragment", "MTU : " + mtu);
        }
    };
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void connectToDevice(BluetoothDevice dv, Context ct){
        bluetoothDevice = dv;
        bledisp = dv;
        try {
            Thread.sleep(1000);
            if (bluetoothGatt == null){
                bluetoothGatt = bluetoothDevice.connectGatt(ct, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
            }
            else bluetoothGatt.connect();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    public void disconnect(){
        // DISCONNETTO DISPOSITIVO COLLEGATO
        System.out.println("DISCONNESSIONE DISPOSITIVO COLLEGATO");
        bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, false);
        BluetoothGattDescriptor bd = notifyCharacteristic.getDescriptor(UUID.fromString(DESCRIPTOR_NOTIFY_UUID));
        bd.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(bd);
        bluetoothGatt.disconnect();
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void sendCommand(String et){
        // MANDARE COMANDO
        Log.d("Device_Fragment", "MANDO IL COMANDO");
        try {
            writeCharacteristic.setValue(et.getBytes("UTF-8"));
            bluetoothGatt.writeCharacteristic(writeCharacteristic);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    void setContext(Context ct){
        context_app = ct;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    BluetoothDevice getDevice(){
        return bledisp;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    int getConnectionStatus(){
        return device_status;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    String getCommandOutput(){
        return returnedValue;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    String getBuffer_reader(){
        return buffer_reader;
    }
    int getBuffer_Status(){
        return buffer_status;
    }
    // - - - - - - - - - - - - - - - - - - - - - - - - - -
    String setBuffer_reader(String s){
        return buffer_reader = s;
    }
}
