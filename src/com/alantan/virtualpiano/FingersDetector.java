package com.alantan.virtualpiano;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FingersDetector implements Detector {
	
	private final static String TAG = FingersDetector.class.getSimpleName();

	private int lowerHue = 3;
	private int upperHue = 33;
	private Scalar lowerThreshold = new Scalar(lowerHue, 50, 50);
	private Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	private Mat hsvMat = new Mat();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, hsvMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply static skin color threshold using inRange function
		Core.inRange(hsvMat, lowerThreshold, upperThreshold, dst);
	}
}
