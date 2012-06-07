package org.nilis.utils.image_processing;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;

import android.graphics.Bitmap;

public class BitmapFactory {
	
	private static VMRuntimeHack runtime;
	
	public BitmapFactory(boolean useHack) {
		this.useHack = useHack;
		if(useHack && runtime == null) {
			runtime = new VMRuntimeHack();
		}
	}
	
	public Bitmap createBitmap(int dx, int dy) {
        return createBitmap(dx, dy, Bitmap.Config.RGB_565);
	}
	
	public Bitmap createBitmap(int dx, int dy, Bitmap.Config config) {
        Bitmap bmp = Bitmap.createBitmap(dx, dy, config);
        trackBitmap(bmp);
        return bmp;
	}
	
	public Bitmap createScaledBitmap(Bitmap source, int width, int height, boolean filter) {
		Bitmap bmp = Bitmap.createScaledBitmap(source, width, height, filter);
		trackBitmap(bmp);
		return bmp;
	}
	
	public Bitmap decodeFile(String path) {
        Bitmap bmp = android.graphics.BitmapFactory.decodeFile(path);
        trackBitmap(bmp);
        return bmp;
	}
	
	private void trackBitmap(Bitmap bmp) {
		if (useHack) {
            runtime.trackFree(bmp.getRowBytes() * bmp.getHeight());
            hackedBitmaps.add(new SoftReference<Bitmap>(bmp));
        }
	}

	public void free(Bitmap bmp) {
			bmp.recycle();
        if (hackedBitmaps.contains(bmp)) {
            runtime.trackAlloc(bmp.getRowBytes() * bmp.getHeight());
            hackedBitmaps.remove(bmp);
        }
	}
  	
	public void freeAll() {
		Bitmap bmp = null;
		for (SoftReference<Bitmap> bmpRef : hackedBitmaps) {
			bmp = bmpRef.get();
			if(bmp != null)
			free(bmp);
		}
	}

	private final boolean useHack;
	private Set<SoftReference<Bitmap>> hackedBitmaps = new HashSet<SoftReference<Bitmap>>(); 
}
