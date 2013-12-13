package com.lxb.window;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;

import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;

@SuppressLint("HandlerLeak")
public class FloatingWindowService extends Service {
	
	public static final String OPERATION = "operation";
	public static final int OPERATION_SHOW = 100;
	public static final int OPERATION_HIDE = 101;
	public static final int OPERATION_EXIT = -1;
	
	private static final int HANDLE_CHECK_ACTIVITY = 200;
	public static final String CONTEXT = null;
	
	private boolean isAdded = false; // the spirit is exist or not
	private static WindowManager wm;
	private static WindowManager.LayoutParams params;
	private Button btn_floatView;
	private int screenBrightnessInit;
	private boolean automicBrightnessFlag;
	
	private String screenLockStatus = "000";
	private int clickCounter = 0;
	private long longClickDownUnixTime = 0;
	private long longClickUpUnixTime = 0;
	
	private long longClickVolumeDownUnixTime = 0;
	private long longClickVolumeUpUnixTime = 0;
	
	private boolean screenLockFlag = true;
	private boolean initFlag = true;
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	    getHomes();
		createFloatView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    switch (keyCode) {
	        case KeyEvent.KEYCODE_BACK:
	        Log.d("Debug", "!!! OK, find KEYCODE_BACK!!!");	
	        return true;
	    }
	    
