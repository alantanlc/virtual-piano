package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.util.Log;

public class PianoDetector extends Detector {
	
	private final static String TAG = PianoDetector.class.getSimpleName();
	
	private final Mat mHSVMat = new Mat();
	private final Mat mMaskMat = new Mat();
	
	private final Scalar lowerThreshold = new Scalar(0, 0, 100);
	private final Scalar upperThreshold = new Scalar(179, 255, 255);
	
	private final int keySizeLower = 1000;
	private final int keySizeUpper = 12500;
	
	private List<MatOfPoint> contoursOutLMOP = new ArrayList<MatOfPoint>();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mPianoKeyContours = new ArrayList<MatOfPoint>();
		
		List<Point> mPianoKeyContoursLP = new ArrayList<Point>();
		MatOfPoint mPianoKeyContoursMOP = new MatOfPoint();
		
		// 1. Convert the image to HSV color space
		Imgproc.cvtColor(src, mHSVMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply threshold to detect white piano keys
		Core.inRange(mHSVMat, lowerThreshold, upperThreshold, mMaskMat);
		
		// 3. Perform erosion
		Imgproc.erode(src, src, new Mat());
		
		// 4. Find contours
		Imgproc.findContours(mMaskMat, mContours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. If no contours detected, return.
		if(mContours.size() == 0) {
			Log.i(TAG, "No contours found!");
			return;
		}
		
		// 7. Get contours that are within certain contour size range
		mPianoKeyContours = getContoursBySizeRange(mContours, keySizeLower, keySizeUpper);
		
		// 8. Reduce number of points of each contour using DP algorithm
		for(int i=0; i<mPianoKeyContours.size(); i++) {
			mPianoKeyContours.set(i, reduceContourPoints(mPianoKeyContours.get(i)));
		}
		
		// 9. Eliminate contours that have less than 6 points or more than 8 points
		
		// 10. If no contours, just return
		if(mPianoKeyContours.size() == 0) {
			return;
		}
		
		// 11. Draw piano key contours
		drawAllContours(dst, mPianoKeyContours, -1);
		
		// Get convex hull of piano
		//mPianoKeyContours.addAll(mPianoKeyContours);
		//mPianoKeyContoursMOP.fromList(mPianoKeyContoursLP);
		
		// 12. Sort piano keys and update contoursOut list
		contoursOutLMOP = sortPianoKeys(mPianoKeyContours, true);
		
		// 14. Piano mask
		
		// 15. Apply dilation 3 times to get rid of piano lines
		
		// 16. Invert binary image
		
		// 17. Get black key contours
	}
	
	public List<MatOfPoint> getPianoContours() {
		return contoursOutLMOP;
	}
	
	public void drawAllContours(final Mat dst, List<MatOfPoint> contours, int thickness) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, Colors.mLineColorBlue, thickness);
		}
	}
	
	private List<MatOfPoint> getContoursBySizeRange(List<MatOfPoint> contours, int lower, int upper) {
		List<MatOfPoint> newContours = new ArrayList<MatOfPoint>();
		
		for(int i=0; i<contours.size(); i++) {
			//Log.i(TAG, Double.toString(Imgproc.contourArea(contours.get(i))));
			if(Imgproc.contourArea(contours.get(i)) >= lower && Imgproc.contourArea(contours.get(i)) <= upper) {
				newContours.add(contours.get(i));
			}
		}
		
		return newContours;
	}
	
	private List<MatOfPoint> sortPianoKeys(List<MatOfPoint> contours, boolean reverse) {
		if(reverse) {
			Collections.sort(contours, new Comparator<MatOfPoint>() {
				public int compare(MatOfPoint mop1, MatOfPoint mop2) {
					return Double.compare(Imgproc.boundingRect(mop2).tl().x, Imgproc.boundingRect(mop1).tl().x);
				}
			});
		} else {
			Collections.sort(contours, new Comparator<MatOfPoint>() {
				public int compare(MatOfPoint mop1, MatOfPoint mop2) {
					return Double.compare(Imgproc.boundingRect(mop1).tl().x, Imgproc.boundingRect(mop2).tl().x);
				}
			});
		}
		
		return contours;
	}
}