package com.example.musicplayerapp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Helpter extends SQLiteOpenHelper {
    public static final String DB_NAME = "musicDB.db";
    public static final int DB_VERSION = 1;
    public static final String sql = "create table allMusicTable("+
            "name TEXT," +
            "singer TEXT,"+
            /*"size long,"+*/
            "duration TEXT,"+
            "path TEXT)";
    public Helpter(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
