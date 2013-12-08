package com.xtr3d.skeletonjointssamplerelease;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class CameraView extends View {
	
	public ExtremeMotionUtils emUtils;

	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	private Mat matRgb;
	private Bitmap mRgb = Bitmap.createBitmap(WIDTH, HEIGHT,
			Bitmap.Config.ARGB_8888);

	public CameraView(Context context) {
		super(context);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		FrameInfo frameInfo = emUtils.getLatestFrameInfo();
		if (frameInfo == null)
			return;

		if (frameInfo.getRgbImage() != null) {
			if (matRgb == null) {
				matRgb = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
			}
			matRgb.put(0, 0, frameInfo.getRgbImage());
			Utils.matToBitmap(matRgb, mRgb);
			
			Rect src = new Rect(0, 0, mRgb.getWidth() - 1, mRgb.getHeight() - 1);
			Rect dest = new Rect(0, 0, 1280 - 1, 960 - 1);
			
			canvas.drawBitmap(mRgb, src, dest, null);
		}
	}
}