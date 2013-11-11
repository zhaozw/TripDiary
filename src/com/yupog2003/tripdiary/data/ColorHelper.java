package com.yupog2003.tripdiary.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ColorHelper {
	public static Drawable getColorDrawable(Context context,int size,int color){
		size=(int)pxFromDp(context, size);
		Bitmap bitmap=Bitmap.createBitmap(size,size, Bitmap.Config.ARGB_8888);
		Canvas canvas=new Canvas(bitmap);
		Paint paint=new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setMaskFilter(new EmbossMaskFilter(new float[]{1,1,1}, 0.4f, 2, 3.5f));
		canvas.drawCircle(size/2, size/2, (int)(size*0.4), paint);
		return new BitmapDrawable(context.getResources(), bitmap);
	}
	public static float dpFromPx(Context c,float px){
	   return px / c.getResources().getDisplayMetrics().density;
	}


	public static float pxFromDp(Context c,float dp){
	   return dp * c.getResources().getDisplayMetrics().density;
	}
}
