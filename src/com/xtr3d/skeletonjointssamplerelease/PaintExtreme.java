package com.xtr3d.skeletonjointssamplerelease;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xtr3d.extrememotion.api.Joint;
import com.xtr3d.extrememotion.api.Skeleton.StateType;
import com.xtr3d.skeletonjointssamplerelease.ExtremeMotionUtils.NewFrameReadyListener;

public class PaintExtreme extends Activity {

	private View mPreviewView;

	public enum ViewHandler {
		INSTANCE;
		public static CameraView mCameraView;
		public static HandsView mHandsView;
		public static CanvasView mCanvasView;
	};
	
	public static MenuHandler mMenuHandler;
	
	public static ImageView menuImageTop;
	public static ImageView menuImageLeft;
	public static ImageView menuImageRight;
	public static ImageView menuImageBottom;

	private FrameLayout mCameraLayout;

	private final ExtremeMotionUtils emUtils = new ExtremeMotionUtils();

	private StateType mLastSkeletonState = StateType.INITIALIZING;
	
	Queue<Float> handLeftOldX = new LinkedList<Float>();
	Queue<Float> handLeftOldY = new LinkedList<Float>();
	Queue<Float> handRightOldX = new LinkedList<Float>();
	Queue<Float> handRightOldY = new LinkedList<Float>();

	@Override
	protected void onResume() {
		super.onResume();
		emUtils.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		
		menuImageTop = (ImageView) findViewById(R.id.top);
		menuImageLeft = (ImageView) findViewById(R.id.left);
		menuImageRight = (ImageView) findViewById(R.id.right);
		menuImageBottom = (ImageView) findViewById(R.id.bottom);

		mCameraLayout = (FrameLayout) findViewById(R.id.cameraLayout);

		// Hog the entire screen and keep it on. Force landscape orientation.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mPreviewView = emUtils.onCreate(this, new SkeletonListenerImpl());
		mCameraLayout.addView(mPreviewView);

		ViewHandler.mCameraView = new CameraView(this);
		ViewHandler.mCameraView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mCameraView,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);

		ViewHandler.mHandsView = new HandsView(this);
		ViewHandler.mHandsView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mHandsView,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);

		ViewHandler.mCanvasView = new CanvasView(this);
		ViewHandler.mCanvasView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mCanvasView,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		
		findViewById(R.id.top).bringToFront();
		findViewById(R.id.bottom).bringToFront();
		
		mMenuHandler = new MenuHandler();
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
		private Runnable mResetEngineTaskOnUserExit = new Runnable() {
			@Override
			public void run() {
				emUtils.reset();
			}
		};

		@Override
		public void onNewFrameReady(final FrameInfo newFrameInfo) {

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateAppViews(newFrameInfo);
				}
			});

			ViewHandler.mCameraView.postInvalidate();
			ViewHandler.mHandsView.postInvalidate();
			ViewHandler.mCanvasView.postInvalidate();
		}

		private void updateAppViews(FrameInfo newFrameInfo) {

			final StateType state = newFrameInfo.getSkeleton().getState();

			if (state == StateType.TRACKED) {
				// mResetButton2.setVisibility(View.VISIBLE);

				FrameInfo frameInfo = emUtils.getLatestFrameInfo();
				if (frameInfo == null)
					return;
				List<Joint> joints = frameInfo.getSkeleton().getJoints();
				if (null != joints && !joints.isEmpty()) {
					enableJestureChecker(joints);
				}
			}

			// the skeleton was lost, if the user will not come back fast
			// enough(ENGINE_RESET_TIME_OUT seconds), we will call engine reset.
			if (mLastSkeletonState == StateType.TRACKED
					&& state == StateType.NOT_TRACKED) {
				mHandler.postDelayed(mResetEngineTaskOnUserExit,
						ENGINE_RESET_TIME_OUT);
			} else if (mLastSkeletonState == StateType.NOT_TRACKED
					&& state == StateType.TRACKED) {
				mHandler.removeCallbacks(mResetEngineTaskOnUserExit);
			}

			mLastSkeletonState = state;
		}

		public void enableJestureChecker(List<Joint> mJoints) {
			float handLeftX = 0;
			float handLeftY = 0;
			float handRightX = 0;
			float handRightY = 0;

			for (Joint joint : mJoints) {
				float x = (joint.getPoint().getImgCoordNormHorizontal() * (float) 640);
				float y = (joint.getPoint().getImgCoordNormVertical() * (float) 480);
				
				switch (joint.getJointType()) {
				case HandLeft:
					handLeftX = x;
					handLeftY = y;
					break;
				case HandRight:
					handRightX = x;
					handRightY = y;
					break;
				default:
					break;
				}
			}
			
			handLeftOldX.add(handLeftX);
			handLeftOldY.add(handLeftY);
			handRightOldX.add(handRightX);
			handRightOldY.add(handRightY);
			
			final float SAMPLES = 5;
			
			if(handLeftOldX.size() > SAMPLES){handLeftOldX.poll();};
			if(handLeftOldY.size() > SAMPLES){handLeftOldY.poll();};
			if(handRightOldX.size() > SAMPLES){handRightOldX.poll();};
			if(handRightOldY.size() > SAMPLES){handRightOldY.poll();};
			
			// Get average motion of hands
			float avgLeftX = 0, avgLeftY = 0, avgRightX = 0, avgRightY = 0;
			for (float leftX : handLeftOldX) {
				avgLeftX += leftX;
			}
			for (float leftY : handLeftOldY) {
				avgLeftY += leftY;
			}
			for (float rightX : handRightOldX) {
				avgRightX += rightX;
			}
			for (float rightY : handRightOldY) {
				avgRightY += rightY;
			}
			avgLeftX /= SAMPLES;
			avgLeftY /= SAMPLES;
			avgRightX /= SAMPLES;
			avgRightY /= SAMPLES;
			
			float varLeftX = 0, varLeftY = 0, varRightX = 0, varRightY = 0;
			// Get standard error
			for (float leftX : handLeftOldX) {
				varLeftX += Math.abs(avgLeftX - leftX);
			}
			for (float leftY : handLeftOldY) {
				varLeftY += Math.abs(avgLeftY - leftY);
			}
			for (float rightX : handRightOldX) {
				varRightX += Math.abs(avgRightX - rightX);
			}
			for (float rightY : handRightOldY) {
				varRightY += Math.abs(avgRightY - rightY);
			}
			Log.e("TAG", "---------- varRightY: " + varRightY + " varRightX:"+varRightX + " varLeftY:"+varLeftY+" varLeftX:"+ varLeftX);
			
			// Check standard error
			//if(varLeftX <= 200 && varLeftY <= 200 && varRightX <= 200 && varRightY <= 200)
			{
				mMenuHandler.processActions(handLeftX, handLeftY, handRightX, handRightY);
			}
		}
	}
}
