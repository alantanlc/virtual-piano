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
	
	private List<MatOfPoint> contoursOut = new ArrayList<MatOfPoint>();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> mContours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mPianoKeyContours = new ArrayList<MatOfPoint>();
		
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
		drawAllContours(dst, mPianoKeyContours);
		
		// 12. Sort piano keys and update contoursOut list
		contoursOut = sortPianoKeys(mPianoKeyContours, true);
		
		// Find piano mask using Convex Hull
		/*List<Point> pianoKeyContourPointsList = new ArrayList<Point>();
		MatOfPoint pianoKeyContourPointsMOP = new MatOfPoint();
		MatOfInt hull = new MatOfInt();
		
		for(int i=0; i<mPianoKeyContours.size(); i++) {
			pianoKeyContourPointsList.addAll(mPianoKeyContours.get(i).toList());
		}
		
		pianoKeyContourPointsMOP.fromList(pianoKeyContourPointsList);
		Imgproc.convexHull(pianoKeyContourPointsMOP, hull, true);*/	//Imgproc.convexHull(MatOfPoints points, MatOfInt hull);
		
		//Log.i(TAG, "Convex Hull Rows:" + Integer.toString(hull.rows()));
		
		// Hull contains the indices of the points in pianoKeyContours which comprise a convex hull
		// In order to draw the points with drawContours(),
		// populate a new MatOfPoint containing only the points on the convex hull,
		// and pass that to drawContours
		
		/*MatOfPoint mopOut = new MatOfPoint();
		mopOut.create((int) hull.size().height, 1, CvType.CV_32SC2);*/
		
		/*for(int i=0; i<hull.size().height; i++) {
			int index = (int) hull.get(i, 0)[0];
			double[] point = new double[] { pianoKeyContourPointsMOP.get(index, 0)[0], pianoKeyContourPointsMOP.get(index, 0)[1] };
			mopOut.put(i, 0, point);
			Log.i(TAG, "Point " + i + ": " + point[0] + ", " + point[1]);
		}
		
		List<MatOfPoint> hullMOPList = new ArrayList<MatOfPoint>();
		hullMOPList.add(mopOut);
		
		Mat hullMaskMat = new Mat(dst.size(), dst.type(), new Scalar(0));
		Imgproc.drawContours(hullMaskMat, hullMOPList, 0, new Scalar(255, 255, 255), -1);*/
		
		// 9. Apply mask to binary reference image.
		/*dst.copyTo(hullMaskMat, hullMaskMat);
		hullMaskMat.copyTo(dst);*/
		
		// 10. Invert masked image and detect black keys
	}
	
	public List<MatOfPoint> getPianoContours() {
		return contoursOut;
	}
	
	@Override
	public void drawAllContours(final Mat dst, List<MatOfPoint> contours) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, Colors.mLineColorBlue, 2);
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