package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

	private RelativeLayout mCameraLayout;

	private final ExtremeMotionUtils emUtils = new ExtremeMotionUtils();

	private StateType mLastSkeletonState = StateType.INITIALIZING;
	
	// Test
	public static TextView mDebugText;

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
		
		ViewHandler.mCameraView = new CameraView(this);
		ViewHandler.mCameraView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mCameraView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		
		ViewHandler.mHandsView = new HandsView(this);
		ViewHandler.mHandsView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mHandsView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		
		ViewHandler.mCanvasView = new CanvasView(this);
		ViewHandler.mCanvasView.emUtils = emUtils;
		mCameraLayout.addView(ViewHandler.mCanvasView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
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
		
		private float newX = 10000000;
		private float newY = 10000000;
		
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
        			//mResetButton2.setVisibility(View.VISIBLE);
        			
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
			
			float newPaintingHandX = 0;
			float newPaintingHandY = 0;
			
			for (Joint joint : mJoints) 
			{
				float x = (joint.getPoint().getImgCoordNormHorizontal() * (float)640);
				float y = (joint.getPoint().getImgCoordNormVertical() * (float)480);
				float xx = (joint.getPoint().getX());
				float yy = (joint.getPoint().getY());
				
				int r = 0, g = 0, b = 0;					
				// select a high-contrast color scheme for the currently available joints
				switch (joint.getJointType()) {
				case ShoulderLeft:
					newPaintingHandX = xx;
					newPaintingHandY = yy;
					break;			
				default:
					break;
				}
				
				Log.e("TAG", "X: "+newPaintingHandX +" y: "+newPaintingHandY //+ "newHipCenterX: "+newHipCenterX + " newHipCenterX: " +newHipCenterX
						);
				
				if(newPaintingHandX < 0.01 && newPaintingHandY < 0.01){
					Log.e("TAG", "x,y");
				}
				if (newX-x > 5 && newY-y > 5 && newY-y < 10000 ) {
					Log.e("TAG", "inside");
				}
				newX = x;
				newY = y;
			}
		}
	}
}
