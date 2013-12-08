package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.xtr3d.extrememotion.api.Joint;

public class SkeletonDrawer {
	
	private int mHeight;
	private int mWidth;
	private Paint mPaint;
	
	//0-3: left hand to left elbow
	//4-7: left elbow to left shoulder
	//8-11: right hand to right elbow
	//12-15: right elbow to right shoulder
	//16-19: left shoulder to center
	//20-23: right shoulder to center
	//24-27: head to shoulder center
	//28-31: shoulder center to hip center
	
	public static int brushColor = Color.RED;

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
		//canvas.drawCircle(handLeftX, handLeftY, 15, mPaint);
		//canvas.drawCircle(handRightX, handRightY, 15, mPaint);
		
		// Test
		//PaintExtreme.mDebugText.setText(Float.toString(handLeftZ));
		//PaintExtreme.ViewHandler.mCanvasView.drawCircle(handLeftX, handLeftY, 25, );
		PaintExtreme.ViewHandler.mCanvasView.drawCircle(handRightX * 2, handRightY * 2, 25, brushColor);
	}
}
