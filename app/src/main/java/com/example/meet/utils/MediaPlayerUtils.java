package com.example.meet.utils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;

/**
 * 封装MediaPlayer的相关操作
 */
public class MediaPlayerUtils {

    /**
     * 定义音乐的播放状态
     * play：播放状态
     * pause：暂停状态
     * stop：停止状态（默认状态）
     */
    public static final int MEDIA_STATUS_PLAY=0;
    public static final int MEDIA_STATUS_PAUSE=1;
    public static final int MEDIA_STATUS_STOP=2;
    public int MEDIA_STATUS=MEDIA_STATUS_STOP;

    private static final int HANDLER_MESSAGE=101;

    private MediaPlayer mMediaPlayer;

    private OnMusicProgressListener musicProgressListener;

    /**
     * 计算歌曲的进度
     * 1.开始播放时，循环计算播放时长
     * 2.将计算结果对外抛出
     */
    private Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what){
                case HANDLER_MESSAGE:
                    if(musicProgressListener != null){
                        int currentPosition=getCurrentPosition();
                        int percent=(int) (((float) currentPosition) / ((float) getDuration()) * 100);
                        musicProgressListener.OnProgress(currentPosition,percent);
                        mHandler.sendEmptyMessageDelayed(HANDLER_MESSAGE,1000);
                    }
                    break;
            }
            return false;
        }
    });

    public MediaPlayerUtils() {
        mMediaPlayer=new MediaPlayer();
    }

    /**
     * 判断音乐是否处于播放状态
     */
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    /**
     * 开始播放音乐
     * @param path
     */
    public void startPlay(AssetFileDescriptor path){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path.getFileDescriptor(),path.getStartOffset(),path.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MEDIA_STATUS=MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(HANDLER_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startPlay(String path){
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            MEDIA_STATUS=MEDIA_STATUS_PLAY;
            mHandler.sendEmptyMessage(HANDLER_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 继续播放
     */
    public void continuePlay(){
        mMediaPlayer.start();
        MEDIA_STATUS=MEDIA_STATUS_PLAY;
        mHandler.sendEmptyMessage(HANDLER_MESSAGE);
    }

    /**
     * 暂停播放
     */
    public void pausePlay(){
        if(isPlaying()){
            mMediaPlayer.pause();
            MEDIA_STATUS=MEDIA_STATUS_PAUSE;
            mHandler.removeMessages(HANDLER_MESSAGE);
        }
    }
    /**
     * 停止播放
     */
    public void stopPlay(){
        mMediaPlayer.stop();
        MEDIA_STATUS=MEDIA_STATUS_STOP;
        mHandler.removeMessages(HANDLER_MESSAGE);
    }
    /**
     * 跳转位置
     */
    public void seekTo(int millisecond){
        mMediaPlayer.seekTo(millisecond);
    }

    /**
     * 获取音乐当前播放的位置
     */
    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    /**
     * 获取音乐总时长
     */
    public int getDuration(){
        return mMediaPlayer.getDuration();
    }
    /**
     * 是否需要循环播放
     */
    public void setLooping(boolean isLooping){
        mMediaPlayer.setLooping(isLooping);
    }
    /**
     * 监听播放结束
     */
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
        mMediaPlayer.setOnCompletionListener(listener);
    }

    /**
     * 播放错误
     */
    public void setOnErrorListener(MediaPlayer.OnErrorListener listener){
        mMediaPlayer.setOnErrorListener(listener);
    }

    /**
     * 监听音乐播放进度
     */
    public void setMusicProgressListener(OnMusicProgressListener listener){
        musicProgressListener=listener;
    }
    public interface OnMusicProgressListener{
        void OnProgress(int progress, int percent);
    }
}
