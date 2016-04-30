package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class KeyPressDetector {
	
	private final String TAG = KeyPressDetector.class.getSimpleName();
	
	private List<MatOfPoint2f> mWhiteKeysLMOP2f = new ArrayList<MatOfPoint2f>();
	private List<MatOfPoint2f> mBlackKeysLMOP2f = new ArrayList<MatOfPoint2f>();
	
	private int mPianoKeyIndex = -1;

	private double mDivideConquerX;
	
	public boolean checkFingerDownwardMotion(Point prevPoint, Point currPoint) {
		double xDiff = currPoint.x - prevPoint.x;
		double yDiff = currPoint.y - prevPoint.y;
		
		//Log.i(TAG, "Y diff: " + yDiff + ", X diff: " + xDiff);
		
		if(yDiff < 8 || xDiff > 30) {
			return false;
		}
		
		//Log.i(TAG, "Key pressed! Y diff: " + yDiff + ", X diff: " + xDiff);
		
		return true;
	}
	
	public int getPianoKeyIndex(Point point) {
		
		int whiteIndex = -1;
		int blackIndex = -1;
		
		if(point.x < mDivideConquerX) {
			whiteIndex = whiteKeysPolygonTest(point, 5, 9);
			blackIndex = blackKeysPolygonTest(point, 3, 6);
		} else {
			whiteIndex = whiteKeysPolygonTest(point, 0, 4);
			blackIndex = blackKeysPolygonTest(point, 0, 3);
		}
		
		if(whiteIndex > blackIndex) {
			return whiteIndex;
		} else if(blackIndex > whiteIndex) {
			return blackIndex;
		}
		
		return -1;
	}
	
	public void setWhiteKeysMOP2f(List<MatOfPoint> lmop) {
		for(int i=0; i<lmop.size(); i++) {
			MatOfPoint2f p = new MatOfPoint2f();
			p.fromArray(lmop.get(i).toArray());
			mWhiteKeysLMOP2f.add(p);
		}
	}
	
	public void setBlackKeysMOP2f(List<MatOfPoint> lmop) {
		for(int i=0; i<lmop.size(); i++) {
			MatOfPoint2f p = new MatOfPoint2f();
			p.fromArray(lmop.get(i).toArray());
			mBlackKeysLMOP2f.add(p);
		}
	}
	
	public boolean isNotConsecutiveKey(int index) {
		return (index != mPianoKeyIndex);
	}
	
	public void setPianoKeyIndex(int index) {
		mPianoKeyIndex = index;
	}
	
	public void setDivideConquerX(double x) {
		mDivideConquerX = x;
	}
	
	private int whiteKeysPolygonTest(Point point, int start, int end) {
		int polygonTest;
		
		for(int i=start; i<=end; i++) {
			polygonTest = (int) Imgproc.pointPolygonTest(mWhiteKeysLMOP2f.get(i), point, false);
			if(polygonTest == 0 || polygonTest == 1) {
				return i;
			}
		}
		
		return -1;
	}
	
	private int blackKeysPolygonTest(Point point, int start, int end) {
		int polygonTest;
		
		for(int i=start; i<=end; i++) {
			polygonTest = (int) Imgproc.pointPolygonTest(mBlackKeysLMOP2f.get(i), point, false);
			if(polygonTest == 0 || polygonTest == 1) {
				return i+10;
			}
		}
		
		return -1;
	}
}
