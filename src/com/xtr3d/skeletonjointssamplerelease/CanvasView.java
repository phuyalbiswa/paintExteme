package com.xtr3d.skeletonjointssamplerelease;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CanvasView extends View {
	
	public ExtremeMotionUtils emUtils;

	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	Bitmap mBitmap;
	Paint mBitmapPaint;
	Canvas mCanvas;
	
	Paint mCirclePaint;

	public CanvasView(Context context) {
		super(context);
		
		mBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mCirclePaint = new Paint();
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Paint.Style.FILL);
		mCirclePaint.setStrokeJoin(Paint.Join.MITER);
		mCirclePaint.setStrokeWidth(4f);
	}
	
	public void drawCircle(float x, float y, int radius, int color)
	{
		mCirclePaint.setColor(color);
		mCanvas.drawCircle(x, y, radius, mCirclePaint);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
	}
}