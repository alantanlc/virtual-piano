package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class PianoDetector implements Detector {
	
	private final static String TAG = PianoDetector.class.getSimpleName();
	
	private final Mat hsvMat = new Mat();
	private final Mat mMaskMat = new Mat();
	private final Mat mDilatedMat = new Mat();
	
	// The color of the outline drawn around the detected image.
	private final Scalar mLineColorRed = new Scalar(255, 0, 0);
	private final Scalar mLineColorGreen = new Scalar(0, 255, 0);
	private final Scalar mLineColorBlue = new Scalar(0, 0, 255);
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		List<MatOfPoint> pianoKeyContours = new ArrayList<MatOfPoint>();
		
		// 1. Color Space Conversion
		// Convert the image to HSV
		Imgproc.cvtColor(src, hsvMat, Imgproc.COLOR_RGB2HSV);
		
		// 2. Image Processing
		// Preprocess image to select all pixels within white color range
		// using OpenCV's InRange function
		Scalar lowerThreshold = new Scalar(0, 0, 100);
		Scalar upperThreshold = new Scalar(179, 255, 255);
		Core.inRange(hsvMat, lowerThreshold, upperThreshold, mMaskMat);
		
		// 3. Perform morphological operations
		// Dilation, helps in removing noise in the mask.
		Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());
		
		// 4. Finding contours
		Imgproc.findContours(mDilatedMat, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 5. Reduce number of points for each contours
		// and store potential piano key contours in a separate ArrayList
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		for(int i=0; i<contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i)) > 10000
					&& Imgproc.contourArea(contours.get(i)) < 200000) {
				// Convert contour(i) from MatOfPoint to MatOfPoint2f
				MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
				
				// Processing on mMOP2f1 which is in type MatOfPoint2f
				// The function approxPolyDP approximates a curve or a polygon to another curve/polygon 
				// with less vertices so that the distance between them is less than or equal to the specified precision.
				// It uses the Douglas-Peucker algorithm
				double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;	// arclength returns perimeter
				Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
				
				// Convert back to MatOfPoint
				MatOfPoint points = new MatOfPoint(approxCurve.toArray());
				
				// If point size between 6 and 8, assume that it is a piano key contour
				// and add to pianoKeyContours list
				if(points.rows() >= 6 && points.rows() <= 8) {
					pianoKeyContours.add(points);
				}
				
				//Log.i(TAG, "Coordinates: " + contours.get(i).toArray()[0].toString());
			}
		}
		
		// Log.i("ContourDetectionFilter", "Piano Key Contour Size: " + Integer.toString(pianoKeyContours.size()));
		
		// If no contours, just return
		if(pianoKeyContours.size() == 0) {
			return;
		}
		
		// 6. Draw piano key contours
		/*for(int i=0; i<pianoKeyContours.size(); i++) {
			Imgproc.drawContours(dst, pianoKeyContours, i, new Scalar(0), -1);
		}*/
		
		// 7. Find piano mask using Convex Hull
		List<Point> pianoKeyContourPointsList = new ArrayList<Point>();
		MatOfPoint pianoKeyContourPointsMOP = new MatOfPoint();
		MatOfInt hull = new MatOfInt();
		
		for(int i=0; i<pianoKeyContours.size(); i++) {
			pianoKeyContourPointsList.addAll(pianoKeyContours.get(i).toList());
		}
		
		pianoKeyContourPointsMOP.fromList(pianoKeyContourPointsList);
		Imgproc.convexHull(pianoKeyContourPointsMOP, hull, true);	//Imgproc.convexHull(MatOfPoints points, MatOfInt hull);
		//Log.i(TAG, "Convex Hull Rows:" + Integer.toString(hull.rows()));
		
		// Hull contains the indices of the points in pianoKeyContours which comprise a convex hull
		// In order to draw the points with drawContours(),
		// populate a new MatOfPoint containing only the points on the convex hull,
		// and pass that to drawContours
		
		MatOfPoint mopOut = new MatOfPoint();
		mopOut.create((int) hull.size().height, 1, CvType.CV_32SC2);
		
		for(int i=0; i<hull.size().height; i++) {
			int index = (int) hull.get(i, 0)[0];
			double[] point = new double[] { pianoKeyContourPointsMOP.get(index, 0)[0], pianoKeyContourPointsMOP.get(index, 0)[1] };
			mopOut.put(i, 0, point);
			//Log.i(TAG, "Point " + i + ": " + point[0] + ", " + point[1]);
		}
		
		List<MatOfPoint> hullMOPList = new ArrayList<MatOfPoint>();
		hullMOPList.add(mopOut);
		
		Mat hullMaskMat = new Mat(dst.size(), dst.type(), new Scalar(0));
		Imgproc.drawContours(hullMaskMat, hullMOPList, 0, new Scalar(255, 255, 255), -1);
		
		// 9. Apply mask to binary reference image.
		dst.copyTo(hullMaskMat, hullMaskMat);
		hullMaskMat.copyTo(dst);
		
		// 10. Invert masked image and detect black keys
		
		//pianoKeyContours.clear();
		//contours.clear();
	}
}