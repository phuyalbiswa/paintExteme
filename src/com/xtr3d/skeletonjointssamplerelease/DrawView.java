package com.xtr3d.skeletonjointssamplerelease;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceView;

public class DrawView extends SurfaceView {
	public DrawHandler drawHandler;

	public DrawView(Context context) {
		super(context);
	}

	protected void onDraw(Canvas c) {
		super.onDraw(c);
		drawHandler.draw(c);
	}
}
