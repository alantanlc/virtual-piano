package com.alantan.virtualpiano;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

@SuppressLint("NewApi")
//Use the deprecated Camera class.
@SuppressWarnings("deprecation")
public class CameraActivity extends AppCompatActivity implements CvCameraViewListener2 {
	// A tag for log output.
	private static final String TAG = CameraActivity.class.getSimpleName();
	
	// A key for storing the index of the active camera.
	private static final String STATE_CAMERA_INDEX = "cameraIndex";
	
	// A key for storing the index of the active image size.
	private static final String STATE_IMAGE_SIZE_INDEX = "imageSizeIndex";
	
	// An ID for items in the image size submenu.
	private static final int MENU_GROUP_ID_SIZE = 2;
	
	// The index of the active camera.
	private int mCameraIndex;
	
	// The index of the active image size.
	private int mImageSizeIndex;
	
	// Whether the active camera is front-facing.
	// If so, the camera view should be mirrored.
	private boolean mIsCameraFrontFacing;
	
	// The number of cameras on the device.
	private int mNumCameras;
	
	// The camera view.
	private CameraBridgeViewBase mCameraView;
	
	// The image sizes supported by the active camera.
	private List<Size> mSupportedImageSizes;
	
	// Whether an asynchronous menu action is in progress.
	// If so, menu interaction should be disabled.
	private boolean mIsMenuLocked;
	
	// PianoDetector
	private PianoDetector mPianoDetector;
	
	// Piano keys contour list
	private List<MatOfPoint> mWhiteKeysLMOP = new ArrayList<MatOfPoint>();
	private List<MatOfPoint> mBlackKeysLMOP = new ArrayList<MatOfPoint>();
	
	// FingerDetector
	private HandDetector mHandDetector;
	
	// Whether piano detection should be applied
	private boolean mIsPianoDetection;
	
	// Whether skin detection should be applied
	private boolean mIsFingersDetection;
	
	// KeyPressDetector
	private KeyPressDetector mKeyPressDetector;
	
	// To toggle piano layout
	private boolean mIsPianoLayout1;
	
	// To toggle between one or two hands detection
	private boolean mIsTwoHands;
	
	// To toggle dynamic keypress
	private boolean mIsDynamicKeyPress;
	
	// Whether erosion should be applied
	private boolean mIsHSV;
	
	// Whether dilation should be applied
	private boolean mIsYCbCr;
	
	// SoundPoolPlayer
	private SoundPoolPlayer sound;
	
	// Points to detect finger downward motion
	//private Point prevPoint;
	private Point currPoint;
	
	private int keyPressedIndex;
	
	private int checkKeyPressedMaxIndex;
	
	private List<Point> mFirstFingerTipsLP = new ArrayList<Point>();
	private List<Point> mSecondFingerTipsLP = new ArrayList<Point>();
	private List<Point> mThirdFingerTipsLP = new ArrayList<Point>();
	private List<Point> mFourthFingerTipsLP = new ArrayList<Point>();
	private List<Point> mCurrFingerTipsLP = new ArrayList<Point>();
	
	private List<Integer> mKeyPressedIndexLI = new ArrayList<Integer>();
	
	// Buttons
	private ToggleButton detectPianoToggleBtn;
	private ToggleButton detectSkinToggleBtn;
	private ToggleButton handsToggleBtn;
	private ToggleButton hsvToggleBtn;
	private ToggleButton yCbCrToggleBtn;
	private Button setPianoBtn;
	
