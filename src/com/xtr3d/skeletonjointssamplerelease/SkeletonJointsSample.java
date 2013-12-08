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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xtr3d.extrememotion.api.Joint;
import com.xtr3d.extrememotion.api.Skeleton.StateType;
import com.xtr3d.extrememotion.api.WarningType;
import com.xtr3d.skeletonjointssamplerelease.ExtremeMotionUtils.NewFrameReadyListener;

/**
 * A sample application to demonstrate XTR3D Android Skeleton API
 * 
 * @author tom
 * @author zvika
 * 
 */
public class SkeletonJointsSample extends Activity {
	

	/**
	 * This is responsible of obtaining camera-frames for processing. Including
	 * this in our view hierarchy is essential in allowing the camera stream
	 * processing.
	 */
	private View mPreviewView;

	/** Draws skeleton joints position using colored circles */
	private DemoView mDemoView;

	/** Used to display frame-rate on screen */
	private TextView mFPSTextView;

	/** Used to display tracking-status on screen */
	private TextView mTrackingStatusTextView;
	
	/** Used to display the warnings on screen */
	private TextView mWarningsTextView;

	/** Used to contain the calibration icon */
	private ImageView mCalibIcon;

	/** Used to contain the reset button */
	private Button mResetButton;
	private ImageView mDocumentMenu;

	/** Used to contain the above UI elements */

	private RelativeLayout mCameraLayout;
	/** Handles skeleton interaction */

	private final ExtremeMotionUtils emUtils = new ExtremeMotionUtils();

	private StateType mLastSkeletonState = StateType.INITIALIZING;
	
	//DemoView always renders the joint positions as circles on the Canvas.
	//It can also render the RGB received from the engine on the same Canvas, with 2 benefits:
	//The joint render time is better, and it will show the RGB&skeleton data of the same frame id. (i.e. sync the RGB images
	//with their respective joints.)
	//If you set DRAW_BITMAP_EXPLICITLY to true, the DemoView will render the RGB.
	//Otherwise, if it is set to false, another view (CameraPreview) will do the RGB rendering async with the skeleton data. 
	//This approach should be used only if your application does not use the Canvas, and
	//it is here, only as a reference for other applications which will not draw the skeleton, and therefore no syncing is needed. 
	private final boolean DRAW_BITMAP_EXPLICITLY = true;
	
	
	private final int WIDTH = 640;
	private final int HEIGHT = 480;
	private int mNumOfCoordinates = 134;
	private float mPoints[] = new float[mNumOfCoordinates];

	/** View where actual bitmap is drawn **/
	//private DrawHandler mDrawHandler;

