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

public class FingersDetector implements Detector {
	
	private final static String TAG = FingersDetector.class.getSimpleName();

	private final int lowerHue = 3;
	private final int upperHue = 33;
	private final Scalar lowerThreshold = new Scalar(lowerHue, 50, 50);
	private final Scalar upperThreshold = new Scalar(upperHue, 255, 255);
	private final Mat mMat = new Mat();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mHandContours = new ArrayList<MatOfPoint>();
		
		// 1. Convert image to HSV color space
		Imgproc.cvtColor(src, mMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Apply static skin color threshold using inRange function
		Core.inRange(mMat, lowerThreshold, upperThreshold, mMat);
		
		// 3. Perform dilation
		Imgproc.dilate(mMat, mMat, new Mat());
		
		// 4. Find contours
		Imgproc.findContours(mMat, mContours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. If no contours, return
		if(mContours.size() == 0) {
			Log.i(TAG, "No contours found");
			return;
		}
		
		// 6. Find index of largest contour
		// Assume that contour with the largest area is the hand
		int largestContourIndex = findLargestContour(mContours);
		
		// 6b. If hand area is smaller than specific number,
		// assume contour is not a hand and return (i.e, no hand exists in frame)
		if(largestContourIndex == -1) {
			Log.i(TAG, "No hand detected");
			return;
		}
		
		// 7. Reduce number of points using DP algorithm
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		MatOfPoint2f contour2f = new MatOfPoint2f(mContours.get(largestContourIndex).toArray());
		double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;	// arclength returns perimeter
		Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
		
		// Convert back to MatOfPoint
		MatOfPoint points = new MatOfPoint(approxCurve.toArray());
		
		// 8. Store hand contours in a separate ArrayList
		mHandContours.add(points);
		
		// 9. Get convex hull of hand
		List<Point> handContourListPoint = new ArrayList<Point>();
		MatOfPoint handContourMOP = new MatOfPoint();
		MatOfInt hull = new MatOfInt();
		
		handContourListPoint.addAll(mHandContours.get(0).toList());
		handContourMOP.fromList(handContourListPoint);
		Imgproc.convexHull(handContourMOP, hull, true);
		
		// 10. Convert hull to contours
		MatOfPoint mopOut = new MatOfPoint();
		mopOut.create((int) hull.size().height, 1, CvType.CV_32SC2);
		
		for(int i=0; i<hull.size().height; i++) {
			int index = (int) hull.get(i, 0)[0];
			double[] point = new double[] { handContourMOP.get(index, 0)[0], handContourMOP.get(index, 0)[1] };
			mopOut.put(i, 0, point);
			//Log.i(TAG, "Point " + i + ": " + point[0] + ", " + point[1]);
		}
		
		List<MatOfPoint> hullContours = new ArrayList<MatOfPoint>();
		hullContours.add(mopOut);
		
		// 11. Draw hull contours
		Imgproc.drawContours(dst, hullContours, 0, new Scalar(0, 255, 0), 2);
		
		MatOfInt4 mConvexityDefectsMatOfInt4 = new MatOfInt4();
		
		Imgproc.convexityDefects(mHandContours.get(0), hull, mConvexityDefectsMatOfInt4);
		
		if(!mConvexityDefectsMatOfInt4.empty()) {
			List<Integer> cdList = mConvexityDefectsMatOfInt4.toList();
			Point data[] = mHandContours.get(0).toArray();
			
			for(int i=0; i<cdList.size(); i+=4) {
				Point start = data[cdList.get(i)];
				Point end = data[cdList.get(i+1)];
				Point defect = data[cdList.get(i+2)];
		        //Point depth = data[cdList.get(j+3)];

		        Imgproc.circle(dst, start, 5, new Scalar(0, 255, 0), 2);
		        Imgproc.circle(dst, end, 5, new Scalar(0, 255, 0), 2);
		        Imgproc.circle(dst, defect, 5, new Scalar(0, 255, 0), 2);
			}
		}
	}
	
	private int findLargestContour(List<MatOfPoint> contours) {
		int index = -1;
		double maxArea = -1;
		
		for(int i=0; i<contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i)) > maxArea && Imgproc.contourArea(contours.get(i)) > 25000) {
				index = i;
			}
		}
		
		return index;
	}
}
