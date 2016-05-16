package com.alantan.virtualpiano;

import java.util.List;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

public class Detector {
	public void apply(final Mat src, final Mat dst) {
		
	}
	
	public void drawAllContours(final Mat dst, List<MatOfPoint> contours) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, Colors.mLineColorGreen, -1);
		}
	}
	
	public int findLargestContourIndex(List<MatOfPoint> contours) {
		int index = -1;
		double maxArea = 0;
		
		for(int i=0; i<contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i)) > maxArea) {
				index = i;
				maxArea = Imgproc.contourArea(contours.get(i));
			}
		}
		
		return index;
	}
	
	public MatOfPoint reduceContourPoints(MatOfPoint contours) {
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		MatOfPoint2f contour2f = new MatOfPoint2f(contours.toArray());
		
		double approxDistance = Imgproc.arcLength(contour2f, true) * 0.01;
		
		Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
		
		MatOfPoint points = new MatOfPoint(approxCurve.toArray());
		
		return points;
	}
	
	public MatOfPoint hullToContour(MatOfInt hullMOI, MatOfPoint contourMOP) {
		MatOfPoint mopOut = new MatOfPoint();
		mopOut.create((int) hullMOI.size().height, 1, CvType.CV_32SC2);
		
		for(int i=0; i<hullMOI.size().height; i++) {
			int index = (int) hullMOI.get(i, 0)[0];
			double[] point = new double[] { contourMOP.get(index, 0)[0], contourMOP.get(index, 0)[1] };
			mopOut.put(i, 0, point);
		}
		
		return mopOut;
	}
}
