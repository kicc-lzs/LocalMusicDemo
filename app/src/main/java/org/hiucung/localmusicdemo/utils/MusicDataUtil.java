package org.hiucung.localmusicdemo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;


import org.hiucung.localmusicdemo.LocalMusicBean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 加载本地📛mp3数据
 */
public class MusicDataUtil {

    public static ArrayList<LocalMusicBean> getMusicData(Context context) {

        ArrayList<LocalMusicBean> mDatas = new ArrayList<>();

        //获取contentResolver
        ContentResolver contentResolver = context.getContentResolver();

        //获取本地音乐的uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //开始查询地址
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        //遍历cursor
        int id = 0;
        while (cursor.moveToNext()) {
            Long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            if (duration>5000){//时长太小的mp3不是音乐
                String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                id++;
                String sid = String.valueOf(id);
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                String time = sdf.format(new Date(duration));
                //将数据封装到对象里面
                LocalMusicBean bean = new LocalMusicBean(sid, song, singer, album, time, path);
                mDatas.add(bean);
            }

        }
        cursor.close();
        return mDatas;
    }
}
