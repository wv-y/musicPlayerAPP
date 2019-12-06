package com.example.musicplayerapp.song;

public class SongData {
    private String name;
    private String singer;
   // private long size;
    private String duration;
    private String path;

    public void setName(String name){
        this.name=name;
    }
    public void setSinger(String singer){
        this.singer=singer;
    }
   /* public void setSize(long size){
        this.size=size;
    }*/
    public void setDuration(String duration){
        this.duration=duration;
    }
    public void setPath(String path){
        this.path=path;
    }

    public String getName(){
        return name;
    }
    public String getSinger(){
        return singer;
    }
   /* public long getSize(){
        return size;
    }*/

    public String getDuration() {
        return duration;
    }

    public String getPath() {
        return path;
    }
}
