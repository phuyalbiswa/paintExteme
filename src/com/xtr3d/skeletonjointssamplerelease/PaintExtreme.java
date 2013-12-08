package com.xtr3d.skeletonjointssamplerelease;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.xtr3d.extrememotion.api.Skeleton.StateType;
import com.xtr3d.skeletonjointssamplerelease.ExtremeMotionUtils.NewFrameReadyListener;

public class PaintExtreme extends Activity {

	private View mPreviewView;
	private CameraView mCameraView;
	private HandsView mHandsView;

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
		
		mCameraView = new CameraView(this);
		mCameraView.emUtils = emUtils;
		mCameraLayout.addView(mCameraView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		
		mHandsView = new HandsView(this);
		mHandsView.emUtils = emUtils;
		mCameraLayout.addView(mHandsView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		
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

			mCameraView.postInvalidate();
			mHandsView.postInvalidate();
		}

		private void updateAppViews(FrameInfo newFrameInfo) {

			final StateType state = newFrameInfo.getSkeleton().getState();

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
	}
}
