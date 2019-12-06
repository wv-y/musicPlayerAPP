package com.example.musicplayerapp.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musicplayerapp.R;
import com.example.musicplayerapp.adpter.SongListAdapter;
import com.example.musicplayerapp.db.Helpter;
import com.example.musicplayerapp.db.dbOperator;
import com.example.musicplayerapp.service.Loading_view;
import com.example.musicplayerapp.song.Getsong;
import com.example.musicplayerapp.song.SongData;

import java.util.List;
import java.util.Random;


import static com.example.musicplayerapp.MainActivity.verifyStoragePermissions;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<SongData> songList;    //保存当前播放歌曲列表
    private MediaPlayer mediaPlayer = null;
    private ImageView imagePlay, imageLast, imageNext, imageOrder;    //控制按钮
    private SeekBar songSeekBar;
    private boolean ischanging = false; // 判断seekbar是否正在滑动
    private Thread thread = null;
    private int id; //记录当前播放歌曲的位置
    private TextView songPlaying, songTimeStart, songTimeEnd; //记录正在播放的歌曲名
    private int playStyle;  //1为列表循环、2为单曲循环、3为随机播放
    private Handler handler;
    private Loading_view loading;

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

  /* @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
        mediaPlayer.reset();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        verifyStoragePermissions(getActivity());

        ListView songListView = (ListView) getActivity().findViewById(R.id.songList);
        imagePlay = getActivity().findViewById(R.id.info_play);
        imageLast = getActivity().findViewById(R.id.info_last);
        imageNext = getActivity().findViewById(R.id.info_next);
        imageOrder = getActivity().findViewById(R.id.info_order);
        songSeekBar = getActivity().findViewById(R.id.info_seekBar);

        songPlaying = (TextView) getActivity().findViewById(R.id.play_song_name);
        songPlaying.setSelected(true);//设置文本走马灯循环
        songTimeStart = (TextView) getActivity().findViewById(R.id.song_time_start);
        songTimeEnd = (TextView) getActivity().findViewById(R.id.song_time_end);
        playStyle = 1;
        mediaPlayer = null;

        //从数据库获得歌曲
        Helpter helpter = new Helpter(getActivity());
        dbOperator operator = new dbOperator(getActivity(), helpter, "allMusicTable");
        songList = operator.getAllSong();
        //songList = Getsong.getMusicData(getActivity());
        SongListAdapter adapter = new SongListAdapter(getActivity(), R.layout.song_list_layout, songList);
        songListView.setAdapter(adapter);
        songListView.setOnItemClickListener(this); //listview点击事件

        //点击搜索按钮，扫描本地文件将歌曲加入数据库，并重新加载listview
        Button buttonSearch = (Button) getActivity().findViewById(R.id.but_search);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListView mListView = (ListView) getActivity().findViewById(R.id.songList);

                /*Handler handler1 = new Handler() {    //加载数据时显示 loading 动画未实现
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(msg.what == 1){
                            loading = new Loading_view(getActivity(), R.style.CustomDialog);
                            loading.show();
                        }
                        else
                            loading.dismiss();
                    }
                };

                Thread thread1 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Message ms = new Message();
                        ms.what = 1;
                        handler.sendMessage(ms);
                        if (getsong()) {
                            ms.what = 0;
                        }
                        else{
                            ms.what = 1;
                        }
                        handler.sendMessage(ms);
                    }
                });*/
                songList = Getsong.getMusicData(getActivity());
                Helpter helpter = new Helpter(getActivity());
                dbOperator operator = new dbOperator(getActivity(), helpter, "allMusicTable");
                for (int i = 0; i < songList.size(); i++) {
                    SongData song;
                    song = songList.get(i);
                    operator.insertSong(song.getName(), song.getSinger(), song.getDuration(), song.getPath());
                }
                SongListAdapter adapter = new SongListAdapter(getActivity(), R.layout.song_list_layout, songList);
                mListView.setAdapter(adapter);
            }
        });


        //监听歌曲播放完成取下一首
        setMediaPlayerListener();

        //播放按钮点击事件
        imagePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (songPlaying.getText().toString().equals("请选择播放歌曲")) {
                    //没有播放歌曲则取第一首歌
                    id = 0;
                    SongData songdata = songList.get(0);
                    initMediaPlayer(songdata.getPath(), songdata.getName() + " " + songdata.getSinger(), songdata.getDuration());
                    setPlayView();
                } else if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    setPlayView();
                } else {
                    mediaPlayer.start();
                    setPlayView();
                }
            }
        });

        //上一首
        imageLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null) {
                    if (playStyle == 2) {
                        if (id > 0) {
                            id--;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());

                        } else {
                            id = songList.size() - 1;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                        }

                    } else if (playStyle == 3) {   //随机播放
                        id = getRandom();
                        SongData songData = songList.get(id);
                        initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                    } else {    //列表循环
                        if (id > 0) {
                            id--;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());

                        } else {
                            id = songList.size() - 1;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "请先选择歌曲", Toast.LENGTH_SHORT).show();
                }
                setPlayView();
            }
        });


        //下一首
        imageNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mediaPlayer != null) {
                    if (playStyle == 2) {
                        if (id == songList.size() - 1) {
                            id = 0;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());

                        } else {
                            id++;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                        }

                    } else if (playStyle == 3) {   //随机播放
                        id = getRandom();
                        SongData songData = songList.get(id);
                        initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                    } else {    //列表循环
                        if (id == songList.size() - 1) {
                            id = 0;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());

                        } else {
                            id++;
                            SongData songData = songList.get(id);
                            initMediaPlayer(songData.getPath(), songData.getName() + " " + songData.getSinger(), songData.getDuration());
                        }
                    }

                } else {
                    Toast.makeText(getActivity(), "请先选择歌曲", Toast.LENGTH_SHORT).show();
                }
                setPlayView();
            }
        });


        imageOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playStyle == 1) { //列表循环--->单曲循环
                    playStyle = 2;
                    imageOrder.setImageResource(R.drawable.ic_play_xunhuan);
                    Toast.makeText(getActivity(), "单曲循环", Toast.LENGTH_SHORT).show();
                } else if (playStyle == 2) {    //单曲循环-->随机播放
                    playStyle = 3;
                    imageOrder.setImageResource(R.drawable.ic_play_random_black);
                    Toast.makeText(getActivity(), "随机播放", Toast.LENGTH_SHORT).show();
                } else {      //随机播放-->列表循环
                    playStyle = 1;
                    imageOrder.setImageResource(R.drawable.ic_play_order_black);
                    Toast.makeText(getActivity(), "列表循环", Toast.LENGTH_SHORT).show();
                }
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 1) {
                    songTimeStart.setText(Getsong.formatTime(mediaPlayer.getCurrentPosition()));
                    songSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
            }
        };


        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ischanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ischanging = false;
                if (mediaPlayer != null)
                    mediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        /*thread = new Thread(new SeekBarThread());
        thread.start();*/
    }

    /**
     * 播放歌曲
     */
    public void initMediaPlayer(String path, String name, String durtion) {

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        //播放内存卡中音频文件
        mediaPlayer = new MediaPlayer();

        songPlaying.setText(name);  //设置正在播放的歌曲的名字
        songTimeEnd.setText(durtion);//设置歌曲时间
        //设置音源
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
        songSeekBar.setMax(mediaPlayer.getDuration());
        thread = new Thread(new SeekBarThread());
        thread.start();
    }

    /*
     * listView的点击事件监听器
     * */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id2) {
        //通过view获取其内部的组件，进而进行操作

        String path = "";
        //大多数情况下，position和id相同，并且都从0开始
        SongData song;
        song = songList.get(position);
        id = position;
        path = song.getPath();
        // 播放歌曲
        initMediaPlayer(path, song.getName() + " " + song.getSinger(), song.getDuration());
        setPlayView();

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return false;
    }


    public void setPlayView() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {   //改为暂停样式
                imagePlay.setImageResource(R.drawable.ic_pause_black);
            } else {
                imagePlay.setImageResource(R.drawable.ic_play_black);
            }
        }
    }

    /**
     * 取随机数
     *
     * @return
     */
    public int getRandom() {
        Random random = new Random();
        int i = random.nextInt(songList.size());
        return i;
    }

    // 自定义的线程,用于下方seekbar的刷新
    class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (mediaPlayer != null) {
                if (mediaPlayer != null && !ischanging) {
                    Message ms = new Message();
                    ms.what = 1;
                    Bundle bundle = new Bundle();
                    //ms.setData(bundle);
                    handler.sendMessage(ms);

                    try {
                        // 每500毫秒更新一次位置
                        Thread.sleep(100);
                        // 播放进度

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void setMediaPlayerListener() {
        // 监听mediaplayer播放完毕时调用
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {

                    switch (playStyle) {
                        case 1:
                            if (id < songList.size() - 1) {   //列表循环 且不是最后一首歌
                                //播放下首歌
                                id++;
                                SongData songdata = songList.get(id);
                                initMediaPlayer(songdata.getPath(), songdata.getName() + " " + songdata.getSinger(), songdata.getDuration());
                            } else if (id == songList.size() - 1) {   //列表循环 且是最后一首歌
                                //播放第一首歌
                                id = 0;
                                SongData songdata = songList.get(0);
                                initMediaPlayer(songdata.getPath(), songdata.getName() + " " + songdata.getSinger(), songdata.getDuration());

                            }
                            break;
                        case 2:
                            mediaPlayer.seekTo(0);
                            mediaPlayer.setLooping(true);
                            break;
                        case 3:
                            id = getRandom();
                            SongData songdata = songList.get(id);
                            initMediaPlayer(songdata.getPath(), songdata.getName() + " " + songdata.getSinger(), songdata.getDuration());
                            break;
                        default:
                            break;
                    }
                }
            });
        }
    }

    public boolean getsong() {
        boolean i = false;
        try {
            songList = Getsong.getMusicData(getActivity());
            i = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return i;
    }

    public void loding(View v) {//点击加载并按钮模仿网络请求
        loading = new Loading_view(getActivity(), R.style.CustomDialog);
        loading.show();
        new Handler().postDelayed(new Runnable() {//定义延时任务模仿网络请求
            @Override
            public void run() {
                loading.dismiss();//3秒后调用关闭加载的方法
            }
        }, 3000);

    }

    }
