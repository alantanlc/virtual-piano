package com.alantan.virtualpiano;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FingerDetector implements Detector {
	
	private final static String TAG = FingerDetector.class.getSimpleName();

	private final int lowerHue = 3;
	private final int upperHue = 33;
	private final Scalar lowerThreshold = new Scalar(lowerHue, 50, 50);
	private final Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	private final Mat mMat = new Mat();
	
	@Override
	public void apply(final Mat dst, final Mat src) {
		
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, mMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply static skin color threshold
		Core.inRange(mMat, lowerThreshold, upperThreshold, dst);
		
		// 3. Perform dilation
		
	}
}
