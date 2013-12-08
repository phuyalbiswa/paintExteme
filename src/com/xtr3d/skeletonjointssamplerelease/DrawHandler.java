package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.xtr3d.extrememotion.api.Joint;
import com.xtr3d.extrememotion.api.Joint.JointType;

public class DrawHandler {

	DrawView drawView;
	Paint mPaint;
	ExtremeMotionUtils emUtils;

	public DrawHandler(Context context) {
		drawView = new DrawView(context);
		drawView.drawHandler = this;

		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setAntiAlias(true);
	}

	public void setEmUtils(ExtremeMotionUtils emUtils) {
		this.emUtils = emUtils;
	}

	private int getViewWidth() {
		return drawView.getWidth();
	}

	private int getViewHeight() {
		return drawView.getHeight();
	}

	public void draw(Canvas c) {
		drawHands(c);
	}

	private void drawHands(Canvas c) {
		// Set paint color
		mPaint.setARGB(255, 255, 127, 36);

		List<Joint> joints = emUtils.getLatestFrameInfo().getSkeleton().getJoints();

		// Get left and right hands
		Joint handLeft = null, handRight = null;
		for (Joint joint : joints) {
			if (joint.getJointType() == JointType.HandLeft) {
				handLeft = joint;
			} else if (joint.getJointType() == JointType.HandRight) {
				handRight = joint;
			}
		}

		// Get left/right hand points
		float handLeftX = handLeft.getPoint().getImgCoordNormHorizontal()
				* getViewWidth();
		float handLeftY = handLeft.getPoint().getImgCoordNormVertical()
				* getViewHeight();

		float handRightX = handRight.getPoint().getImgCoordNormHorizontal()
				* getViewWidth();
		float handRightY = handRight.getPoint().getImgCoordNormVertical()
				* getViewHeight();

		// Draw left hand
		drawCircle(handLeftX, handLeftY, 15, c);
		drawCircle(handRightX, handRightY, 15, c);
	}

	private void drawCircle(float x, float y, int radius, Canvas c) {
		c.drawCircle(x, y, 15, mPaint);
	}
}
