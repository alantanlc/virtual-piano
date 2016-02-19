package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
	private final int blackKeySizeUpper = 5000;
	
	private List<MatOfPoint> whiteKeysOutLMOP = new ArrayList<MatOfPoint>();
	private List<MatOfPoint> blackKeysOutLMOP = new ArrayList<MatOfPoint>();
	
	private MatOfPoint mPianoMaskMOP = new MatOfPoint();
	private MatOfPoint mPianoRectMaskMOP = new MatOfPoint();
	
	@Override
	public void apply(final Mat src, final Mat dst) {
		List<MatOfPoint> mWhiteContoursLMOP = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mWhiteKeysLMOP = new ArrayList<MatOfPoint>();
		
		List<MatOfPoint> mBlackContoursLMOP = new ArrayList<MatOfPoint>();
		List<MatOfPoint> mBlackKeysLMOP = new ArrayList<MatOfPoint>();
		
		List<Point> mWhiteKeysLP = new ArrayList<Point>();
		MatOfPoint mWhiteKeysMOP = new MatOfPoint();
		
		MatOfInt hullMOI = new MatOfInt();
		
		//MatOfPoint mPianoMaskMOP = new MatOfPoint();
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
			Log.i(TAG, "No white contours found!");
			return;
		}
		
		// 7. Reduce number of points of each contour using DP algorithm
		for(int i=0; i<mWhiteContoursLMOP.size(); i++) {
			mWhiteContoursLMOP.set(i, reduceContourPoints(mWhiteContoursLMOP.get(i)));
		}
		
		// 8. Get contours that are within certain contour size range
		mWhiteKeysLMOP = getPianoKeyContours(mWhiteContoursLMOP, whiteKeySizeLower, whiteKeySizeUpper);
		
		// 10. If no contours, just return
		if(mWhiteKeysLMOP.size() == 0) {
			Log.i(TAG, "No white keys found!");
			return;
		}
		
		// 11. Draw white keys
		drawAllContours(dst, mWhiteKeysLMOP, Colors.mLineColorBlue, -1);
		
		// 12. Get convex hull of piano
		// 12a. Convert LMOP to LP
		for(int i=0; i<mWhiteKeysLMOP.size(); i++) {
			mWhiteKeysLP.addAll(mWhiteKeysLMOP.get(i).toList());
		}
		
		// 12b. Convert LP to MOP
		mWhiteKeysMOP.fromList(mWhiteKeysLP);
		
		// 12c. Get convex hull
		Imgproc.convexHull(mWhiteKeysMOP, hullMOI);
		
		// 12d. Convert hullMOI to MOP
		mPianoMaskMOP = hullToContour(hullMOI, mWhiteKeysMOP);
		
		// 12e. Convert MOP to LMOP
		mPianoMaskLMOP.add(mPianoMaskMOP);
		
		// 12f. Increase piano mask range for key press detection
		Rect mPianoRect = Imgproc.boundingRect(mPianoMaskMOP);
		
		mPianoRect.y -= 20;
		mPianoRect.height += 20;
		
		Imgproc.rectangle(dst, mPianoRect.tl(), mPianoRect.br(), Colors.mLineColorYellow, 2);
		
		
		
		// 13. Create piano mask mat
		Imgproc.drawContours(mPianoMaskMat, mPianoMaskLMOP, 0, Colors.mLineColorWhite, -1);
		Core.inRange(mHSVMat, lowerThreshold, upperThreshold, mMaskMat);
		
		// 14. Dilate image 3 times to remove piano lines
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		Imgproc.dilate(mMaskMat, mMaskMat, new Mat());
		
		// 15. Invert piano mask
		Core.bitwise_not(mMaskMat, mMaskMat);
		
		// 16. Apply piano mask to binary image
		mMaskMat.copyTo(mPianoMaskMat, mPianoMaskMat);
		
		// 17. Find black key contours
		Imgproc.findContours(mPianoMaskMat, mBlackContoursLMOP, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// 18. If no contours detected, return.
		if(mBlackContoursLMOP.size() == 0) {
			Log.i(TAG, "No black contours found!");
			return;
		}
		
		// 19. Reduce number of points of each contour using DP algorithm
		for(int i=0; i<mBlackContoursLMOP.size(); i++) {
			mBlackContoursLMOP.set(i, reduceContourPoints(mBlackContoursLMOP.get(i)));
		}
		
		// 20. Get contours that are within certain contour size range
		mBlackKeysLMOP = getPianoKeyContours(mBlackContoursLMOP, blackKeySizeLower, blackKeySizeUpper);
		
		// 21. If no contours, just return
		if(mBlackKeysLMOP.size() == 0) {
			Log.i(TAG, "No black key found!");
			return;
		}
		
		// 22. Draw black key contours
		drawAllContours(dst, mBlackKeysLMOP, Colors.mLineColorRed, -1);
		
		// 25. Sort piano keys and update whiteKeysOutLMOP and blackKeysOutLMOP
		whiteKeysOutLMOP = sortPianoKeys(mWhiteKeysLMOP, true);
		blackKeysOutLMOP = sortPianoKeys(mBlackKeysLMOP, true);
	}
	
	public List<MatOfPoint> getWhiteKeysLMOP() {
		return whiteKeysOutLMOP;
	}
	
	public List<MatOfPoint> getBlackKeysLMOP() {
		return blackKeysOutLMOP;
	}
	
	public MatOfPoint getPianoMaskMOP() {
		return mPianoMaskMOP;
	}
	
	public MatOfPoint getPianoRectMaskMOP() {
		return mPianoRectMaskMOP;
	}
	
	public void drawAllContours(final Mat dst, List<MatOfPoint> contours, Scalar color, int thickness) {
		for(int i=0; i<contours.size(); i++) {
			Imgproc.drawContours(dst, contours, i, color, thickness);
		}
	}
	
	private List<MatOfPoint> getPianoKeyContours(List<MatOfPoint> contours, int lower, int upper) {
		List<MatOfPoint> newContours = new ArrayList<MatOfPoint>();
		
		for(int i=0; i<contours.size(); i++) {
			//Log.i(TAG, Double.toString(Imgproc.contourArea(contours.get(i))));
			if((Imgproc.contourArea(contours.get(i)) >= lower && Imgproc.contourArea(contours.get(i)) <= upper)
					&& (contours.get(i).rows() >= 4 && contours.get(i).rows() <= 8)) {
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