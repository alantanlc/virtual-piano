package com.alantan.virtualpiano;

import java.util.ArrayList;
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
	
	private List<MatOfPoint> contoursOut = new ArrayList<MatOfPoint>();
	
	private Point lowestPoint = new Point();
	
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
		
		// 11. Draw convex hull points
		/*for(int i=0; i<hullContourLMOP.get(0).rows(); i++) {
			Point p = new Point(hullContourLMOP.get(0).get(i, 0));
			Imgproc.circle(dst, p, 10, Colors.mLineColorGreen, 2);
		}*/
		
		// 12. Find convex hull points that are within piano area
		// (Create new method)
		// getPointsByRegion();
		
		// 13. Reduce convex hull points to (maximum) 5 distinct points
		// to correspond to 5 finger tips (Create new method)
		// getFingerTipPoints();
		
		// 11. Get convexity defects
		//MatOfInt4 convDefMOI4 = new MatOfInt4();
		//Imgproc.convexityDefects(reducedHandContours.get(0), hullMOI, convDefMOI4);
		
		// Draw contours
		//Imgproc.drawContours(dst, contours, largestContourIndex, Colors.mLineColorGreen, 2);
		//Imgproc.drawContours(dst, reducedHandContours, 0, Colors.mLineColorRed, 2);
		Imgproc.drawContours(dst, hullContourLMOP, 0, Colors.mLineColorBlue, 2);
		
		// Draw convexity defect points
		/*if(!convDefMOI4.empty()) {
			List<Integer> cdList = convDefMOI4.toList();
			
			Point data[] = reducedHandContours.get(0).toArray();
			
			for(int i=0; i<cdList.size(); i+=4) {
				Point start = data[cdList.get(i)];
				Point end = data[cdList.get(i+1)];
				Point defect = data[cdList.get(i+2)];
				
				Imgproc.circle(dst, start, 15, Colors.mLineColorGreen, 2);
				Imgproc.circle(dst, end, 20, Colors.mLineColorRed, 2);
				Imgproc.circle(dst, defect, 10, Colors.mLineColorYellow, 2);
			}
		}*/
		
		// Find lowest point
		lowestPoint = findLowestPoint(hullContourLMOP.get(0));
		
		// Draw lowest point
		if(lowestPoint != null) {
			Imgproc.circle(dst, lowestPoint, 5, Colors.mLineColorRed, -1);
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
}
