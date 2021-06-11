package org.hiucung.localmusicdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import org.hiucung.localmusicdemo.utils.BlurUtil;
import org.hiucung.localmusicdemo.utils.ImageUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.util.NierAnimator;

public class BtnBottomDialog extends BottomSheetDialogFragment{

    public static final String TAG = "BtnBottomDialog";

    ArrayList<LocalMusicBean> mDatas = new ArrayList<>();//数据源

    ImageView backgoundImageView;

    SurfaceView surfaceView;

    LocalMusicBean musicBean;

    int currnetPlayPosition;

    MediaPlayerHelper mediaPlayerHelper;

    static int AUTO_SESSION_ID = 0;//mediaPlayer的autoseeeionid

    NierVisualizerManager visualizerManager;//音乐可视化组件

    TextView playerSong,playerSinger,playerEndTime;

    @SuppressLint("StaticFieldLeak")
    static TextView playerStartTime;

    ImageView playerBanner,playerBackground,mediaPlayerSong,lastSong,nextSong;

    SurfaceView playerSurfaceview;

    @SuppressLint("StaticFieldLeak")
    static SeekBar songSeekBar;//进度条seekbar

    int currnetPausePosition = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) return super.onCreateDialog(savedInstanceState);

        BottomSheetDialog dialog = new BottomSheetDialog(getActivity(), R.style.Theme_MaterialComponents_BottomSheetDialog);

        dialog.setContentView(R.layout.dialog_fragment_layout);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,  @Nullable Bundle savedInstanceState){
        Context mContext = getContext();
        Log.d("TAG","onCreateView启动");

        View view = inflater.inflate(R.layout.dialog_fragment_layout, container, false);

        initViews(view);
        initData();
        buttonOnClickListener();
        //设置进度条更新TimerTask调度器
        TimerTask timerTask  = new TimerTask(){
            public void run(){
                if(mediaPlayerHelper.isPlaying()){//当歌曲播放的时候时间更新
                    updateTimer();
                }
            }
        };
        new Timer().scheduleAtFixedRate(timerTask,0,500);//固定码率运算，500毫秒执行一次

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //获取dialog对象
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();

        //获取diglog的根部局
        FrameLayout bottomSheet = dialog.getDelegate().findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            //获取根部局的LayoutParams对象
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
            layoutParams.height = getPeekHeight();
            //修改弹窗的最大高度，不允许上滑（默认可以上滑）
            bottomSheet.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        visualizerManager.pause();
        Log.d(TAG,"BottomSheetDialogFragment已销毁");
    }


    /**
     * 弹窗高度，默认为屏幕高度的四分之三
     * 子类可重写该方法返回peekHeight
     * @return height
     */
    protected int getPeekHeight() {
        int peekHeight = getResources().getDisplayMetrics().heightPixels;
        //设置弹窗高度为屏幕高度的3/4
        return peekHeight;
    }

    /**
     * 更新进度条进度和显示对应的时间,使用getActivity()时需要判断null
     */
    private void updateTimer(){
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> {
                //不用这个的话UI线程会报错
                int currentMs = mediaPlayerHelper.getCurrentPosition();
                int sec = currentMs / 1000;
                int min = sec / 60;
                sec -= min * 60;
                //格式化当前歌曲的进度时间
                String time = String.format("%02d:%02d", min, sec);
                //进度条更新
                songSeekBar.setProgress(currentMs);
                //进度时间刷新
                playerStartTime.setText(time);
            });
        }
    }

    @SuppressLint("CutPasteId")
    private void initViews(View view) {

        backgoundImageView = view.findViewById(R.id.iv_background_menu);
        surfaceView = view.findViewById(R.id.player_surfaceview_menu);
        //SurfaceView设置透明背景
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //音频可视化控件初始化
        visualizerManager = new NierVisualizerManager();

        playerBanner = view.findViewById(R.id.player_iv_banner_menu);
        playerSong = view.findViewById(R.id.player_tv_song_menu);
        playerSinger = view.findViewById(R.id.player_tv_singer_menu);
        playerBackground = view.findViewById(R.id.iv_background_menu);
        mediaPlayerSong = view.findViewById(R.id.playsong_menu);
        playerEndTime= view.findViewById(R.id.endTime_menu);

        playerSurfaceview = view.findViewById(R.id.player_surfaceview_menu);
        songSeekBar = view.findViewById(R.id.songseek_menu);
        playerStartTime = view.findViewById(R.id.startTime_menu);

        lastSong = view.findViewById(R.id.lastsong_menu);
        nextSong = view.findViewById(R.id.nextsong_menu);

    }

    private void initData() {
        //使用getActivity()获取fragment依附的activity的context上下文对象
        mediaPlayerHelper = MediaPlayerHelper.getInstance(getActivity());

        Bundle bundle = this.getArguments();//得到从Activity传来的数据
        mDatas = (ArrayList<LocalMusicBean>) bundle.getSerializable("mDatas");
        currnetPlayPosition = bundle.getInt("currnetPlayPosition");

        musicBean = mDatas.get(currnetPlayPosition);
        AUTO_SESSION_ID = bundle.getInt("AUTO_SESSION_ID");

        String path = musicBean.getPath();
        String musicSong = musicBean.getSong();
        String musicDuration = musicBean.getDuration();
        String musicSinger = musicBean.getSinger();

        //更新频谱、音乐时长、背景模糊等
        updateMusicUI(path, musicSong, musicDuration, musicSinger, AUTO_SESSION_ID);
    }

    /***
     * 更新播放页面的所有UI视图
     * @param path
     * @param musicSong
     * @param musicDuration
     * @param musicSinger
     * @param auto_session_id
     */
    private void updateMusicUI(String path, String musicSong, String musicDuration, String musicSinger, int auto_session_id) {

        //获取📛mp3文件路径直接获取歌曲专辑bitmap
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] picture = null;
        picture = mediaMetadataRetriever.getEmbeddedPicture();

        //有的歌曲并没有专辑图，所以需要判空
        if(picture!=null) {
            //byte数组转换成位图
            Bitmap bytes2Bimap = ImageUtil.Bytes2Bimap(picture);

            //位图变圆
            Bitmap bm = ImageUtil.ClipSquareBitmap(bytes2Bimap, 250, 150);
            playerBanner.setImageBitmap(bm);

            //设置背景图
            //直接调用BlurUtil中的doBlur方法。3个参数在上述工具类代码中有详细注释
            Bitmap bgbm = BlurUtil.doBlur(bytes2Bimap, 20, 5);
            playerBackground.setImageBitmap(bgbm);
        }else {
            //播放页点击上一首下一首时，如果该歌曲没有专辑图片，需要设置默认的图片
            playerBanner.setImageResource(R.mipmap.audio_player_default_show_bg);
        }
        //设置歌手和歌名
        playerSong.setText(musicSong);
        playerSinger.setText(musicSinger);
        playerEndTime.setText(musicDuration);


        //visualizerManager.release();//当每次选中新的歌曲item时，销毁框架实例，释放资源
        visualizerManager = new NierVisualizerManager();//可视化控件初始化
        if (auto_session_id != 0){
            visualizerManager.init(auto_session_id);//给visualizer传入autoSessionID
            visualizerManager.start(playerSurfaceview, new IRenderer[]{getMyCircleBarRenderer()});
        }

        int duration = mediaPlayerHelper.getDuration();
        //设置进度条的最大值是根据音乐的时间总长
        songSeekBar.setMax(duration);
    }

    /**
     * @return 自定义圆形可视化UI
     */
    private CircleBarRenderer getMyCircleBarRenderer() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        return new CircleBarRenderer(
                paint, 2,
                CircleBarRenderer.Type.TYPE_A,
                0.9f,
                .8f,
                new NierAnimator(
                        new LinearInterpolator(),
                        20000,
                        new float[]{0f, -360f},
                        true)
        );
    }

    /**
     * BtnBottomDialog所有的点击监听事件
     */
    private void buttonOnClickListener() {
        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //fromUser判断是否来自用户
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //按下进度条的时候，把进度传给MediaPlayer
                if(fromUser){
                    mediaPlayerHelper.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //这个是当拖动的时候，当拖动的时候暂停音乐
                mediaPlayerHelper.pause();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //当松开进度条的时候，播放音乐
                mediaPlayerHelper.start();
            }
        });

        lastSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currnetPlayPosition == 0) {
                    //已经是第一首
                    Toast.makeText(getActivity(),"已经是第一首音乐啊TNT",Toast.LENGTH_SHORT).show();
                }else {
                    currnetPlayPosition = currnetPlayPosition - 1;
                    musicBean = mDatas.get(currnetPlayPosition);
                    preparedMediaPlayer();
                    mediaPlayerSong.setImageResource(R.drawable.ic_baseline_pause_24);
                    visualizerManager.release();
                    visualizerManager = new NierVisualizerManager();
                    updateMusicUI(musicBean.getPath(),musicBean.getSong(),musicBean.getDuration(),musicBean.getSinger(),mediaPlayerHelper.getAudioSessionId());
                }
            }
        });

        mediaPlayerSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayerHelper.isPlaying()) {//如果正在播放，就暂停
                    mediaPlayerHelper.pause();
                    currnetPausePosition = mediaPlayerHelper.getCurrentPosition();
                    mediaPlayerSong.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                }else {
                    //如果不在播放，就是暂停状态，继续播放
                    mediaPlayerHelper.seekTo(currnetPausePosition);
                    mediaPlayerHelper.start();
                    mediaPlayerSong.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                }
            }
        });

        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currnetPlayPosition == mDatas.size()-1) {
                    //已经是第一首
                    Toast.makeText(getActivity(), "已经是最后一首音乐了啊TNT", Toast.LENGTH_SHORT).show();
                }else {
                    currnetPlayPosition = currnetPlayPosition + 1;
                    musicBean = mDatas.get(currnetPlayPosition);
                    preparedMediaPlayer();
                    mediaPlayerSong.setImageResource(R.drawable.ic_baseline_pause_24);
                    visualizerManager.release();
                    visualizerManager = new NierVisualizerManager();
                    updateMusicUI(musicBean.getPath(),musicBean.getSong(),musicBean.getDuration(),musicBean.getSinger(),mediaPlayerHelper.getAudioSessionId());
                }
            }
        });
    }

    //MediaPlayer加载，准备播放
    private void preparedMediaPlayer() {
        mediaPlayerHelper.setPath(musicBean.getPath());
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
    }
}


