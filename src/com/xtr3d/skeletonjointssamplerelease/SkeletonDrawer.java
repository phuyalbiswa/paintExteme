package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.xtr3d.extrememotion.api.Joint;

public class SkeletonDrawer {
	
	private int mHeight;
	private int mWidth;
	private Paint mPaint;
	private int mNumOfCoordinates = 134;
	private float mPoints[] = new float[mNumOfCoordinates];
	
	//0-3: left hand to left elbow
	//4-7: left elbow to left shoulder
	//8-11: right hand to right elbow
	//12-15: right elbow to right shoulder
	//16-19: left shoulder to center
	//20-23: right shoulder to center
	//24-27: head to shoulder center
	//28-31: shoulder center to hip center

	public SkeletonDrawer(int height, int width){
		mHeight = height;
		mWidth = width;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
	}
	


	public void drawSkeleton(Canvas canvas, List<Joint> mJoints){
		float shoulderCenterX = 0, shoulderCenterY=0, hipCenterX = 0, hipCenterY = 0;
		for (Joint joint : mJoints) 
		{
			float x = (joint.getPoint().getImgCoordNormHorizontal() * (float)mWidth);
			float y = (joint.getPoint().getImgCoordNormVertical() * (float)mHeight);
			float xx = (joint.getPoint().getX());
			float yy = (joint.getPoint().getY());
			
			int r = 0, g = 0, b = 0;					
			// select a high-contrast color scheme for the currently available joints
			switch (joint.getJointType()) {
			case HandLeft:
				r = 118;
				g = 42;
				b = 131;
				mPoints[0] = x;
				mPoints[1] = y;
				break;
			case ElbowLeft:
				r = 175;
				g = 141;
				b = 195;
				mPoints[2] = x;
				mPoints[3] = y;
				
				mPoints[4] = x;
				mPoints[5] = y;
				break;
			case ShoulderLeft:
				r = 0;
				g = 0;
				b = 255;
				mPoints[6] = x;
				mPoints[7] = y;
				
				mPoints[16] = x;
				mPoints[17] = y;
				break;
			case Head:
				r = 255;
				g = 0;
				b = 0;
				mPoints[24] = x;
				mPoints[25] = y;
				break;
			case ShoulderRight:
				r = 255;
				g = 255;
				b = 0;
				mPoints[14] = x;
				mPoints[15] = y;
				
				mPoints[20] = x;
				mPoints[21] = y;
				break;
			case ElbowRight:
				r = 127;
				g = 191;
				b = 123;
				mPoints[10] = x;
				mPoints[11] = y;
				
				mPoints[12] = x;
				mPoints[13] = y;
				break;
			case HandRight:
				r = 27;
				g = 120;
				b = 55;
				mPoints[8] = x;
				mPoints[9] = y;
				mPoints[132] = xx;
				mPoints[133] = yy;
				break;
			case ShoulderCenter:
				r = 100;
				g = 100;
				b = 100;
				mPoints[18] = x;
				mPoints[19] = y;
				
				mPoints[22] = x;
				mPoints[23] = y;
				
				mPoints[26] = x;
				mPoints[27] = y;
				
				mPoints[28] = x;
				mPoints[29] = y;
				
				shoulderCenterX = x;
				shoulderCenterY = y;
				break;
			case HipCenter:							
				mPoints[30] = x;
				mPoints[31] = y;
				
				hipCenterX = x;
				hipCenterY = y;
				continue; //don't draw this joint, it is recalculated later on and placed farther up in the skeleton				
			default:
				break;
			}
			mPaint.setARGB(255, r, g, b);
			canvas.drawCircle(x, y, 15, mPaint);
		}
		//raise the hip point a little bit
		float newHipCenterX = (float)(hipCenterX + 0.4*(shoulderCenterX-hipCenterX));
		float newHipCenterY = (float)(hipCenterY + 0.4*(shoulderCenterY-hipCenterY));
		mPoints[30] = newHipCenterX;
		mPoints[31] = newHipCenterY;
		mPaint.setARGB(255, 255, 127, 36);
		canvas.drawCircle(newHipCenterX, newHipCenterY, 15, mPaint);
		boolean drawLines = true;
		for(float point : mPoints)
		{
			//if there are points that haven't been filled, don't draw the lines
			if(point == 0.0){
				drawLines = false;
				break;
			}
		}
		if(drawLines){
			mPaint.setARGB(255, 255, 0, 0);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(10);
			canvas.drawLines(mPoints, 0, mNumOfCoordinates, mPaint);
		}
		
//		float newPaintingHandX = mPoints[8];
//		float newPaintingHandY = mPoints[9];
//		Log.e("TAG", "X: "+newPaintingHandX +" y: "+newPaintingHandY + "newHipCenterX: "+newHipCenterX + " newHipCenterX: " +newHipCenterX);
		float newPaintingHandX = mPoints[132];
		float newPaintingHandY = mPoints[133];
		//Log.e("TAG", "X: "+newPaintingHandX +" y: "+newPaintingHandY + "newHipCenterX: "+newHipCenterX + " newHipCenterX: " +newHipCenterX);
		
	}
}
