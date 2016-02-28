package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class HandDetector extends Detector {
	
	private final static String TAG = HandDetector.class.getSimpleName();

	private final int handArea = 500;
	
	private final int lowerHue = 3;
	private final int upperHue = 33;
	
	private final Scalar lowerThreshold = new Scalar(lowerHue, 50, 120);
	private final Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	
	private final Mat mMat = new Mat();
	
	private Point lowestPoint = new Point();
	
	private MatOfPoint mPianoMaskMOP;
	
	private List<Point> fingerTipsLPOut = new ArrayList<Point>();
	
	@Override
	public void apply(final Mat dst, final Mat src) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, mMat, Imgproc.COLOR_RGB2HSV);
		
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
			lowestPoint = null;
			return;
		}
		
		// 6. Find index of the largest contour, assume that is the hand
		int largestContourIndex = findLargestContourIndex(contours);
		
		// 7. If index equals -1, return
		if(largestContourIndex == -1 || Imgproc.contourArea(contours.get(largestContourIndex)) < handArea) {
			//Log.i(TAG, "No hand detected");
			lowestPoint = null;
			return;
		};
		
		// 8. Reduce number of points using DP algorithm
		List<MatOfPoint> reducedHandContours = new ArrayList<MatOfPoint>();
		reducedHandContours.add(reduceContourPoints(contours.get(largestContourIndex)));
		
		// 9. Get convex hull of hand
		MatOfInt hullMOI = new MatOfInt();
		Imgproc.convexHull(reducedHandContours.get(0), hullMOI);
		
		// 10. Convert hull to contours
		List<MatOfPoint> hullContourLMOP = new ArrayList<MatOfPoint>();
		hullContourLMOP.add(hullToContour(hullMOI, reducedHandContours.get(0)));
		
		List<Point> pianoRegionConvexLP = new ArrayList<Point>();
		List<Point> fingerTipsLP = new ArrayList<Point>();
		
		// 11. Find reduced hand contour points that are within piano area
		if(mPianoMaskMOP != null) {
			pianoRegionConvexLP = getPointsByRegion(hullContourLMOP.get(0).toList(), mPianoMaskMOP);
		}
		
		// 12. Sort convex hull points by x-coordinate
		List<Point> sortedPianoRegionConvexLP = sortPoints(pianoRegionConvexLP, false);
		
		// 13. Reduce convex hull points to (maximum) 5 distinct points to correspond to 5 finger tips
		// may not necessarily return a list of 5 points, depends on how many fingers are within piano
		fingerTipsLP = getFingerTipsLP(sortedPianoRegionConvexLP);
		
		setFingerTipsLPOut(fingerTipsLP);
		
		// Find lowest point
		lowestPoint = findLowestPoint(hullContourLMOP.get(0));
		
		// Draw lowest point
		if(lowestPoint != null) {
			Imgproc.circle(dst, lowestPoint, 5, Colors.mLineColorRed, -1);
		}
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
	
	public void setPianoMaskMOP(MatOfPoint maskMOP) {
		mPianoMaskMOP = maskMOP;
	}
	
	private List<Point> getPointsByRegion(List<Point> hullPoints, MatOfPoint pianoMaskMOP) {
		List<Point> lpOut = new ArrayList<Point>();
		MatOfPoint2f p = new MatOfPoint2f();
		p.fromArray(pianoMaskMOP.toArray());
		
		for(int i=0; i<hullPoints.size(); i++) {
			if(Imgproc.pointPolygonTest(p, hullPoints.get(i), false) == 0
					|| Imgproc.pointPolygonTest(p, hullPoints.get(i), false) == 1) {
				lpOut.add(hullPoints.get(i));
			}
		}
		
		return lpOut;
	}
	
	private List<Point> sortPoints(List<Point> pointsLP, boolean reverse) {
		if(reverse) {
			Collections.sort(pointsLP, new Comparator<Point>() {
				public int compare(Point p1, Point p2) {
					return Double.compare(p2.x, p1.x);
				}
			});
		} else {
			Collections.sort(pointsLP, new Comparator<Point>() {
				public int compare(Point p1, Point p2) {
					return Double.compare(p1.x, p2.x);
				}
			});
		}
		
		return pointsLP;
	}
	
	private List<Point> getFingerTipsLP(List<Point> lpIn) {
		if(lpIn.size() <= 1) return lpIn;
		
		List<Point> lpOut = new ArrayList<Point>();
		int fingerIndex = 0;
		
		lpOut.add(lpIn.get(0));
		
		for(int i=1; i<lpIn.size(); i++) {
			//Log.i(TAG, "Gap: " + (lpIn.get(i).x - lpOut.get(fingerIndex).x));
			if(lpIn.get(i).x - lpOut.get(fingerIndex).x < 35 && Math.abs(lpIn.get(i).y - lpOut.get(fingerIndex).y) < 25) {
				lpOut.get(fingerIndex).x = (lpOut.get(fingerIndex).x + lpIn.get(i).x)/2;
				//lpOut.get(fingerIndex).y = (lpOut.get(fingerIndex).y + lpIn.get(i).y)/2;
				lpOut.get(fingerIndex).y = (lpOut.get(fingerIndex).y > lpIn.get(fingerIndex).y) ? lpOut.get(fingerIndex).y :  lpIn.get(fingerIndex).y;
			} else {
				lpOut.add(lpIn.get(i));
				fingerIndex++;
			}
		}
		
		return lpOut;
	}
	
	private void setFingerTipsLPOut(List<Point> lpIn) {
		fingerTipsLPOut.clear();
		fingerTipsLPOut = lpIn;
	}
	
	public List<Point> getFingerTipsLPOut() {
		return fingerTipsLPOut;
	}
}
