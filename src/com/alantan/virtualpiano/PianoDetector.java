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
	
	private final int whiteKeySizeLower = 1000;
	private final int whiteKeySizeUpper = 12500;
	
	private final int blackKeySizeLower = 500;
	private final int blackKeySizeUpper = 70000;
	
	private List<MatOfPoint> contoursOutLMOP = new ArrayList<MatOfPoint>();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> mWhiteContoursLMOP = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mWhiteKeysLMOP = new ArrayList<MatOfPoint>();
		
		List<MatOfPoint> mBlackContoursLMOP = new ArrayList<MatOfPoint>();
		
		List<Point> mPianoKeyContoursLP = new ArrayList<Point>();
		MatOfPoint mPianoKeyContoursMOP = new MatOfPoint();
		
		MatOfInt hullMOI = new MatOfInt();
		
		MatOfPoint mPianoMaskMOP = new MatOfPoint();
		List<MatOfPoint> mPianoMaskLMOP = new ArrayList<MatOfPoint>();
		Mat mPianoMaskMat = new Mat(mMaskMat.size(), mMaskMat.type(), new Scalar(0));
		
		// 1. Convert the image to HSV color space
		Imgproc.cvtColor(src, mHSVMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply threshold to detect white piano keys
		Core.inRange(mHSVMat, lowerThreshold, upperThreshold, mMaskMat);
		
		// 3. Perform erosion
		//Imgproc.erode(mMaskMat, mMaskMat, new Mat());
		
		// 4. Find contours
		Imgproc.findContours(mMaskMat, mWhiteContoursLMOP, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. If no contours detected, return.
		if(mWhiteContoursLMOP.size() == 0) {
			Log.i(TAG, "No contours found!");
			return;
		}
		
		// 7. Get contours that are within certain contour size range
		mWhiteKeysLMOP = getContoursBySizeRange(mWhiteContoursLMOP, whiteKeySizeLower, whiteKeySizeUpper);
		
		// 8. Reduce number of points of each contour using DP algorithm
		for(int i=0; i<mWhiteKeysLMOP.size(); i++) {
			mWhiteKeysLMOP.set(i, reduceContourPoints(mWhiteKeysLMOP.get(i)));
		}
		
		// 9. Eliminate contours that have less than 6 points or more than 8 points
		
		// 10. If no contours, just return
		if(mWhiteKeysLMOP.size() == 0) {
			return;
		}
		
		// 11. Draw piano key contours
		drawAllContours(dst, mWhiteKeysLMOP, -1);
		
		// 12. Get convex hull of piano
		for(int i=0; i<mWhiteKeysLMOP.size(); i++) {
			mPianoKeyContoursLP.addAll(mWhiteKeysLMOP.get(i).toList());
		}
		
		mPianoKeyContoursMOP.fromList(mPianoKeyContoursLP);
		Imgproc.convexHull(mPianoKeyContoursMOP, hullMOI);
		
		mPianoMaskMOP = hullToContour(hullMOI, mPianoKeyContoursMOP);
		mPianoMaskLMOP.add(mPianoMaskMOP);
		
		// Create piano mask
		Imgproc.drawContours(mPianoMaskMat, mPianoMaskLMOP, 0, Colors.mLineColorWhite, -1);
		Core.inRange(mHSVMat, lowerThreshold, upperThreshold, mMaskMat);
		
		// Dilate image 3 times to remove piano lines
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		
		// Invert piano mask
		Core.bitwise_not(mMaskMat, mMaskMat);
		
		mMaskMat.copyTo(mPianoMaskMat, mPianoMaskMat);
		
		// Find black key contours
		Imgproc.findContours(mPianoMaskMat, mBlackContoursLMOP, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		drawAllContours(dst, mBlackContoursLMOP, 2);
		
		// 12. Sort piano keys and update contoursOut list
		contoursOutLMOP = sortPianoKeys(mWhiteKeysLMOP, true);
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