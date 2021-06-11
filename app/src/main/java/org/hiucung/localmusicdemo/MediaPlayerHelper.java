package org.hiucung.localmusicdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

/**
 * MediaPlayerHelper
 * 媒体工具类，使用单例设计模式保证使用的媒体对象只有一个
 */
public class MediaPlayerHelper {

    @SuppressLint("StaticFieldLeak")
    private static MediaPlayerHelper instance;

    private final Context mContext;//上下文对象
    private final MediaPlayer mMediaPlayer;//MediaPlayer媒体类
    private String mPath;//歌曲路径

    public static MediaPlayerHelper getInstance(Context context) {

        if (instance == null) {
            synchronized (MediaPlayerHelper.class) {
                if (instance == null) {
                    instance = new MediaPlayerHelper(context);
                }
            }
        }

        return instance;
    }

    private MediaPlayerHelper(Context context) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
    }

    private OnMeidaPlayerHelperListener onMeidaPlayerHelperListener;//媒体监听

    /**
     * 媒体监听接口，播放准备和播放完成
     */
    public interface OnMeidaPlayerHelperListener {
        void onPrepared(MediaPlayer mp);
        void onCompletion(MediaPlayer mp);
    }

    public void setOnMeidaPlayerHelperListener(OnMeidaPlayerHelperListener onMeidaPlayerHelperListener) {
        this.onMeidaPlayerHelperListener = onMeidaPlayerHelperListener;
    }

    /**
     * 设置当前需要播放的音乐
     */
    public void setPath (String path) {

        // 1、音乐正在播放，重置音乐播放状态
        if (mMediaPlayer.isPlaying() || !path.equals(mPath)) {
            mMediaPlayer.reset();
        }
        mPath = path;

        // 2、设置播放音乐路径
        try {
            mMediaPlayer.setDataSource(mContext, Uri.parse(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3、准备播放
        mMediaPlayer.prepareAsync();
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (onMeidaPlayerHelperListener != null) {
                    onMeidaPlayerHelperListener.onPrepared(mp);
                }
            }
        });

        //监听音乐播放完成
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (onMeidaPlayerHelperListener != null) {
                    onMeidaPlayerHelperListener.onCompletion(mp);
                }
            }
        });

    }

    /**
     * 返回正在播放的音乐路径
     */
    public String getPath () {
        return mPath;
    }


    /**
     * 播放音乐
     */
    public void start () {
        if (mMediaPlayer.isPlaying()) return;
        mMediaPlayer.start();
    }

    /**
     * 返回正在播放的音乐路径AudioSessionId
     */
    public int getAudioSessionId() {
        return mMediaPlayer.getAudioSessionId();
    }

    /**
     * 暂停播放
     */
    public void pause () {
        mMediaPlayer.pause();
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    /**
     * 获取媒体播放的位置（进度条）
     */
    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }

    /**
     * 获取媒体播放的总时长
     */
    public int getDuration(){
        return mMediaPlayer.getDuration();
    }

    /**
     * 定位到媒体播放进度
     */
    public void seekTo(int progress){
        mMediaPlayer.seekTo(progress);
    }


}