	// The OpenCV loader callback.
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(final int status) {
			switch(status) {
			case LoaderCallbackInterface.SUCCESS:
				Log.d(TAG, "OpenCV loaded successfully");
				mCameraView.enableView();
				
				mPianoDetector = new PianoDetector();
				mHandDetector = new HandDetector();
				mKeyPressDetector = new KeyPressDetector();
				
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	// Suppress backward incompatibility errors because we provide
	// backward-compatible fallbacks.
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_camera);
		
		final Window window = getWindow();
		window.addFlags(
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		if(savedInstanceState != null) {
			mCameraIndex = savedInstanceState.getInt(STATE_CAMERA_INDEX, 0);
			mImageSizeIndex = savedInstanceState.getInt(STATE_IMAGE_SIZE_INDEX, 0);
		} else {
			mCameraIndex = 0;
			mImageSizeIndex = 8;
		}
		
		final Camera camera;
		
		// Certain data regarding device's cameras are unavailable on Froyo.
		// To avoid runtime errors, we check Build.VERSION.SDK_INT before using the new APIs.
		// Furthermore, to avoid seeing errors during static analysis (that is, before compilation),
		// we add the @SuppressLin("NewApi") annotation to the declaration of onCreate.
		
		//Also note that every call to Camera.open must be paired with a call to the camera instance's release method
		// in order to make the camera available later.
		// Otherwise, our app and other apps may subsequently encounter a RuntimeException when calling Camera.open.
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			CameraInfo cameraInfo = new CameraInfo();
			Camera.getCameraInfo(mCameraIndex, cameraInfo);
			mIsCameraFrontFacing = (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT);
			mNumCameras = Camera.getNumberOfCameras();
			camera = Camera.open(mCameraIndex);
		} else {	// pre-Gingerbread
			// Assume there is only 1 camera and it is rear-facing.
			mIsCameraFrontFacing = false;
			mNumCameras = 1;
			camera = Camera.open();
		}
		
		// If the Android version is lower than Jellybean, use this call to hide the status bar.
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
		final Parameters parameters = camera.getParameters();
		camera.release();
		mSupportedImageSizes = parameters.getSupportedPreviewSizes();
		final Size size = mSupportedImageSizes.get(mImageSizeIndex);
		
		//mCameraView = new JavaCameraView(this, mCameraIndex);
		
		mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
		mCameraView.setCameraIndex(mCameraIndex);
		//mCameraView.setMaxFrameSize(size.width, size.height);
		mCameraView.setMaxFrameSize(352, 288);
		mCameraView.enableFpsMeter();
		mCameraView.setCvCameraViewListener(this);
		
		View decorView = getWindow().getDecorView();
		// Hide the status bar.
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);
		
		sound = new SoundPoolPlayer(this);
		
		mIsPianoLayout1 = true;
		mIsPianoDetection = true;
		
		currPoint = new Point(0, 0);
		
		mKeyPressedIndexLI.add(-1);
		mKeyPressedIndexLI.add(-1);
		
