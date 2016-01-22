package com.alantan.virtualpiano;

import org.opencv.core.Mat;

public interface Detector {
	public abstract void apply(final Mat src, final Mat dst);
}
