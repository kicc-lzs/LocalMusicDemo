package org.hiucung.localmusicdemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class ImageUtil {

    /**
     * byte数组转换成位图
     * @param bitmap
     * @return
     */
    public static Bitmap Bytes2Bimap(byte[] bitmap) {
        if (bitmap.length != 0) {
            return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
        } else {
            return null;
        }
    }

    /**
     * 缩放截取矩形
     */
    public static Bitmap ClipSquareBitmap(Bitmap bmp, int width, int radius) {
        if (bmp == null || width <= 0)
            return null;
        //如果图片比较小就没必要进行缩放了

        /**
         * 把图片进行缩放，以宽高最小的一边为准，缩放图片比例
         * */
        if(bmp.getWidth()>width&&bmp.getHeight()>width) {
            if (bmp.getWidth() > bmp.getHeight()) {
                bmp = Bitmap.createScaledBitmap(bmp, (int) (((float) width) * bmp.getWidth() / bmp.getHeight()), width, false);
            } else {
                bmp = Bitmap.createScaledBitmap(bmp, width, (int) (((float) width) * bmp.getHeight() / bmp.getWidth()), false);
            }

        }else{
            width=bmp.getWidth() > bmp.getHeight()? bmp.getHeight():bmp.getWidth();
            //Ulog.d("宽" + width + ",w" + bmp.getWidth() + ",h" + bmp.getHeight());
            if(radius>width){
                radius=width;
            }
        }
        Bitmap output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        //设置画笔全透明
        canvas.drawARGB(0, 0, 0, 0);
        Paint paints = new Paint();
        paints.setColor(Color.WHITE);
        paints.setAntiAlias(true);//去锯齿
        paints.setFilterBitmap(true);
        //防抖动
        paints.setDither(true);

        //把图片圆形绘制矩形
        if(radius<=0)
            canvas.drawRect(new Rect(0,0, width, width),paints);
        else //绘制圆角
            canvas.drawRoundRect(new RectF(0, 0, width, width), radius, radius, paints);
        // 取两层绘制交集。显示前景色。
        paints.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Rect rect = new Rect();
        if (bmp.getWidth() >= bmp.getHeight()) {
            rect.set((bmp.getWidth() - width) / 2, 0, (bmp.getWidth() + width) / 2, width);
        } else {
            rect.set(0,(bmp.getHeight() - width) / 2,  width ,(bmp.getHeight() + width) / 2);
        }
        Rect rect2 = new Rect(0, 0, width, width);
        //第一个rect 针对bmp的绘制区域，rect2表示绘制到上面位置
        canvas.drawBitmap(bmp, rect, rect2, paints);
        bmp.recycle();
        return output;
    }
}
