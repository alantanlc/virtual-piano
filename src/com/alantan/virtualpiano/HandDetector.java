package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class HandDetector extends Detector {
	
	private final static String TAG = HandDetector.class.getSimpleName();

	private final int handArea = 500;
	
	private final int lowerHue = 3;
	private final int upperHue = 33;
	
	private final Scalar lowerThreshold = new Scalar(lowerHue, 50, 100);
	private final Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	
	private final Mat mMat = new Mat();
	
	private List<MatOfPoint> contoursOut = new ArrayList<MatOfPoint>();
	
	private Point lowestPoint = new Point();
	
	private List<Point> mFingerTipsLPOut = new ArrayList<Point>();
	
	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	
	private List<MatOfPoint> mPianoMaskLMOP = new ArrayList<MatOfPoint>();
	
	@Override
	public void apply(final Mat dst, final Mat src) {
		
	}
	
	public void apply(final Mat dst, final Mat src, boolean mIsTwoHands) {
		mFingerTipsLPOut.clear();
		contours.clear();
		
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, mMat, Imgproc.COLOR_RGB2HSV);
		
		if(!mPianoMaskLMOP.isEmpty()) {
			Imgproc.drawContours(dst, mPianoMaskLMOP, 0, Colors.mLineColorGreen, 1);
			
			Rect r = Imgproc.boundingRect(mPianoMaskLMOP.get(0));
			
			Imgproc.rectangle(dst, r.tl(), r.br(), Colors.mLineColorBlue, 1);
		}
		
		// 2. Apply static skin color threshold
		Core.inRange(mMat, lowerThreshold, upperThreshold, mMat);
		
		// 3a. Perform dilation
		//Imgproc.dilate(mMat, mMat, new Mat());
		
		// 3a. Perform erosion
		Imgproc.erode(mMat, mMat, new Mat());
		
		// 4. Find contours
		Imgproc.findContours(mMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. If no contours, return
		if(contours.size() == 0) { 
			//Log.i(TAG, "No contours found");
			//lowestPoint = null;
			return;
		}
		
		// 6. Find index of the largest contour, assume that is the hand
		int largestContourIndex = findLargestContourIndex(contours);
		
		// 7. If index equals -1, return
		if(largestContourIndex == -1 || Imgproc.contourArea(contours.get(largestContourIndex)) < handArea) {
			//Log.i(TAG, "No hand detected");
			//lowestPoint = null;
			return;
		};
		
		// 8. Reduce number of points using DP algorithm
		//List<MatOfPoint> reducedHandContours = new ArrayList<MatOfPoint>();
		//reducedHandContours.add(reduceContourPoints(contours.get(largestContourIndex)));
		
		Imgproc.drawContours(dst, contours, largestContourIndex, Colors.mLineColorBlue, 1);
		
		mFingerTipsLPOut.add(findLowestPoint(contours.get(largestContourIndex)));
		
		// Draw lowest point
		if(mFingerTipsLPOut.get(0) != null) {
			Imgproc.circle(dst, mFingerTipsLPOut.get(0), 2, Colors.mLineColorRed, -1);
		}
		
		if(!mIsTwoHands) {
			return;
		}
		
		// Else detect second hand
		contours.remove(largestContourIndex);
		largestContourIndex = findLargestContourIndex(contours);
		
		// 7. If index equals -1, return
		if(largestContourIndex == -1 || Imgproc.contourArea(contours.get(largestContourIndex)) < handArea) {
			//Log.i(TAG, "No hand detected");
			return;
		};
		
		//reducedHandContours.add(reduceContourPoints(contours.get(largestContourIndex)));
		mFingerTipsLPOut.add(findLowestPoint(contours.get(largestContourIndex)));
		
		// Draw lowest point
		if(mFingerTipsLPOut.get(1) != null) {
			Imgproc.circle(dst, mFingerTipsLPOut.get(1), 2, Colors.mLineColorRed, -1);
		}
	}
	
	public void drawAllContours(final Mat dst, List<MatOfPoint> contours, Scalar color, int thickness) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, color, thickness);
		}
	}
	
	public List<MatOfPoint> getHandContours() {
		return contoursOut;
	}
	
	private Point findLowestPoint(MatOfPoint contour) {
		Point highest = new Point();
		highest.x = 0;
		highest.y = 0;
		
		for(int i=0; i<contour.rows(); i++) {
			if(contour.get(i, 0)[1] > highest.y) {
				highest.x = contour.get(i, 0)[0];
				highest.y = contour.get(i, 0)[1];
			}
		}
		
		return highest;
	}
	
	public Point getLowestPoint() {
		return lowestPoint;
	}
	
	public List<Point> getFingerTipsLPOut() {
		return mFingerTipsLPOut;
	}
	
	public void setmPianoMaskLMOP(MatOfPoint mopIn) {
		mPianoMaskLMOP.add(mopIn);
	}
	
	public void clearPianoMaskLMOP() {
		mPianoMaskLMOP.clear();
	}
}
