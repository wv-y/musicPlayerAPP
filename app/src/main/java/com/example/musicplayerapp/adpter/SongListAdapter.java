package com.example.musicplayerapp.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.musicplayerapp.R;
import com.example.musicplayerapp.fragment.MainFragment;
import com.example.musicplayerapp.song.SongData;

import java.util.List;

public class SongListAdapter extends ArrayAdapter {
    private final int resourceId;

    public SongListAdapter(Context context, int textViewResourceId, List<SongData> items) {
        super(context, textViewResourceId, items);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SongData songData = (SongData) getItem(position); // 获取当前项的实例
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);//实例化一个对象

        TextView songName = (TextView) view.findViewById(R.id.songName);
        TextView songSinger = (TextView) view.findViewById(R.id.songSinger);
        TextView songTime = (TextView) view.findViewById(R.id.songTime);

        songName.setText(songData.getName()); //为文本视图设置文本内容
        songSinger.setText(songData.getSinger());
        songTime.setText(songData.getDuration());
        return view;
    }
}
