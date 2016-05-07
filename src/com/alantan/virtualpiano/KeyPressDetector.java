package com.alantan.virtualpiano;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class KeyPressDetector {
	
	private final String TAG = KeyPressDetector.class.getSimpleName();
	
	private List<MatOfPoint2f> mWhiteKeysLMOP2f = new ArrayList<MatOfPoint2f>();
	private List<MatOfPoint2f> mBlackKeysLMOP2f = new ArrayList<MatOfPoint2f>();
	
	private int mPianoKeyIndex = -1;
	
	private double middleY = 0;
	
	public boolean checkFingerDownwardMotion(Point prevPoint, Point currPoint) {
		double xDiff = currPoint.x - prevPoint.x;
		double yDiff = currPoint.y - prevPoint.y;
		
		//Log.i(TAG, "Y diff: " + yDiff + ", X diff: " + xDiff);
		
		if(yDiff < 5 /*|| Math.abs(xDiff) > 40*/) {
			return false;
		}
		
		//Log.i(TAG, "Key pressed! Y diff: " + yDiff + ", X diff: " + xDiff);
		
		return true;
	}
	
	public void setMiddleY(double y) {
		middleY = y;
	}
	
	public int getPianoKeyIndex(Point point) {
		
		for(int i=0; i<mWhiteKeysLMOP2f.size(); i++) {
			if(point.y < middleY && Imgproc.pointPolygonTest(mWhiteKeysLMOP2f.get(i), point, false) == 1) {
				//Log.i(TAG, "Index: " + i);
				return i;
			}
		}
		
		for(int i=0; i<mBlackKeysLMOP2f.size(); i++) {
			if(Imgproc.pointPolygonTest(mBlackKeysLMOP2f.get(i), point, false) == 1) {
				//Log.i(TAG, "Index: " + i);
				return i+14;
			}
		}
		
		return -1;
	}
	
	public void setWhiteKeysMOP2f(List<MatOfPoint> lmop) {
		mWhiteKeysLMOP2f.clear();
		
		for(int i=0; i<lmop.size(); i++) {
			MatOfPoint2f p = new MatOfPoint2f();
			p.fromArray(lmop.get(i).toArray());
			mWhiteKeysLMOP2f.add(p);
		}
	}
	
	public void setBlackKeysMOP2f(List<MatOfPoint> lmop) {
		mBlackKeysLMOP2f.clear();
		
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
}
