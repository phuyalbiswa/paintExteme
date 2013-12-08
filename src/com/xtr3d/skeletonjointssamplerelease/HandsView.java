package com.xtr3d.skeletonjointssamplerelease;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.xtr3d.extrememotion.api.Joint;

public class HandsView extends View {
	
	public ExtremeMotionUtils emUtils;

	private SkeletonDrawer mSkeletonDrawer;
	private final int WIDTH = 640;
	private final int HEIGHT = 480;

	public HandsView(Context context) {
		super(context);
		mSkeletonDrawer = new SkeletonDrawer(HEIGHT, WIDTH);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		FrameInfo frameInfo = emUtils.getLatestFrameInfo();
		if (frameInfo == null)
			return;

		if (frameInfo.getSkeleton() == null)
			return;
		List<Joint> joints = frameInfo.getSkeleton().getJoints();
		if (null != joints && !joints.isEmpty()) {
			mSkeletonDrawer.drawHands(canvas, joints);
		}
	}
}