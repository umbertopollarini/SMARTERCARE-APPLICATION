package com.example.uiapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME="Nexter_SmartercareDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "device_info";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "devicename";
    private static final String COLUMN_MAC = "macaddress";
    private static final String COLUMN_FLAG_SINCRO = "flag_sincro";

    public MyDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " + COLUMN_MAC + " TEXT, " + COLUMN_FLAG_SINCRO + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    void addDeviceToDb(String name, String MAC, String flag_sincro){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_MAC, MAC);
        cv.put(COLUMN_FLAG_SINCRO, flag_sincro);

        long result = db.insert(TABLE_NAME, null, cv);
        if (result ==-1) Toast.makeText(context, "FAILED TO UPLOAD TO DB", Toast.LENGTH_LONG).show();
        else Toast.makeText(context, "SUCCESSFULLY UPLOAD TO DB", Toast.LENGTH_LONG).show();
    }

    Cursor readAllData(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    void updateData(String row_id, String name, String mac, String flag_sincro){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        System.out.println(" - - - - - - -\nMODIFICA:\nNOME " + name);
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_MAC, mac);
        cv.put(COLUMN_FLAG_SINCRO, flag_sincro);

        long result = db.update(TABLE_NAME, cv, "_id=?", new String[]{row_id});
        if(result == -1) Toast.makeText(context, "Errore nell'aggiornamento", Toast.LENGTH_SHORT).show();
        else Toast.makeText(context, "Aggiornato con Successo", Toast.LENGTH_SHORT).show();

    }
    void deleteOneRow(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.delete(TABLE_NAME, "_id=?", new String[]{row_id});
        if(result == -1) Toast.makeText(context, "Errore nell'eliminazione", Toast.LENGTH_SHORT).show();
        else Toast.makeText(context, "Eliminato con Successo", Toast.LENGTH_SHORT).show();
    }
}
