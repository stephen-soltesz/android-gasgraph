package com.thinkseedo.gasgraph.widget;

import com.thinkseedo.gasgraph.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

public class PreferenceWidget extends LinearLayout {
	
	public PreferenceWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private static final String PREFERENCES_TAG = "android:preferences";
	
	private PreferenceManager mPreferenceManager;
	
	private Bundle mSavedInstanceState;
	
	/**
	 * The starting request code given out to preference framework.
	 */
	private static final int FIRST_REQUEST_CODE = 100;
	
	private static final int MSG_BIND_PREFERENCES = 0;
	private Handler mHandler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	        switch (msg.what) {
	            case MSG_BIND_PREFERENCES:
	                bindPreferences();
	                break;
	        }
	    }
	};
	ListView mListView=null;
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.preference_list_content, this);
	    
	    mPreferenceManager = onCreatePreferenceManager();
	    mListView = (ListView)findViewById(R.id.list);
	    mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
	}
	
	protected void onStop() {
	    //mPreferenceManager.dispatchActivityStop();
	}
	
	protected void onDestroy() {
	    //mPreferenceManager.dispatchActivityDestroy();
	}
	
	protected void onSaveInstanceState(Bundle outState) {
	    //super.onSaveInstanceState(outState);
	    //final PreferenceScreen preferenceScreen = getPreferenceScreen();
	    //if (preferenceScreen != null) {
	    //    Bundle container = new Bundle();
	    //    preferenceScreen.saveHierarchyState(container);
	    //    outState.putBundle(PREFERENCES_TAG, container);
	    //}
	}
	
	//protected void onRestoreInstanceState(Bundle state) {
	//    Bundle container = state.getBundle(PREFERENCES_TAG);
	//    if (container != null) {
	//        final PreferenceScreen preferenceScreen = getPreferenceScreen();
	//        if (preferenceScreen != null) {
	//            preferenceScreen.restoreHierarchyState(container);
	//            mSavedInstanceState = state;
	//            return;
	//        }
	//    }
	
	    // Only call this if we didn't save the instance state for later.
	    // If we did save it, it will be restored when we bind the adapter.
	//    super.onRestoreInstanceState(state);
	//}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    //super.onActivityResult(requestCode, resultCode, data);
	    //mPreferenceManager.dispatchActivityResult(requestCode, resultCode, data);
	}
	
	public void onContentChanged() {
	    //super.onContentChanged();
	    postBindPreferences();
	}
	
	/**
	 * Posts a message to bind the preferences to the list view.
	 * <p>
	 * Binding late is preferred as any custom preference types created in
	 * {@link #onCreate(Bundle)} are able to have their views recycled.
	 */
	private void postBindPreferences() {
	    if (mHandler.hasMessages(MSG_BIND_PREFERENCES)) return;
	    mHandler.obtainMessage(MSG_BIND_PREFERENCES).sendToTarget();
	}
	
	private void bindPreferences() {
	    final PreferenceScreen preferenceScreen = getPreferenceScreen();
	    if (preferenceScreen != null) {
	        preferenceScreen.bind(mListView);
	        if (mSavedInstanceState != null) {
	            super.onRestoreInstanceState(mSavedInstanceState);
	            mSavedInstanceState = null;
	        }
	    }
	}
	
	/**
	 * Creates the {@link PreferenceManager}.
	 * @return The {@link PreferenceManager} used by this activity.
	 */
	private PreferenceManager onCreatePreferenceManager() {
	    //PreferenceManager preferenceManager = new PreferenceManager(this, FIRST_REQUEST_CODE);
	    //PreferenceManager preferenceManager = new PreferenceManager(getContext());
	    //preferenceManager.setOnPreferenceTreeClickListener(this);
	    //return preferenceManager;
		return null;
	}
	
	/**
	 * Returns the {@link PreferenceManager} used by this activity.
	 * @return The {@link PreferenceManager}.
	 */
	public PreferenceManager getPreferenceManager() {
	    return mPreferenceManager;
	}
	
	private void requirePreferenceManager() {
	    if (mPreferenceManager == null) {
	        throw new RuntimeException("This should be called after super.onCreate.");
	    }
	}
	
	/**
	 * Sets the root of the preference hierarchy that this activity is showing.
	 * 
	 * @param preferenceScreen The root {@link PreferenceScreen} of the preference hierarchy.
	 */
	public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
	    //if (mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
	        postBindPreferences();
	        CharSequence title = getPreferenceScreen().getTitle();
	        // Set the title of the activity
	        //if (title != null) {
	        //    setTitle(title);
	        //}
	    //}
	}
	
	/**
	 * Gets the root of the preference hierarchy that this activity is showing.
	 * 
	 * @return The {@link PreferenceScreen} that is the root of the preference
	 *         hierarchy.
	 */
	public PreferenceScreen getPreferenceScreen() {
	    //return mPreferenceManager.getPreferenceScreen();
		return null;
	}
	
	/**
	 * Inflates the given XML resource and adds the preference hierarchy to the current
	 * preference hierarchy.
	 * 
	 * @param preferencesResId The XML resource ID to inflate.
	 */
	public void addPreferencesFromResource(int preferencesResId) {
	    requirePreferenceManager();
	    //setPreferenceScreen(mPreferenceManager.inflateFromResource(this, 
	    //			preferencesResId, getPreferenceScreen()));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	    return false;
	}
	
	/**
	 * Finds a {@link Preference} based on its key.
	 * 
	 * @param key The key of the preference to retrieve.
	 * @return The {@link Preference} with the key, or null.
	 * @see PreferenceGroup#findPreference(CharSequence)
	 */
	public Preference findPreference(CharSequence key) {
	    
	    if (mPreferenceManager == null) {
	        return null;
	    }
    
	    return mPreferenceManager.findPreference(key);
	}

	protected void onNewIntent(Intent intent) {
	    //if (mPreferenceManager != null) {
	    //    mPreferenceManager.dispatchNewIntent(intent);
	    //}
	}

}
