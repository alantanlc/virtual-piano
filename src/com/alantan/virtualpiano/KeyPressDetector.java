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
	
	private List<MatOfPoint> mWhiteKeysLMOP = new ArrayList<MatOfPoint>();
	private List<MatOfPoint> mBlackKeysLMOP = new ArrayList<MatOfPoint>();
	
	private int mPianoKeyIndex = -1;
	
	private double mPianoDividerY;
	
	public boolean checkFingerDownwardMotion(Point prevPoint, Point currPoint) {
		double xDiff = currPoint.x - prevPoint.x;
		double yDiff = currPoint.y - prevPoint.y;
		
		//Log.i(TAG, "Y diff: " + yDiff + ", X diff: " + xDiff);
		
		if(yDiff < 8) {
			return false;
		}
		
		//Log.i(TAG, "Key pressed! Y diff: " + yDiff + ", X diff: " + xDiff);
		
		return true;
	}
	
	public int getPianoKeyIndex(Point point) {
		
		for(int i=0; i<mWhiteKeysLMOP.size(); i++) {
			MatOfPoint2f p = new MatOfPoint2f();
			p.fromArray(mWhiteKeysLMOP.get(i).toArray());
			
			if(Imgproc.pointPolygonTest(p, point, false) == 0
					|| Imgproc.pointPolygonTest(p, point, false) == 1) {
				Log.i(TAG, "Index: " + i);
				return i;
			}
		}
		
		for(int i=0; i<mBlackKeysLMOP.size(); i++) {
			MatOfPoint2f p = new MatOfPoint2f();
			p.fromArray(mBlackKeysLMOP.get(i).toArray());
			
			if(Imgproc.pointPolygonTest(p, point, false) == 0
					|| Imgproc.pointPolygonTest(p, point, false) == 1) {
				Log.i(TAG, "Index: " + i);
				return i+10;
			}
		}
		
		return -1;
	}
	
	public void setWhiteKeysLMOP(List<MatOfPoint> lmop) {
		mWhiteKeysLMOP = lmop;
	}
	
	public void setBlackKeysLMOP(List<MatOfPoint> lmop) {
		mBlackKeysLMOP = lmop;
	}
	
	public void setPianoDividingYCoord() {
		mPianoDividerY = Imgproc.boundingRect(mBlackKeysLMOP.get(6)).tl().y;
		
	}
	
	public boolean isNotConsecutiveKey(int index) {
		return (index != mPianoKeyIndex);
	}
	
	public void setPianoKeyIndex(int index) {
		mPianoKeyIndex = index;
	}
}