		setButtonsClickListener();
	}
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save the current camera index.
		savedInstanceState.putInt(STATE_CAMERA_INDEX, mCameraIndex);
		
		// Save the current image size index.
		savedInstanceState.putInt(STATE_IMAGE_SIZE_INDEX, mImageSizeIndex);
		
		super.onSaveInstanceState(savedInstanceState);
	}
	
	// When we switch to a different camera or image size, it will be most convenient to
	// recreate the activity so that onCreate will run again. On Honeycomb and newer Android versions,
	// a recreate method is available, but for backward compatibility,
	// we should write our own alternative implementation
	@SuppressLint("NewApi")
	@Override
	public void recreate() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			super.recreate();
		} else {
			finish();
			startActivity(getIntent());
		}
	}
	
	// When the activity goes into the background (onPause) or finishes (onDestroy),
	// the camera view should be disabled.
	// When the activity comes into the foreground (onResume), the OpenCVLoader should attempt
	// to initialize the library. (Remember that the camera view is enabled once the library is successfully initialized.)
	
	@Override
	public void onPause() {
		if(mCameraView != null) {
			mCameraView.disableView();
		}
		
		mIsPianoDetection = false;
		mIsFingersDetection = false;
		mIsPianoLayout1 = false;
		mIsTwoHands = false;
		mIsDynamicKeyPress = false;
		
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
		
		// Hide the status bar.
		View decorView = getWindow().getDecorView();
		int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
		decorView.setSystemUiVisibility(uiOptions);

		mIsMenuLocked = false;
	}
	
	@Override
	public void onDestroy() {
		if(mCameraView != null) {
			mCameraView.disableView();
		}
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_camera, menu);
		if(mNumCameras < 2) {
			// Remove the option to switch cameras, since there is only 1.
			menu.removeItem(R.id.menu_next_camera);
		}
		
		int numSupportedImageSizes = mSupportedImageSizes.size();
		if(numSupportedImageSizes > 1) {
			final SubMenu sizeSubMenu = menu.addSubMenu(R.string.menu_image_size);
			for(int i=0; i<numSupportedImageSizes; i++) {
				final Size size = mSupportedImageSizes.get(i);
				sizeSubMenu.add(MENU_GROUP_ID_SIZE, i, Menu.NONE, String.format("%dx%d", size.width, size.height));
			}
		}
		return true;
	}

	// Suppress backward incompatibility errors because we provide
	// backward-compatible fallbacks (for recreate).
	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getGroupId() == MENU_GROUP_ID_SIZE) {
			mImageSizeIndex = item.getItemId();
			recreate();
			
			return true;
		}
		
		switch (item.getItemId()) {
		case R.id.menu_erosion:
			mIsHSV = !mIsHSV;
			return true;
		case R.id.menu_detect_piano:
			mIsPianoDetection = !mIsPianoDetection;
			mWhiteKeysLMOP.clear();
			mBlackKeysLMOP.clear();
			return true;
		case R.id.menu_set_piano:
			mIsPianoDetection = false;
			setPianoKeys();
			return true;
		case R.id.menu_detect_skin:
			mIsFingersDetection = !mIsFingersDetection;
			return true;
		case R.id.menu_toggle_layout:
			mIsPianoLayout1 = !mIsPianoLayout1;
			return true;
		case R.id.menu_toggle_hands:
			mFirstFingerTipsLP.clear();
			mIsTwoHands = !mIsTwoHands;
			return true;
		case R.id.menu_toggle_dynamic_keypress:
			mIsDynamicKeyPress = !mIsDynamicKeyPress;
			return true;
		case R.id.menu_next_camera:
			mIsMenuLocked = true;
			
			// With another camera index, recreate the activity.
			mCameraIndex = (mCameraIndex + 1) % mNumCameras;
			recreate();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG, "onCameraViewStarted");
		Log.i(TAG, "Width: " + width + " Height: " + height);
	}

	@Override
	public void onCameraViewStopped() {
		Log.i(TAG, "onCameraViewStopped");
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final Mat rgba = inputFrame.rgba();
		
		if(mIsPianoDetection) {
			mPianoDetector.apply(rgba, rgba);
		}
		
		if(mIsFingersDetection) {
			mHandDetector.apply(rgba, rgba, mIsTwoHands);
			
			mCurrFingerTipsLP.clear();
			mCurrFingerTipsLP.addAll(mHandDetector.getFingerTipsLPOut());
			
			if(!mCurrFingerTipsLP.isEmpty() && !mWhiteKeysLMOP.isEmpty() && !mBlackKeysLMOP.isEmpty()) {
				checkKeyPressed();
			}
			
			return rgba;
		}
		
		/*if(!mWhiteKeysLMOP.isEmpty()) {
			mPianoDetector.drawAllContours(rgba, mWhiteKeysLMOP, Colors.mLineColorGreen, 1);
		}
		
		if(!mBlackKeysLMOP.isEmpty()) {
			mPianoDetector.drawAllContours(rgba, mBlackKeysLMOP, Colors.mLineColorYellow, 1);
		}*/
		
		if(mIsHSV) {
			Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGB2HSV);
			//Scalar lowerThreshold = new Scalar(3, 50, 120);
			//Scalar upperThreshold = new Scalar(33, 255, 255);
			Scalar lowerThreshold = new Scalar(0, 0, 100);
			Scalar upperThreshold = new Scalar(179, 255, 255);
			Core.inRange(rgba, lowerThreshold, upperThreshold, rgba);
		}
		
		if(mIsYCbCr) {
			Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGB2YCrCb);
			// Skin pixels: 133 ≤ Cr ≤ 173 and 77 ≤ Cb ≤ 127
			// Skin pixels (Relaxed): 128 ≤ Cr ≤ 178 and 72 ≤ Cb ≤ 132
			Scalar lowerThreshold = new Scalar(0, 133, 77);
			Scalar upperThreshold = new Scalar(255, 173, 127);
			Core.inRange(rgba, lowerThreshold, upperThreshold, rgba);
		}
		
		// Flip image if using front facing camera
		if(mIsCameraFrontFacing) {
			// Mirror (horizontally flip) the previews.
			Core.flip(rgba, rgba, 1);
		}
		
		return rgba;
	}
	
	private void checkKeyPressed() {
		checkKeyPressedMaxIndex = (mFirstFingerTipsLP.size() < mCurrFingerTipsLP.size()) ? mFirstFingerTipsLP.size() : mCurrFingerTipsLP.size();
		
		for(int i=0; i<checkKeyPressedMaxIndex; i++) {
			currPoint = mCurrFingerTipsLP.get(i);
			
			if(mKeyPressDetector.checkFingerDownwardMotion(mFirstFingerTipsLP.get(i), currPoint)) {
				keyPressedIndex = mKeyPressDetector.getPianoKeyIndex(currPoint);
				
				if(keyPressedIndex != -1 && keyPressedIndex != mKeyPressedIndexLI.get(i)) {
					playSound(keyPressedIndex);
					mKeyPressedIndexLI.set(i, keyPressedIndex);
					continue;
				}
			}
			
			mKeyPressedIndexLI.set(i, -1);
		}
		
		mFourthFingerTipsLP.clear();
		mFourthFingerTipsLP.addAll(mCurrFingerTipsLP);
		
		mThirdFingerTipsLP.clear();
		mThirdFingerTipsLP.addAll(mFourthFingerTipsLP);
		
		mThirdFingerTipsLP.clear();
		mThirdFingerTipsLP.addAll(mCurrFingerTipsLP);
		
		mSecondFingerTipsLP.clear();
		mSecondFingerTipsLP.addAll(mThirdFingerTipsLP);
		
		mFirstFingerTipsLP.clear();
		mFirstFingerTipsLP.addAll(mSecondFingerTipsLP);
	}
	
	private void setPianoKeys() {
		mWhiteKeysLMOP = mPianoDetector.getWhiteKeysLMOP();
		mBlackKeysLMOP = mPianoDetector.getBlackKeysLMOP();
		mKeyPressDetector.setWhiteKeysMOP2f(mWhiteKeysLMOP);
		mKeyPressDetector.setBlackKeysMOP2f(mBlackKeysLMOP);
	}
	
	private void playSound(int i) {
		if(i == -1) return;
		if(mIsPianoLayout1) {
			//play sound from layout 1
			sound.playLayout1Sound(i);
		} else {
			// play sound from layout 2
			sound.playLayout2Sound(i);
		}
	}
	
	private void setButtonsClickListener() {
		detectPianoToggleBtn = (ToggleButton) findViewById(R.id.toggle_btn_detect_piano);
		setPianoBtn = (Button) findViewById(R.id.btn_set_piano);
		detectSkinToggleBtn = (ToggleButton) findViewById(R.id.toggle_btn_detect_skin);
		handsToggleBtn = (ToggleButton) findViewById(R.id.toggle_btn_two_hands);
		hsvToggleBtn = (ToggleButton) findViewById(R.id.toggle_btn_hsv);
		yCbCrToggleBtn = (ToggleButton) findViewById(R.id.toggle_btn_ycbcr);
		
		detectPianoToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked) {
					// Toggle is enabled
					mIsPianoDetection = true;
					mWhiteKeysLMOP.clear();
					mBlackKeysLMOP.clear();
				} else {
					// Toggle is disabled
					mIsPianoDetection = false;
				}
			}
		});
		
		detectSkinToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				// TODO Auto-generated method stub
				if(isChecked) {
					// Toggle is enabled
					mIsFingersDetection = true;
				} else {
					// Toggle is disabled
					mIsFingersDetection = false;
				}
			}
		});
		
		handsToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				// TODO Auto-generated method stub
				if(isChecked) {
					// Toggle is enabled
					mIsTwoHands = true;
				} else {
					// Toggle is disabled
					mIsTwoHands = false;
				}
			}
		});
		
		hsvToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked) {
					// Toggle is enabled
					mIsHSV = true;
					yCbCrToggleBtn.setChecked(false);
				} else {
					// Toggle is disabled
					mIsHSV = false;
				}
			}
		});
		
		yCbCrToggleBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked) {
					// Toggle is enabled
					mIsYCbCr = true;
					hsvToggleBtn.setChecked(false);
				} else {
					// Toggle is disabled
					mIsYCbCr = false;
				}
			}
		});
		
		setPianoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(!detectPianoToggleBtn.isChecked()) return;
				
				setPianoKeys();
				detectPianoToggleBtn.setChecked(false);
			}
		});
	}
}
