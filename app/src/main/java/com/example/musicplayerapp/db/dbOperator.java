package com.example.musicplayerapp.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.musicplayerapp.song.SongData;

import java.util.ArrayList;
import java.util.List;

public class dbOperator {
private static Helpter helpter;
private static Context context;
private static String tableName;
    public dbOperator(Context context,Helpter  dbHelper,String tableName){
        this.context=context;
        this.helpter = dbHelper;
        this.tableName = tableName;
    }
    //向曲库加入歌曲
    public void insertSong(String name,String singer,String duration,String path) {
        String sql = "insert into "+tableName+" (name,singer,duration,path) values(?,?,?,?)";
        SQLiteDatabase db = helpter.getWritableDatabase();
        db.execSQL(sql, new String[]{name,singer,duration,path});
    }

    //删除曲库中的歌曲
    public void deleteSong (String name) {
        SQLiteDatabase db = helpter.getWritableDatabase();
        String sql = "delete  from "+tableName+" where name = ?";
        db.execSQL(sql, new String[]{name});

    }

    //清空歌曲
    public void deleteAllSong(){
        SQLiteDatabase db = helpter.getWritableDatabase();
        String sql = "delete  from "+tableName;
        db.execSQL(sql);

    }

    //搜索曲库,得到所有歌曲，返回一个list
    public List<SongData> getAllSong(){
        String sql ="select * from "+tableName;
        SQLiteDatabase db = helpter.getReadableDatabase();
        List<SongData> result = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql,new String[]{});
        while (cursor.moveToNext()) {

            SongData songData = new SongData();
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String singer = cursor.getString(cursor.getColumnIndex("singer"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            songData.setName(name);
            songData.setSinger(singer);
            songData.setDuration(duration);
            songData.setPath(path);
            result.add(songData);
        }
        return result;
    }

    //查找该歌曲的路径
    public String getSongPath(String songName){
        String path = "";
        String sql ="select * from "+tableName+"where name = ?";
        SQLiteDatabase db = helpter.getReadableDatabase();
        List<SongData> result = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql,new String[]{songName});
        while (cursor.moveToNext()){
            path = cursor.getString(cursor.getColumnIndex("name"));
        }
        return path;
    }
}
