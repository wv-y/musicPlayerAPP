package com.example.musicplayerapp.song;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.example.musicplayerapp.db.Helpter;
import com.example.musicplayerapp.db.dbOperator;

import java.util.ArrayList;
import java.util.List;

public class Getsong {
    /**
     * 扫描音频文件，返回一个list
     */
    public  static List<SongData> getMusicData(Context context) {
        List<SongData> list = new ArrayList<>();
        Helpter helpter = new Helpter(context);
        dbOperator operator = new dbOperator(context,helpter,"allMusicTable");
        operator.deleteAllSong();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SongData songdata = new SongData();
                songdata.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
                songdata.setSinger(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
               // songdata.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
                songdata.setDuration(formatTime(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))));
                songdata.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));


                if(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))>1000) {
                    list.add(songdata);
                    // 切割标题，分离出歌曲名和歌手 （本地媒体库读取的歌曲信息不规范）
                    if (songdata.getName().contains("-")) {
                        String[] str = songdata.getName().split("-");
                        songdata.setSinger(str[0]);
                        songdata.setName(str[1]);
                    }
                    operator.insertSong(songdata.getName(),songdata.getSinger(),songdata.getDuration(),songdata.getPath());
                   // Log.i("path",songdata.getPath());
                }
            }

        }
        cursor.close();
        return list;
    }

    /**
     * 定义一个方法用来格式化获取到的时间
     */
    public static String formatTime(int time) {
        if (time / 1000 % 60 < 10) {
            return time / 1000 / 60 + ":0" + time / 1000 % 60;

        } else {
            return time / 1000 / 60 + ":" + time / 1000 % 60;
        }

    }
}
