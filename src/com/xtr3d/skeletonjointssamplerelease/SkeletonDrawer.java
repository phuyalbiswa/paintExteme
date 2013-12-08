package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.xtr3d.extrememotion.api.Joint;

public class SkeletonDrawer {
	
	private int mHeight;
	private int mWidth;
	private Paint mPaint;

	public SkeletonDrawer(int height, int width){
		mHeight = height;
		mWidth = width;
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
	}

	public void drawHands(Canvas canvas, List<Joint> mJoints){
		float handLeftX = 0;
		float handLeftY = 0;
		float handRightX = 0;
		float handRightY = 0;
		for (Joint joint : mJoints) 
		{
			float x = (joint.getPoint().getImgCoordNormHorizontal() * (float)mWidth);
			float y = (joint.getPoint().getImgCoordNormVertical() * (float)mHeight);
			
			// select a high-contrast color scheme for the currently available joints
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
		mPaint.setARGB(255, 118, 42, 131);
		canvas.drawCircle(handLeftX, handLeftY, 15, mPaint);
		canvas.drawCircle(handRightX, handRightY, 15, mPaint);
	}
}