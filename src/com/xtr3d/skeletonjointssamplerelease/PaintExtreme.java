package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xtr3d.extrememotion.api.Joint;
import com.xtr3d.extrememotion.api.Skeleton.StateType;
import com.xtr3d.skeletonjointssamplerelease.ExtremeMotionUtils.NewFrameReadyListener;

public class PaintExtreme extends Activity {

	private View mPreviewView;
	private DemoView mDemoView;

	private RelativeLayout mCameraLayout;

	private final ExtremeMotionUtils emUtils = new ExtremeMotionUtils();

	private StateType mLastSkeletonState = StateType.INITIALIZING;

	@Override
	protected void onResume() {
		super.onResume();
		emUtils.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mCameraLayout = (RelativeLayout) findViewById(R.id.cameraLayout);

		// Hog the entire screen and keep it on. Force landscape orientation.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mPreviewView = emUtils.onCreate(this, new SkeletonListenerImpl());
		mCameraLayout.addView(mPreviewView);
		mDemoView = new DemoView(this);
		mCameraLayout.addView(mDemoView,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		emUtils.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		emUtils.onStop();
	}

	private class SkeletonListenerImpl implements NewFrameReadyListener {

		private final long ENGINE_RESET_TIME_OUT = 4000;
		private Handler mHandler = new Handler();
		private FrameRateCalculator frameRateCalc = new FrameRateCalculator();
		private Runnable mResetEngineTaskOnUserExit = new Runnable() {
			@Override
			public void run() {
				emUtils.reset();
			}
		};

		@Override
		public void onNewFrameReady(final FrameInfo newFrameInfo) {

			frameRateCalc.sample();

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateAppViews(newFrameInfo);
				}
			});

			mDemoView.postInvalidate();

		}

		private void updateAppViews(FrameInfo newFrameInfo) {

			final StateType state = newFrameInfo.getSkeleton().getState();

			// Initializing -> Calibration : the user need to know he need to
			// move to the calibration T-pose.
			if (mLastSkeletonState == StateType.INITIALIZING
					&& state == StateType.CALIBRATING) {

			}
			// Calibration -> Steady/Not-Tracked/Background : calibration
			// completed or reset remove the calibration icon
			else if (mLastSkeletonState == StateType.CALIBRATING
					&& state != StateType.CALIBRATING) {

			}
			// the skeleton was lost, if the user will not come back fast
			// enough(ENGINE_RESET_TIME_OUT seconds), we will call engine reset.
			else if (mLastSkeletonState == StateType.TRACKED
					&& state == StateType.NOT_TRACKED) {
				mHandler.postDelayed(mResetEngineTaskOnUserExit,
						ENGINE_RESET_TIME_OUT);
			} else if (mLastSkeletonState == StateType.NOT_TRACKED
					&& state == StateType.TRACKED) {
				mHandler.removeCallbacks(mResetEngineTaskOnUserExit);
			}

			mLastSkeletonState = state;
		}
	}

	private class DemoView extends View {

		private SkeletonDrawer mSkeletonDrawer;
		private final int WIDTH = 640;
		private final int HEIGHT = 480;
		private Mat matRgb;
		private Bitmap mRgb = Bitmap.createBitmap(WIDTH, HEIGHT,
				Bitmap.Config.RGB_565);
		private boolean isFirstCall = true;

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
}