	/** Call on every application resume **/
	@Override
	protected void onResume() {
		super.onResume();
		emUtils.onResume();
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mResetButton = (Button) findViewById(R.id.resetButton);
		mDocumentMenu = (ImageView) findViewById(R.id.menu);
		mCalibIcon = (ImageView) findViewById(R.id.calibIcon);
		mFPSTextView = (TextView) findViewById(R.id.FPSText);
		mTrackingStatusTextView = (TextView) findViewById(R.id.trackingText);
		mCameraLayout = (RelativeLayout) findViewById(R.id.cameraLayout);
		mWarningsTextView = (TextView) findViewById(R.id.warningsView);
		
		// Hog the entire screen and keep it on. Force landscape orientation.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// set Reset Button onClick behavior
		mResetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				emUtils.reset();
				mCalibIcon.setVisibility(View.INVISIBLE);
			}
		});
		
		mDocumentMenu.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//emUtils.reset();
				//mCalibIcon.setVisibility(View.INVISIBLE);
			}
		});
		//mDrawHandler = new DrawHandler(this);
		//mDrawHandler.setEmUtils(emUtils);
		
		mPreviewView= emUtils.onCreate(this,  new SkeletonListenerImpl());
		mCameraLayout.addView(mPreviewView);
		mDemoView = new DemoView(this);
		mCameraLayout.addView(mDemoView,android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		//mCameraLayout.addView(mDrawHandler.drawView, android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		mCalibIcon.bringToFront();
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
		private boolean isFirstLaunch = true;
		private Runnable mResetEngineTaskOnUserExit = new Runnable() {
			@Override
			public void run() {
				emUtils.reset();
			}
		};
		private float newX = 10000000;
		private float newY = 10000000;


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
			String stateText = state.toString();
			if(state == StateType.NOT_TRACKED){
				stateText = "NOT TRACKED";
			}
			mTrackingStatusTextView.setText(stateText);
			if (frameRateCalc.getTotalCount() % 5 == 0){
				mFPSTextView.setText(String.format("%.3f FPS\n" + frameRateCalc.getTotalCount() + " Frames", frameRateCalc.getLatestFrameRateOverTime()));
			}
			String warningsText = createWarningText(newFrameInfo.getWarningsList());
			mWarningsTextView.setText(warningsText);
			
			// Initializing -> Calibration : the user need to know he need to move to the calibration T-pose.
			if (mLastSkeletonState == StateType.INITIALIZING && state == StateType.CALIBRATING) {
				mCalibIcon.setVisibility(View.VISIBLE);
			}
			// Calibration -> Steady/Not-Tracked/Background : calibration completed or reset remove the calibration icon
			else if (mLastSkeletonState == StateType.CALIBRATING && state != StateType.CALIBRATING){ 
				mCalibIcon.setVisibility(View.INVISIBLE);
			}
			// the skeleton was lost, if the user will not come back fast enough(ENGINE_RESET_TIME_OUT seconds), we will call engine reset.
			else if (mLastSkeletonState == StateType.TRACKED && state == StateType.NOT_TRACKED){ 
				mHandler.postDelayed(mResetEngineTaskOnUserExit, ENGINE_RESET_TIME_OUT);
			}
			else if (mLastSkeletonState== StateType.NOT_TRACKED && state == StateType.TRACKED){
				mHandler.removeCallbacks(mResetEngineTaskOnUserExit);
			}
			else if (state == StateType.TRACKED) {
				//mResetButton2.setVisibility(View.VISIBLE);
				
				FrameInfo frameInfo = emUtils.getLatestFrameInfo();
				if (frameInfo == null)
					return;
				List<Joint> joints = frameInfo.getSkeleton().getJoints();
				if (null != joints && !joints.isEmpty()) {
					enableJestureChecker(joints);
					
				}
			}
			
			mLastSkeletonState = state;
		}
		
		public void enableJestureChecker(List<Joint> mJoints) {
			for (Joint joint : mJoints) 
			{
//				float x = (joint.getPoint().getImgCoordNormHorizontal() * (float)mWidth);
//				float y = (joint.getPoint().getImgCoordNormVertical() * (float)mHeight);
				float x = (joint.getPoint().getImgCoordNormHorizontal() * (float)WIDTH);
				float y = (joint.getPoint().getImgCoordNormVertical() * (float)HEIGHT);
				float xx = (joint.getPoint().getX());
				float yy = (joint.getPoint().getY());
				
				int r = 0, g = 0, b = 0;					
				// select a high-contrast color scheme for the currently available joints
				switch (joint.getJointType()) {
				case HandLeft:
					r = 118;
					g = 42;
					b = 131;
					//mPoints[0] = x;
					//mPoints[1] = y;
					break;
				case ElbowLeft:
					r = 175;
					g = 141;
					b = 195;
					//mPoints[2] = x;
					//mPoints[3] = y;
					
//					mPoints[4] = x;
//					mPoints[5] = y;
					break;
				case ShoulderLeft:
					r = 0;
					g = 0;
					b = 255;
					mPoints[6] = x;
					mPoints[7] = y;
					mPoints[132] = xx;
					mPoints[133] = yy;
//					
//					mPoints[16] = x;
//					mPoints[17] = y;
					break;
				case Head:
					r = 255;
					g = 0;
					b = 0;
//					mPoints[24] = x;
//					mPoints[25] = y;
					break;
				case ShoulderRight:
					r = 255;
					g = 255;
					b = 0;
//					mPoints[14] = x;
//					mPoints[15] = y;
//					
//					mPoints[20] = x;
//					mPoints[21] = y;
					break;
				case ElbowRight:
					r = 127;
					g = 191;
					b = 123;
//					mPoints[10] = x;
//					mPoints[11] = y;
//					
//					mPoints[12] = x;
//					mPoints[13] = y;
					break;
				case HandRight:
					r = 27;
					g = 120;
					b = 55;
//					mPoints[8] = x;
//					mPoints[9] = y;
					break;
				case ShoulderCenter:
					r = 100;
					g = 100;
					b = 100;
//					mPoints[18] = x;
//					mPoints[19] = y;
//					
//					mPoints[22] = x;
//					mPoints[23] = y;
//					
//					mPoints[26] = x;
//					mPoints[27] = y;
//					
//					mPoints[28] = x;
//					mPoints[29] = y;
//					
//					shoulderCenterX = x;
//					shoulderCenterY = y;
					break;
				case HipCenter:							
//					mPoints[30] = x;
//					mPoints[31] = y;
//					
//					hipCenterX = x;
//					hipCenterY = y;
					continue; //don't draw this joint, it is recalculated later on and placed farther up in the skeleton				
				default:
					break;
				}
//				mPaint.setARGB(255, r, g, b);
//				canvas.drawCircle(x, y, 15, mPaint);
				float newPaintingHandX = mPoints[132];
				float newPaintingHandY = mPoints[133];
				Log.e("TAG", "X: "+newPaintingHandX +" y: "+newPaintingHandY //+ "newHipCenterX: "+newHipCenterX + " newHipCenterX: " +newHipCenterX
						);
				
				if(newPaintingHandX < 0.01 && newPaintingHandY < 0.01){
					Log.e("TAG", "-------x,y");
				}
				if (newX-x > 5 && newY-y > 5 && newY-y < 10000 ) {
					Log.e("TAG", "-----------inside <<<<<<<<<<");
					mDocumentMenu.setVisibility(View.VISIBLE);
					mDocumentMenu.setAlpha(127);
				}
				newX = x;
				newY = y;
				if (isFirstLaunch) {
					try {
						isFirstLaunch = false;
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
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
			
			if (frameInfo.getRgbImage() != null) {
				if (DRAW_BITMAP_EXPLICITLY) {
					if (isFirstCall){
						mPreviewView.setVisibility(View.INVISIBLE);
						isFirstCall = false;
					}
					if (matRgb == null){ 
						matRgb = new Mat(HEIGHT, WIDTH, CvType.CV_8UC3);
					}
					matRgb.put(0, 0, frameInfo.getRgbImage());
					Utils.matToBitmap(matRgb, mRgb);
					canvas.drawBitmap(mRgb, 0, 0, null);
				}
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
