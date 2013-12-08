package com.xtr3d.skeletonjointssamplerelease;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;

public class ImageHandler {
	
	byte[] mImageData;
	
	public ImageHandler(byte[] imageData) {
		mImageData = imageData;
	}

	public void saveImage(Context applicationContext)	{
		int nrOfPixels = mImageData.length / 3; // Three bytes per pixel.
		int pixels[] = new int[nrOfPixels];
		for(int i = 0; i < nrOfPixels; i++) {
		   int r = mImageData[3*i];
		   int g = mImageData[3*i + 1];
		   int b = mImageData[3*i + 2];
		   pixels[i] = Color.rgb(r,g,b);
		}
		Bitmap bitmap = Bitmap.createBitmap(pixels, 640, 480, Bitmap.Config.RGB_565);

		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date now = new Date();
		String fileName = "realpainting_" + formatter.format(now) + ".jpg";
		
		File f = new File(Environment.getExternalStorageDirectory()
		                        + File.separator + fileName);
		try {
		   f.createNewFile();
		    FileOutputStream fo = new FileOutputStream(f);
		    fo.write(bytes.toByteArray());
		} catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		}
		
		/*
		String fileURL = Media.insertImage(applicationContext.getContentResolver(), bitmap,
				"KidsPaint", "Picture");

		if (fileURL != null) {
			  Uri uri = Uri.parse(fileURL);
			  applicationContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					  uri));
		}
		*/
		
		Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	    Uri contentUri = Uri.fromFile(f);
	    mediaScanIntent.setData(contentUri);
	    applicationContext.sendBroadcast(mediaScanIntent);
	}
}
