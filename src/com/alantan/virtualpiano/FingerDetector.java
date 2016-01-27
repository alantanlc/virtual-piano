package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class FingerDetector implements Detector {
	
	private final static String TAG = FingerDetector.class.getSimpleName();

	private final int lowerHue = 3;
	private final int upperHue = 33;
	private final Scalar lowerThreshold = new Scalar(lowerHue, 50, 50);
	private final Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	private final Mat mMat = new Mat();
	
	@Override
	public void apply(final Mat dst, final Mat src) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, mMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply static skin color threshold
		Core.inRange(mMat, lowerThreshold, upperThreshold, mMat);
		
		// 3a. Perform dilation
		Imgproc.dilate(mMat, mMat, new Mat());
		
		// 3a. Perform erosion
		Imgproc.erode(mMat, mMat, new Mat());
		
		// 4. Find contours
		Imgproc.findContours(mMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. If no contours, return
		if(contours.size() == 0) { 
			Log.i(TAG, "No contours found");
			return;
		}
		
		// 6. Find index of the largest contour, assume that is the hand
		int largestContourIndex = findLargestContourIndex(contours);
		
		// 7. If index equals -1, return
		if(largestContourIndex == -1) {
			Log.i(TAG, "No hand detected");
			return;
		};
		
		// 8. Reduce number of points using DP algorithm
		List<MatOfPoint> reducedHandContours = new ArrayList<MatOfPoint>();
		reducedHandContours.add(reduceContourPoints(contours.get(largestContourIndex)));
		
		// 9. Get convex hull of hand
		MatOfInt hullMOI = new MatOfInt();
		hullMOI = getConvexHull(reducedHandContours.get(0));
		
		// 10. Convert hull to contours
		List<MatOfPoint> hullContourLMOP = new ArrayList<MatOfPoint>();
		hullContourLMOP.add(hullToContour(hullMOI, reducedHandContours.get(0)));
		
		// 11. Get convexity defects
		MatOfInt4 convDefMOI4 = new MatOfInt4();
		Imgproc.convexityDefects(reducedHandContours.get(0), hullMOI, convDefMOI4);
		
		// Draw contours
		Imgproc.drawContours(dst, contours, largestContourIndex, Colors.mLineColorGreen, 2);
		Imgproc.drawContours(dst, reducedHandContours, 0, Colors.mLineColorRed, 2);
		Imgproc.drawContours(dst, hullContourLMOP, 0, Colors.mLineColorBlue, 2);
	
		// Draw convexity defect points
		if(!convDefMOI4.empty()) {
			List<Integer> cdList = convDefMOI4.toList();
			
			Point data[] = reducedHandContours.get(0).toArray();
			
			for(int i=0; i<cdList.size(); i+=4) {
				Point defect = data[cdList.get(i+2)];
				Imgproc.circle(dst, defect, 10, Colors.mLineColorPurple, 2);
			}
		}
	}
	
	private void drawAllContours(final Mat dst, List<MatOfPoint> contours) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, Colors.mLineColorBlue, 2);
		}
	}
	
	private int findLargestContourIndex(List<MatOfPoint> contours) {
		int index = -1;
		double maxArea = -1;
		
		for(int i=0; i<contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i)) > maxArea && Imgproc.contourArea(contours.get(i)) > 25000) {
				index = i;
			}
		}
		
		return index;
	}
	
	private MatOfPoint reduceContourPoints(MatOfPoint contours) {
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		MatOfPoint2f contour2f = new MatOfPoint2f(contours.toArray());
		
		double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
		
		Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
		
		MatOfPoint points = new MatOfPoint(approxCurve.toArray());
		
		return points;
	}
	
	private MatOfInt getConvexHull(MatOfPoint contour) {
		List<Point> contourLP = new ArrayList<Point>();
		MatOfPoint contourMOP = new MatOfPoint();
		MatOfInt hullMOI = new MatOfInt();
		
		contourLP.addAll(contour.toList());
		contourMOP.fromList(contourLP);
		Imgproc.convexHull(contourMOP, hullMOI, true);
		
		return hullMOI;
	}
	
	private MatOfPoint hullToContour(MatOfInt hullMOI, MatOfPoint contourMOP) {
		MatOfPoint mopOut = new MatOfPoint();
		mopOut.create((int) hullMOI.size().height, 1, CvType.CV_32SC2);
		
		for(int i=0; i<hullMOI.size().height; i++) {
			int index = (int) hullMOI.get(i, 0)[0];
			double[] point = new double[] { contourMOP.get(index, 0)[0], contourMOP.get(index, 0)[1] };
			mopOut.put(i, 0, point);
			
			Point x = new Point(point[0], point[1]);
			//Log.i(TAG, "Point " + i + ": " + point[0] + ", " + point[1]);
		}
		
		return mopOut;
	}
}
