package com.alantan.virtualpiano;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer= null;
    private HashMap mSounds = new HashMap();

    public SoundPoolPlayer(Context pContext)
    {
        // setup Soundpool
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

        mSounds.put(R.raw.pianoc1, this.mShortPlayer.load(pContext, R.raw.pianoc1, 1));
        mSounds.put(R.raw.pianod1, this.mShortPlayer.load(pContext, R.raw.pianod1, 1));
        mSounds.put(R.raw.pianoe1, this.mShortPlayer.load(pContext, R.raw.pianoe1, 1));
        mSounds.put(R.raw.pianof1, this.mShortPlayer.load(pContext, R.raw.pianof1, 1));
        mSounds.put(R.raw.pianog1, this.mShortPlayer.load(pContext, R.raw.pianog1, 1));
        mSounds.put(R.raw.pianoa1, this.mShortPlayer.load(pContext, R.raw.pianoa1, 1));
        mSounds.put(R.raw.pianob1, this.mShortPlayer.load(pContext, R.raw.pianob1, 1));
        mSounds.put(R.raw.pianoc2, this.mShortPlayer.load(pContext, R.raw.pianoc2, 1));
        mSounds.put(R.raw.pianod2, this.mShortPlayer.load(pContext, R.raw.pianod2, 1));
        mSounds.put(R.raw.pianoe2, this.mShortPlayer.load(pContext, R.raw.pianoe2, 1));
        mSounds.put(R.raw.pianof2, this.mShortPlayer.load(pContext, R.raw.pianof2, 1));
        mSounds.put(R.raw.pianog2, this.mShortPlayer.load(pContext, R.raw.pianog2, 1));
        mSounds.put(R.raw.pianoa2, this.mShortPlayer.load(pContext, R.raw.pianoa2, 1));
        mSounds.put(R.raw.pianob2, this.mShortPlayer.load(pContext, R.raw.pianob2, 1));
        
        mSounds.put(R.raw.pianocd1, this.mShortPlayer.load(pContext, R.raw.pianocd1, 1));
        mSounds.put(R.raw.pianode1, this.mShortPlayer.load(pContext, R.raw.pianode1, 1));
        mSounds.put(R.raw.pianofg1, this.mShortPlayer.load(pContext, R.raw.pianofg1, 1));
        mSounds.put(R.raw.pianoga1, this.mShortPlayer.load(pContext, R.raw.pianoga1, 1));
        mSounds.put(R.raw.pianoab1, this.mShortPlayer.load(pContext, R.raw.pianoab1, 1));
        mSounds.put(R.raw.pianocd2, this.mShortPlayer.load(pContext, R.raw.pianocd2, 1));
        mSounds.put(R.raw.pianode2, this.mShortPlayer.load(pContext, R.raw.pianode2, 1));
        mSounds.put(R.raw.pianofg2, this.mShortPlayer.load(pContext, R.raw.pianofg2, 1));
        mSounds.put(R.raw.pianoga2, this.mShortPlayer.load(pContext, R.raw.pianoga2, 1));
        mSounds.put(R.raw.pianoab2, this.mShortPlayer.load(pContext, R.raw.pianoab2, 1));
        
        mSounds.put(R.raw.pianoc9, this.mShortPlayer.load(pContext, R.raw.pianoc9, 1));
        mSounds.put(R.raw.pianod9, this.mShortPlayer.load(pContext, R.raw.pianod9, 1));
        mSounds.put(R.raw.pianoe9, this.mShortPlayer.load(pContext, R.raw.pianoe9, 1));
        mSounds.put(R.raw.pianof9, this.mShortPlayer.load(pContext, R.raw.pianof9, 1));
        mSounds.put(R.raw.pianog9, this.mShortPlayer.load(pContext, R.raw.pianog9, 1));
        mSounds.put(R.raw.pianoa9, this.mShortPlayer.load(pContext, R.raw.pianoa9, 1));
        mSounds.put(R.raw.pianob9, this.mShortPlayer.load(pContext, R.raw.pianob9, 1));
        mSounds.put(R.raw.pianoc0, this.mShortPlayer.load(pContext, R.raw.pianoc0, 1));
        mSounds.put(R.raw.pianod0, this.mShortPlayer.load(pContext, R.raw.pianod0, 1));
        mSounds.put(R.raw.pianoe0, this.mShortPlayer.load(pContext, R.raw.pianoe0, 1));
        mSounds.put(R.raw.pianof0, this.mShortPlayer.load(pContext, R.raw.pianof0, 1));
        mSounds.put(R.raw.pianog0, this.mShortPlayer.load(pContext, R.raw.pianog0, 1));
        mSounds.put(R.raw.pianoa0, this.mShortPlayer.load(pContext, R.raw.pianoa0, 1));
        mSounds.put(R.raw.pianob0, this.mShortPlayer.load(pContext, R.raw.pianob0, 1));
        
        mSounds.put(R.raw.pianocd9, this.mShortPlayer.load(pContext, R.raw.pianocd9, 1));
        mSounds.put(R.raw.pianode9, this.mShortPlayer.load(pContext, R.raw.pianode9, 1));
        mSounds.put(R.raw.pianofg9, this.mShortPlayer.load(pContext, R.raw.pianofg9, 1));
        mSounds.put(R.raw.pianoga9, this.mShortPlayer.load(pContext, R.raw.pianoga9, 1));
        mSounds.put(R.raw.pianoab9, this.mShortPlayer.load(pContext, R.raw.pianoab9, 1));
        mSounds.put(R.raw.pianocd0, this.mShortPlayer.load(pContext, R.raw.pianocd0, 1));
        mSounds.put(R.raw.pianode0, this.mShortPlayer.load(pContext, R.raw.pianode0, 1));
        mSounds.put(R.raw.pianofg0, this.mShortPlayer.load(pContext, R.raw.pianofg0, 1));
        mSounds.put(R.raw.pianoga0, this.mShortPlayer.load(pContext, R.raw.pianoga0, 1));
        mSounds.put(R.raw.pianoab0, this.mShortPlayer.load(pContext, R.raw.pianoab0, 1));
    }

    public void playShortResource(int piResource, double yDiff) {
        int iSoundId = (Integer) mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, (float) getFloatVolume(yDiff), (float) getFloatVolume(yDiff), 0, 0, 1);
    }
    
    private float getFloatVolume(double yDiff) {
    	if(yDiff < 10) {
    		Log.i("SoundPoolPlayer", "Volume: p");
    		return 0.25f;
    	} else if(yDiff < 20) {
    		Log.i("SoundPoolPlayer", "Volume: mf");
    		return 0.5f;
    	} else if( yDiff < 30) {
    		Log.i("SoundPoolPlayer", "Volume: f");
    		return 0.75f;
    	} else {
    		Log.i("SoundPoolPlayer", "Volume: ff");
    		return 0.99f;
    	}
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
    
    public void playLayout1Sound(int i, double yDiff) {
    	// Layout one starts from note C
    	switch(i) {
		case 0:
			playShortResource(R.raw.pianoc1, yDiff);
			break;
		case 1:
			playShortResource(R.raw.pianod1, yDiff);
			break;
		case 2:
			playShortResource(R.raw.pianoe1, yDiff);
			break;
		case 3:
			playShortResource(R.raw.pianof1, yDiff);
			break;
		case 4:
			playShortResource(R.raw.pianog1, yDiff);
			break;
		case 5:
			playShortResource(R.raw.pianoa1, yDiff);
			break;
		case 6:
			playShortResource(R.raw.pianob1, yDiff);
			break;
		case 7:
			playShortResource(R.raw.pianoc2, yDiff);
			break;
		case 8:
			playShortResource(R.raw.pianod2, yDiff);
			break;
		case 9:
			playShortResource(R.raw.pianoe2, yDiff);
			break;
		case 10:
			playShortResource(R.raw.pianof2, yDiff);
			break;
		case 11:
			playShortResource(R.raw.pianog2, yDiff);
			break;
		case 12:
			playShortResource(R.raw.pianoa2, yDiff);
			break;
		case 13:
			playShortResource(R.raw.pianob2, yDiff);
			break;
		case 14:
			playShortResource(R.raw.pianocd1, yDiff);
			break;
		case 15:
			playShortResource(R.raw.pianode1, yDiff);
			break;
		case 16:
			playShortResource(R.raw.pianofg1, yDiff);
			break;
		case 17:
			playShortResource(R.raw.pianoga1, yDiff);
			break;
		case 18:
			playShortResource(R.raw.pianoab1, yDiff);
			break;
		case 19:
			playShortResource(R.raw.pianocd2, yDiff);
			break;
		case 20:
			playShortResource(R.raw.pianode2, yDiff);
			break;
		case 21:
			playShortResource(R.raw.pianofg2, yDiff);
			break;
		case 22:
			playShortResource(R.raw.pianoga2, yDiff);
			break;
		case 23:
			playShortResource(R.raw.pianoab2, yDiff);
			break;
		default:
			break;
		}
    }
    
    public void playLayout2Sound(int i, double yDiff) {
    	// Layout one starts from note C
    	switch(i) {
		case 0:
			playShortResource(R.raw.pianoc9, yDiff);
			break;
		case 1:
			playShortResource(R.raw.pianod9, yDiff);
			break;
		case 2:
			playShortResource(R.raw.pianoe9, yDiff);
			break;
		case 3:
			playShortResource(R.raw.pianof9, yDiff);
			break;
		case 4:
			playShortResource(R.raw.pianog9, yDiff);
			break;
		case 5:
			playShortResource(R.raw.pianoa9, yDiff);
			break;
		case 6:
			playShortResource(R.raw.pianob9, yDiff);
			break;
		case 7:
			playShortResource(R.raw.pianoc0, yDiff);
			break;
		case 8:
			playShortResource(R.raw.pianod0, yDiff);
			break;
		case 9:
			playShortResource(R.raw.pianoe0, yDiff);
			break;
		case 10:
			playShortResource(R.raw.pianof0, yDiff);
			break;
		case 11:
			playShortResource(R.raw.pianog0, yDiff);
			break;
		case 12:
			playShortResource(R.raw.pianoa0, yDiff);
			break;
		case 13:
			playShortResource(R.raw.pianob0, yDiff);
			break;
		case 14:
			playShortResource(R.raw.pianocd9, yDiff);
			break;
		case 15:
			playShortResource(R.raw.pianode9, yDiff);
			break;
		case 16:
			playShortResource(R.raw.pianofg9, yDiff);
			break;
		case 17:
			playShortResource(R.raw.pianoga9, yDiff);
			break;
		case 18:
			playShortResource(R.raw.pianoab9, yDiff);
			break;
		case 19:
			playShortResource(R.raw.pianocd0, yDiff);
			break;
		case 20:
			playShortResource(R.raw.pianode0, yDiff);
			break;
		case 21:
			playShortResource(R.raw.pianofg0, yDiff);
			break;
		case 22:
			playShortResource(R.raw.pianoga0, yDiff);
			break;
		case 23:
			playShortResource(R.raw.pianoab0, yDiff);
			break;
		default:
			break;
		}
    }
}