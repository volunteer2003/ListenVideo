package com.lxb.window;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class Floating_windowActivity extends Activity implements OnClickListener {
	
	private Button btn_show;
	private Button btn_manual;
	private TextView tv_manual;
	private Button btn_exit;
	private Intent intent;
	private int manualShowFlag = 0;
	private int floatButtonShowFlag = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        if (isServiceRunning()) {        	
        	Log.d("Debug", "!!! OK, isServiceStarted return [True]!!!");
        } 
                
        setContentView(R.layout.main);
        
        btn_show   = (Button) findViewById(R.id.btn_show);
        
        btn_manual = (Button) findViewById(R.id.btn_manual);
        tv_manual  = (TextView) findViewById(R.id.tv_manual);
        btn_exit = (Button) findViewById(R.id.btn_exit);
        
        tv_manual.setText("");
        manualShowFlag = 0;
        
        btn_show.setOnClickListener(this);
       
        btn_manual.setOnClickListener(this);
        btn_exit.setOnClickListener(this); 
      
    }
    
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        	Log.d("Debug", "!!! OK, ServiceName return [" + service.service.getClassName() + "]!!!");
            if ("com.example.MyService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    
    public static boolean isServiceStarted(Context context,String PackageName)
    {
        boolean isStarted =false;
        try
        {
            ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
            int intGetTastCounter = 1000;
            List<ActivityManager.RunningServiceInfo> mRunningService = 
                      mActivityManager.getRunningServices(intGetTastCounter );
            for (ActivityManager.RunningServiceInfo amService : mRunningService)
            {
            		//Log.d("Debug", "!!! OK, ServiceName return [" + amService.service.getPackageName() + "]!!!");
                    if(0 == amService.service.getPackageName().compareTo(PackageName))
                    {                
                            isStarted = true;
                            break;
                    }
            }
        }
        catch(SecurityException e)
        {
                e.printStackTrace();
        }            
        return isStarted;                
    }   
    
    
    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
    	if (keyCode == KeyEvent.KEYCODE_BACK) {	
    		Log.d("Debug", "!!! OK, Cancle KeyEvent.KEYCODE_BACK!!!");
    	}
    	return true;
    }
        
	public void onClick(View v) {

		intent = new Intent(this, FloatingWindowService.class);
		Bundle bundle = new Bundle();  
		
		switch(v.getId()) {
			case R.id.btn_show:	
				floatButtonShowFlag ++;
				
				if (floatButtonShowFlag%2 != 0) {
					bundle.putInt(FloatingWindowService.OPERATION, FloatingWindowService.OPERATION_SHOW);   
					btn_show.setText("Hide Floating Spirit");
				} else {
					bundle.putInt(FloatingWindowService.OPERATION, FloatingWindowService.OPERATION_HIDE);  
					btn_show.setText("Show Floating Spirit");
				}
				    
				intent.putExtras(bundle);  	          
				startService(intent); 
				break;
			case R.id.btn_manual:
				manualShowFlag ++;
				
				if (manualShowFlag%2 != 0) {
					tv_manual.setText(R.string.manual);
					btn_manual.setText("Hide User Manual");
				} else {
					tv_manual.setText("");	
					btn_manual.setText("User Manual");
				}
				   						
				break;		
			case R.id.btn_exit: 
				bundle.putInt(FloatingWindowService.OPERATION, FloatingWindowService.OPERATION_EXIT);  
				intent.putExtras(bundle);
				startService(intent);  
				this.finish();  			
				break;             
		}
	}
	
	@Override  
	public void onDestroy(){  
		super.onDestroy();
		if(intent != null){  
			stopService(intent);  
	    }  
	}  
}  

