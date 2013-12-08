package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import com.xtr3d.extrememotion.api.Joint;

class DemoView extends View {

	private SkeletonDrawer mSkeletonDrawer;
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	private Mat matRgb;
	private Bitmap mRgb = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
	private boolean isFirstCall = true;

	public View mPreviewView;
	public ExtremeMotionUtils emUtils;

	public DemoView(Context context) {
		super(context);
		mSkeletonDrawer = new SkeletonDrawer(HEIGHT, WIDTH);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		FrameInfo frameInfo = emUtils.getLatestFrameInfo();
		if (frameInfo == null)
			return;
		// draw rgb

		if (frameInfo.getRgbImage() != null) {
			if (isFirstCall) {
				mPreviewView.setVisibility(View.INVISIBLE);
				isFirstCall = false;
			}
			if (matRgb == null) {
				matRgb = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
			}
			matRgb.put(0, 0, frameInfo.getRgbImage());
			Utils.matToBitmap(matRgb, mRgb);
			canvas.drawBitmap(mRgb, 0, 0, null);
		}

		if (frameInfo.getSkeleton() == null)
			return;
		List<Joint> joints = frameInfo.getSkeleton().getJoints();
		if (null != joints && !joints.isEmpty()) {
			mSkeletonDrawer.drawSkeleton(canvas, joints);
		}
	}
}