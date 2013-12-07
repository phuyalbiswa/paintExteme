package com.xtr3d.skeletonjointssamplerelease;

import java.util.EnumSet;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.xtr3d.extrememotion.api.ExtremeMotionGenerator;
import com.xtr3d.extrememotion.api.StreamType;

/**
 * Utility class with two functions:
 * 1. Inits the AsyncFrameFetcher, which registers a listener (NewFrameReadyListener) to be waked-up 
 * each time a new rgb/skeleton data is found in the sdk.
 * 2. Life-cycle methods which should be called on the activity onCreate,onResume,onStop,onDestroy
 * 
 * @author assafl
 *
 */
public class ExtremeMotionUtils {

	private static final String TAG = "EXTREME_MOTION";

	private Thread mFetchFrameThread;
	private AsyncFrameFetcher mAsyncFrameFetcher;
	private NewFrameReadyListener mNewFrameReadyListener;

	
	
	private EnumSet<StreamType> mStreamTypeSet;
	
	/**
	 * should be called once per application life.
	 * @param activity
	 * @param newFrameReadyListener
	 * @return a View which must be added to the layout.
	 */
	public View onCreate(final Activity activity, final NewFrameReadyListener newFrameReadyListener)
	{
		mStreamTypeSet = EnumSet.of(StreamType.RAW_IMAGE,StreamType.SKELETON, StreamType.WARNINGS);
		View[] cameraView = new View[1];
		  try {
			  ExtremeMotionGenerator.getInstance().initialize(mStreamTypeSet, activity, cameraView);
		  } catch (Exception exception) {
			  Log.e(TAG, "initialize was not successful, exception of type: " + exception.getClass().toString());

		  } 
		  mNewFrameReadyListener = newFrameReadyListener;
		  return cameraView[0];
	}
	
	
	private void startAsyncFetcherSeperateThread()
	{
		mAsyncFrameFetcher = new AsyncFrameFetcher(ExtremeMotionGenerator.getInstance(), mNewFrameReadyListener);
		mFetchFrameThread = new Thread(mAsyncFrameFetcher);
		mFetchFrameThread.start();
	}
	
	public void onDestroy() {
		mAsyncFrameFetcher.shutdown();
		ExtremeMotionGenerator.getInstance().shutdown();
	}
	
	public void onStop() {
		mAsyncFrameFetcher.shutdown();
		try {
			ExtremeMotionGenerator.getInstance().stopStreams(mStreamTypeSet);
		} catch (Exception e) {
			Log.e(TAG, "Error stopping streams: " + e.toString());
		}
	}
	
	public void onResume() {
		try {
			ExtremeMotionGenerator.getInstance().startStreams(mStreamTypeSet);
		} catch (Exception e) {
			Log.e(TAG, "Error starting streams: " + e.toString());
		}
		startAsyncFetcherSeperateThread();
	}
	
	public void reset() {
		onStop();
		onResume();
	}
	
	public FrameInfo getLatestFrameInfo()
	{
		return mAsyncFrameFetcher.getLatestFrameInfo();
	}
	
	public interface NewFrameReadyListener
	{
		public void onNewFrameReady(FrameInfo newFrameInfo);
	}	
	
}
