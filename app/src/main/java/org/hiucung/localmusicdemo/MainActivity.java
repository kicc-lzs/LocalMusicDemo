package org.hiucung.localmusicdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.hiucung.localmusicdemo.utils.MusicDataUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static int AUTO_SESSION_ID = 0;//mediaPlayer的autoseeeionid

    ImageView nextIv,playIv,prevIv,iconIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;

    ArrayList<LocalMusicBean> mDatas;//数据源

    private LocalMusicAdapter adapter;//适配器

    MediaPlayerHelper mediaPlayerHelper;//媒体

    int currnetPlayPosition = -1; //初始化播放状态为-1

    int currnetPausePosition;//初始化暂停状态

    LinearLayoutManager layoutManager;//设置布局管理器

    Bitmap albumBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mediaPlayerHelper = MediaPlayerHelper.getInstance(MainActivity.this);
        mDatas = new ArrayList<>();
        //加载本地数据
        mDatas = MusicDataUtil.getMusicData(this);
        //创建适配器
        adapter = new LocalMusicAdapter(this,mDatas);
        musicRv.setAdapter(adapter);
        //设置布局管理器
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        musicRv.setLayoutManager(layoutManager);
        //设置每一项的点击事件
        setEventListener();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.local_music_bottom_iv_prev:
                if (currnetPlayPosition == 0) {
                    //已经是第一首
                    Toast.makeText(this,"已经是第一首音乐啊TNT",Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    currnetPlayPosition = currnetPlayPosition - 1;
                    LocalMusicBean lastBean = mDatas.get(currnetPlayPosition);
                    playMusicInBean(lastBean);
                    break;
                }
            case R.id.local_music_bottom_iv_play:
                if (currnetPlayPosition == -1) {
                    //并没有选中音乐，从第一首歌开始播放
                    Toast.makeText(this,"没有选中音乐啊TNT，帮你选择第一首歌",Toast.LENGTH_SHORT).show();
                    currnetPlayPosition = currnetPlayPosition + 1;
                    LocalMusicBean nextBean = mDatas.get(currnetPlayPosition);
                    playMusicInBean(nextBean);
                    return;
                }else if (mediaPlayerHelper.isPlaying()){
                    //暂停音乐
                    pauseMusic();

                }else{
                    //从暂停位置开始播放音乐
                    playMusic();
                }
                break;
            case R.id.local_music_bottom_iv_next:
                if (currnetPlayPosition == mDatas.size()-1) {
                    //已经是第一首
                    Toast.makeText(this,"已经是最后一首音乐啊TNT",Toast.LENGTH_SHORT).show();
                }else {
                    currnetPlayPosition = currnetPlayPosition + 1;
                    LocalMusicBean nextBean = mDatas.get(currnetPlayPosition);
                    playMusicInBean(nextBean);
                    break;
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /***
     * 点击事件通过实现接口实现类
     */
    private void setEventListener() {
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                currnetPlayPosition = position;//获取点击的position
                LocalMusicBean musicBean = mDatas.get(currnetPlayPosition);//根据position获取歌曲列表中的歌曲
                playMusicInBean(musicBean);//播放该歌曲

                Bundle bundle = new Bundle();
                bundle.putSerializable("mDatas",mDatas);
                bundle.putInt("currnetPlayPosition",currnetPlayPosition);
                bundle.putInt("AUTO_SESSION_ID",AUTO_SESSION_ID);

                BtnBottomDialog fragment = new BtnBottomDialog();//播放音乐弹出正在播放页面
                fragment.setArguments(bundle);//数据传递到fragment中
                fragment.show(getSupportFragmentManager(), "tag");
            }
        });
    }

    /***
     * 提取冗余的代码：更新可视化视图并播放下一首
     * @param musicBean
     */
    public void playMusicInBean(LocalMusicBean musicBean) {

        //设置专辑图片到底部控制栏
        setAlbumImage(musicBean);

        //获取歌曲autoSessionID并传递给
        AUTO_SESSION_ID =  mediaPlayerHelper.getAudioSessionId();

        //底部同步显示歌曲信息
        singerTv.setText(musicBean.getSinger());
        songTv.setText(musicBean.getSong());
        playIv.setImageResource(R.mipmap.landscape_play_icon_normal);
        mediaPlayerHelper.setPath(musicBean.getPath());
        playMusic();
    }

    /***
     * 播放音乐按钮的方法
     */
    private void playMusic() {
        if (!mediaPlayerHelper.isPlaying()) {
            mediaPlayerHelper.setOnMeidaPlayerHelperListener(new MediaPlayerHelper.OnMeidaPlayerHelperListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayerHelper.start();
                }

                @Override
                public void onCompletion(MediaPlayer mp) {
                    //默认单曲循环
                    mediaPlayerHelper.seekTo(0);
                    mediaPlayerHelper.start();
                }
            });
            playIv.setImageResource(R.mipmap.landscape_pause_icon_normal);
        }else {
            //当音乐暂停之后再继续播放时不需要监听是否异步加载完成
            mediaPlayerHelper.seekTo(currnetPausePosition);
            mediaPlayerHelper.start();
            playIv.setImageResource(R.mipmap.landscape_pause_icon_normal);
        }
    }

    /***
     * 暂停音乐按钮的方法
     */
    private void pauseMusic() {
        mediaPlayerHelper.pause();
        //暂停音乐把暂停位置得到
        currnetPausePosition = mediaPlayerHelper.getCurrentPosition();
        playIv.setImageResource(R.mipmap.landscape_play_icon_normal);
    }

    private void initView(){
        checkPermission();
        //初始化控件
        nextIv = findViewById(R.id.local_music_bottom_iv_next);
        playIv = findViewById(R.id.local_music_bottom_iv_play);
        prevIv = findViewById(R.id.local_music_bottom_iv_prev);
        iconIv = findViewById(R.id.local_music_bottom_iv_icon);
        singerTv = findViewById(R.id.local_music_bottom_tv_singer);
        songTv = findViewById(R.id.local_music_bottom_tv_song);
        musicRv = findViewById(R.id.local_music_rv);
        prevIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        nextIv.setOnClickListener(this);
    }

    /**
     * 获取权限
     */
    public void checkPermission() {
        boolean isGranted = true;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //如果没有写sd卡权限
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            if (this.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
            }
            Log.i("权限获取", " ： " + isGranted);
            if (!isGranted) {
                this.requestPermissions(
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission
                                .ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.RECORD_AUDIO
                        },
                        102);
            }
        }
    }

    //同步设置专辑图片到底部控制栏
    private void setAlbumImage(LocalMusicBean musicBean) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(musicBean.getPath());
        byte[] picture = null;
        picture = mediaMetadataRetriever.getEmbeddedPicture();

        if (picture!=null){
            albumBitmap = BitmapFactory.decodeByteArray(picture,0,picture.length);
            iconIv.setImageBitmap(albumBitmap);
        }else {
            iconIv.setImageResource(R.mipmap.audio_player_default_show_bg);
        }
    }
}