package com.xtr3d.skeletonjointssamplerelease;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

public class CanvasView extends View {
	
	public ExtremeMotionUtils emUtils;

	private final int WIDTH = 1280;
	private final int HEIGHT = 960;
	Bitmap mBitmap;
	Paint mBitmapPaint;
	Canvas mCanvas;
	
	Paint mCirclePaint;
	
	Context mApplicationContext;

	public CanvasView(Context context) {
		super(context);
		
		mApplicationContext = context;
		
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
		mCanvas.drawColor(Color.TRANSPARENT);
		mCirclePaint.setColor(color);
		mCanvas.drawCircle(x, y, radius, mCirclePaint);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//Rect src = new Rect(0, 0, mBitmap.getWidth() - 1, mBitmap.getHeight() - 1);
		//Rect dest = new Rect(0, 0, 1280 - 1, 960 - 1);
		
		//canvas.drawBitmap(mBitmap, src, dest, mBitmapPaint);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
	}
	
	public void saveImage() {
		Bitmap bitmap = mBitmap;
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date now = new Date();
		String fileName = "realpainting_" + formatter.format(now) + ".jpg";
		
		File f = new File(Environment.getExternalStorageDirectory()
		                        + File.separator + fileName);
		try {
		   f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		} catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		}
		
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    mApplicationContext.sendBroadcast(mediaScanIntent);
	}
}