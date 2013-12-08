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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xtr3d.extrememotion.api.Joint;
import com.xtr3d.extrememotion.api.WarningType;
import com.xtr3d.extrememotion.api.Skeleton.StateType;
import com.xtr3d.skeletonjointssamplerelease.ExtremeMotionUtils.NewFrameReadyListener;

public class PaintExtreme extends Activity {

	private View mPreviewView;
	
	private ImageView mDocumentMenu;
	private ImageView brushes;
	private ImageView fileOption;
	private ImageView selectColors;
	
	
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
		mDocumentMenu = (ImageView) findViewById(R.id.menu);
		brushes = (ImageView) findViewById(R.id.brushes);
		fileOption = (ImageView) findViewById(R.id.file_option);
		selectColors = (ImageView) findViewById(R.id.select_colors);

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
		
		mDocumentMenu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//emUtils.reset();
				//mCalibIcon.setVisibility(View.INVISIBLE);
			}
		});
		//mDrawHandler = new DrawHandler(this);
		//mDrawHandler.setEmUtils(emUtils);
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
		private boolean isFirstLaunch = true;
		
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
                //Biswa Test codes start
                else if (state == StateType.TRACKED) {
    				//mResetButton2.setVisibility(View.VISIBLE);
    				
    				FrameInfo frameInfo = emUtils.getLatestFrameInfo();
    				if (frameInfo == null)
    					return;
    				List<Joint> joints = frameInfo.getSkeleton().getJoints();
    				if (null != joints && !joints.isEmpty()) {
    					enableJestureChecker(joints);
    					
    				}
    			} //test code ends

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
				
//				mPaint.setARGB(255, r, g, b);
//				canvas.drawCircle(x, y, 15, mPaint);
				Log.e("TAG", "X: "+newPaintingHandX +" y: "+newPaintingHandY //+ "newHipCenterX: "+newHipCenterX + " newHipCenterX: " +newHipCenterX
						);
				int i = 0;
				i++;
				if (i > 100) {
					i--;

					if (newPaintingHandY > 0.01) {
						// Top icon
						// Need to check for x co-ordinate, -ve or +ve for more
						// accuracy
						getSubMenuDrawables();

					} else if (newPaintingHandY < -0.5) {
						// Bottom icon
					} else if (newPaintingHandX > 0.5) {
						// Right icon
					} else if (newPaintingHandX < -0.5) {
						// Left Icon
					}
				}
				
				
				
				
				//These below stuffs are for testing only,
				if(newPaintingHandX < 0.01 && newPaintingHandY < 0.01){
					Log.e("TAG", "-------x,y");
				}
				if (newX-x > 5 && newY-y > 5 && newY-y < 10000 ) {
					Log.e("TAG", "-----------inside <<<<<<<<<<");
//					mDocumentMenu.setVisibility(View.VISIBLE);
//					mDocumentMenu.setAlpha(127);
				}
				newX = x;
				newY = y;
				if (isFirstLaunch) {
					try {
						isFirstLaunch = false;
						Thread.sleep(1000);
						//mDocumentMenu.setImageResource(R.drawable.brushes);
						getViewAsVisible();
						//getSubOptionsDrawables();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
		private void getViewAsVisible() {
			mDocumentMenu.setVisibility(View.VISIBLE);
			mDocumentMenu.setAlpha(127);
			
			brushes.setVisibility(View.VISIBLE);
			brushes.setAlpha(127);
			
			fileOption.setVisibility(View.VISIBLE);
			fileOption.setAlpha(127);
			
			selectColors.setVisibility(View.VISIBLE);
			selectColors.setAlpha(127);

		}
		
		private void getViewAsGone() {
			mDocumentMenu.setVisibility(View.GONE);
			brushes.setVisibility(View.GONE);
			fileOption.setVisibility(View.GONE);
			selectColors.setVisibility(View.GONE);

		}
		
		private void getSubMenuDrawables() {
			mDocumentMenu.setImageResource(R.drawable.save);
			brushes.setImageResource(R.drawable.open);
			fileOption.setImageResource(R.drawable.back);
			selectColors.setImageResource(R.drawable.delete);
		}
		
		private void getSubOptionsDrawables() {
			mDocumentMenu.setImageResource(R.drawable.twitter);
			brushes.setImageResource(R.drawable.camera);
			fileOption.setImageResource(R.drawable.back);
			//selectColors.setImageResource(R.drawable.p);
		}
		
		private void getSubBrushesDrawables() {
			//TODO:drawables
			mDocumentMenu.setImageResource(R.drawable.twitter);
			brushes.setImageResource(R.drawable.camera);
			fileOption.setImageResource(R.drawable.back);
			//selectColors.setImageResource(R.drawable.p);
		}
		
		private void getSubColorsDrawables() {
			//TODO: get actual drawables
			mDocumentMenu.setImageResource(R.drawable.twitter);
			brushes.setImageResource(R.drawable.camera);
			fileOption.setImageResource(R.drawable.back);
			//selectColors.setImageResource(R.drawable.p);
		}

		private String createWarningText(List<WarningType> warningsList) { 
			String warningsText = "";
			if(warningsList != null){		
				warningsText = "Warnings:\n";
				for(WarningType warning : warningsList){
					switch(warning){
						case SKELETON_FRAME_EDGE_CLIPPED_FAR:
							warningsText += "Too Far From Camera" +  "\n";
							break;
						case SKELETON_FRAME_EDGE_CLIPPED_NEAR:
							warningsText += "Too Close To Camera" +  "\n";
							break;
						case SKELETON_FRAME_EDGE_CLIPPED_LEFT:
							warningsText += "Too Far Left" +  "\n";
							break;
						case SKELETON_FRAME_EDGE_CLIPPED_RIGHT:
							warningsText += "Too Far Right" +  "\n";
							break;
						case RAW_IMAGE_LIGHT_LOW:
							warningsText += "Low Lighting" +  "\n";
							break;
						case RAW_IMAGE_STRONG_BACKLIGHTING:
							warningsText += "Strong Back Lighting" +  "\n";
							break;
						case RAW_IMAGE_TOO_MANY_PEOPLE:
							warningsText += "Too Many People" +  "\n";
							break;
						default:
							warningsText += warning.toString() +  "\n";
							break;	
					}
				}
			}
			return warningsText;
		}
	}

	private class DemoView extends View {

		private SkeletonDrawer mSkeletonDrawer;
		private final int WIDTH = 640;
		private final int HEIGHT = 480;
		private Mat matRgb;
		private Bitmap mRgb = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.RGB_565);
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
			
			if (isFirstCall){
				mPreviewView.setVisibility(View.INVISIBLE);
				isFirstCall = false;
			}
			
			if (frameInfo.getSkeleton() == null)
				return;
			List<Joint> joints = frameInfo.getSkeleton().getJoints();
			if (null != joints && !joints.isEmpty()) {
				//mSkeletonDrawer.drawSkeleton(canvas, joints);
			}
			
		}

	}
}