	    Log.d("Debug", "!!! OK, find ********!!!");	
	    return true;
	    //return super.onKeyDown(keyCode, event);
	}	
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		int operation = intent.getIntExtra(OPERATION, OPERATION_SHOW);
		
		//Log.d("Debug", "!!! OK, find operation ID [" + operation + "], and isAdded is [" + isAdded + "]!!!");
		
		switch(operation) {
		case OPERATION_SHOW:
			mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
			mHandler.sendEmptyMessage(HANDLE_CHECK_ACTIVITY);
			break;
		case OPERATION_HIDE:
			// hide just when it exists
			if (isAdded){
				mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
				wm.removeView(btn_floatView);
				isAdded = false;
				break;
			}
		case OPERATION_EXIT:
			// exit just when it exists
			if (isAdded) {
				mHandler.removeMessages(HANDLE_CHECK_ACTIVITY);
				wm.removeView(btn_floatView);				
				isAdded = false;
			}
			break;				
		}	
	}
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case HANDLE_CHECK_ACTIVITY:
				if(isHome()) {
					if(!isAdded) {
						wm.addView(btn_floatView, params);
						isAdded = true;
					}
				} else {
					if(isAdded) {
						wm.removeView(btn_floatView);
						isAdded = false;
					}
				}
				mHandler.sendEmptyMessageDelayed(HANDLE_CHECK_ACTIVITY, 1000);
				break;
			}
		}
	};
	
	/**
	 * Create the spirit
	 */
	private void createFloatView() {
		btn_floatView = new Button(getApplicationContext());
        btn_floatView.getBackground().setAlpha(150);
        btn_floatView.setBackgroundResource(R.drawable.unlock_button);     
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
 
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // config window type
        params.format = PixelFormat.RGBA_8888; // config pic style
        params.format = PixelFormat.TRANSPARENT;
        
        Log.d("Debug", "!!! initFlag [" + initFlag +"] !!!"); 
        if (initFlag) {        	
        	params.flags = params.flags|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        	params.flags = params.flags|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; 
        	initFlag = false;
        }
        
        params.flags = params.flags|FLAG_HOMEKEY_DISPATCHED; 
 
        // configure the size of the spirit
        params.width  = 40;
        params.height = 40;
                
        btn_floatView.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				
				if (  keyCode == KeyEvent.KEYCODE_BACK 
				   || keyCode == KeyEvent.KEYCODE_MENU ) {					
					screenLockStatus = "000";
		    	}
		    			
				if ( keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_UP ) {
					if (screenLockStatus == "000") {
						screenLockStatus = "001";						
					} else {
						screenLockStatus = "000";
					}
					
					Calendar calendar1 = Calendar.getInstance();
					longClickVolumeUpUnixTime = calendar1.getTimeInMillis();
				}
				
				if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.getAction() == KeyEvent.ACTION_UP ) {
					Calendar calendar1 = Calendar.getInstance();
					longClickVolumeDownUnixTime = calendar1.getTimeInMillis();
					
					long clickInterval = longClickVolumeDownUnixTime - longClickVolumeUpUnixTime;
					
					// check long click action 
					if ( clickInterval > 500 && clickInterval < 1500) {
						if (screenLockStatus == "001") {
							screenLockStatus = "111";												
							clickCounter++;	
							changeOperateMode();
							
							//update the location of the spirit
							wm.updateViewLayout(btn_floatView, params);
							
							longClickUpUnixTime = 0;
							longClickDownUnixTime = 0;
							screenLockStatus = "000";
							
						} else {
							screenLockStatus = "000";
						}						
					}
				}
	
				String eventStr = event.toString();											
				Log.d("Debug", "!!! ACTION_UP with[" + eventStr +"] !!!"); 
				return true;
			}        	
					
        });
             
        // configure the Listener of the spirit
        btn_floatView.setOnTouchListener(new OnTouchListener() {
        	int lastX, lastY;
        	int paramX, paramY;        	
        	
			public boolean onTouch(View v, MotionEvent event) {
				switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					paramX = params.x;
					paramY = params.y;									
				
					Calendar calendar = Calendar.getInstance();
					longClickDownUnixTime = calendar.getTimeInMillis();
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = (int) event.getRawY() - lastY;
					params.x = paramX + dx;
					params.y = paramY + dy;
										
			        wm.updateViewLayout(btn_floatView, params);
					break;
				case MotionEvent.ACTION_UP:
					// Cancle srceen action when the srceen locked
					if (clickCounter > 0 && clickCounter%2 != 0) {											
						break;											
					}
										
					int dx_up = (int) event.getRawX() - lastX;
					int dy_up = (int) event.getRawY() - lastY;
					
					if (dx_up < 10 && dy_up < 10) {
						// Click event check for screenLockStatus					
						Calendar calendar1 = Calendar.getInstance();
						longClickUpUnixTime = calendar1.getTimeInMillis();
						long longClickInterval = longClickUpUnixTime - longClickDownUnixTime;
					
						// Check the long click action
						if (longClickInterval > 500 && longClickInterval < 1500) {
							clickCounter++;																			
						
							changeOperateMode();
						}
						
						// update the location of the spirit
				        wm.updateViewLayout(btn_floatView, params);
				       
				        longClickUpUnixTime = 0;
				        longClickDownUnixTime = 0;
					}
					break;				
				}
				
				screenLockStatus = "000";
				return true;
			}
		});   
        wm.addView(btn_floatView, params);
        isAdded = true;
     }
	
	@SuppressLint("Wakelock")
	protected void changeOperateMode() {
		// TODO Auto-generated method stub
		if (!screenLockFlag) {			
			//active screen
        	params.flags = params.flags|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        	params.flags = params.flags|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE; 
        	btn_floatView.setBackgroundResource(R.drawable.unlock_button);   
        	screenLockFlag = true;
        } else {        	
        	// only longClick action valid when the screenLock
        	params.flags = params.flags&~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        	params.flags = params.flags&~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        	btn_floatView.setBackgroundResource(R.drawable.lock_button);   
       		screenLockFlag = false;
        }
		changeScreenSetting(0);		
	}
	
	
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();  
	    PackageManager packageManager = this.getPackageManager();  
	      
	    Intent intent = new Intent(Intent.ACTION_MAIN);  
	    intent.addCategory(Intent.CATEGORY_HOME);  
	    List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,  
	            PackageManager.MATCH_DEFAULT_ONLY);  
	    for(ResolveInfo ri : resolveInfo) {  
	        names.add(ri.activityInfo.packageName);  
	    }
	    return names;  
	}
	
	public boolean isHome(){ 
		// no need to check it, should show the staff all the time 
		return true;
	}

	
	protected void onPause() {
		
		 Log.d("Debug", "!!! 1-ACTION_SCREEN_OFF, and over the service !!!");
        
        //super.onPause();
        rollbackScreenSetting();
    }

    protected void onResume() {
     
        //super.onResume();
        rollbackScreenSetting();
    }

	
    private void changeScreenSetting(int paramInt){
    	
    	boolean automicBrightness = false;     
    	try {         
    		automicBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;     
    	} catch (SettingNotFoundException e) { 
    		e.printStackTrace();     
    	}    
    	
    	automicBrightnessFlag = automicBrightness;
    	if (automicBrightness == true) {
    		Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL); 
    	} else {
    		try{
        		screenBrightnessInit = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        	}
        	catch (Exception localException){
        		//
        	}
    	}
    		
    	try{	
     		Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 15);    
    		//Settings.System.putInt(getContentResolver(), Settings.System.DIM_SCREEN, 15);    
    		Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
    	}
    	catch (Exception localException){
    		localException.printStackTrace();
    	}
    	
    	Log.d("Debug", "!!! In saveScreenBrightness screenBrightness[" + paramInt + "]!!!");
    	
    }
       
    private void rollbackScreenSetting(){
    	if (automicBrightnessFlag) {    		
    		Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE,Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC); 
    	} else {    		    		    	
    		try{	
    			Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessInit);           		 
        		Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
        	}
        	catch (Exception localException){
        		localException.printStackTrace();
        	}
    	}
    	
    }
    
}
