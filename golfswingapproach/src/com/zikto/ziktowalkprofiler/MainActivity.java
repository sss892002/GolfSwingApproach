package com.zikto.ziktowalkprofiler;

import android.os.Bundle;

import android.content.Intent;

import com.zikto.ziktowalkprofiler.R;
import com.zikto.ziktowalkprofiler.fragments.MeasureFragment;
import com.zikto.ziktowalkprofiler.fragments.MainFragment;
import com.zikto.invensense.BluetoothModule;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import android.widget.EditText;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;


public class MainActivity extends FragmentActivity {
	private TextView out;
	
	private InvensenseManager invenManager;
	
	 /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;
    private MeasureFragment measureFragment;
    private MainFragment mainFragment;

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

		//Remove Titlebar
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
//		setContentView(R.layout.activity_main);
	    
	    setContentView(R.layout.activity_screen_slide);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        
        
//		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//		StrictMode.setThreadPolicy(policy); 

	}
	
	
	@Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

	public void checkBTState()
	{
		if(!BluetoothModule.getInstance().isReady())
		{
			BluetoothModule.getInstance().getAdapter();
			//Prompt user to turn on Bluetooth
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, BluetoothModule.REQUEST_ENABLE_BT);
		}
	}



	public void startInvensenseSensor()
	{
		checkBTState();
		//invenManager = new InvensenseManager(this, plotManager);
		invenManager.start();
	}

	public void stopInvensenseSensor()
	{
		if(invenManager!=null)
		{
			invenManager.stop();
		}
	}

	public void clearAllField()
	{
		final EditText editPelvic = (EditText)findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)findViewById(R.id.editComments);
		final EditText editAge  = (EditText)findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)findViewById(R.id.editHeight);
		final EditText edit=(EditText)findViewById(R.id.editText1);
		
		editPelvic.getText().clear();
		editComment.getText().clear();
		editAge.getText().clear();
		editWeight.getText().clear();
		editHeight.getText().clear();
		edit.getText().clear();
	}

	public void AlertBox( String title, String message ){
		new AlertDialog.Builder(this)
		.setTitle( title )
		.setMessage( message ).show();
	}
	
	public boolean ValidateInputs()
	{
		final EditText editPelvic = (EditText)findViewById(R.id.editPelvic);
		final EditText editComment = (EditText)findViewById(R.id.editComments);
		final EditText editAge  = (EditText)findViewById(R.id.editAge);
		final EditText editWeight  = (EditText)findViewById(R.id.editWeight);
		final EditText editHeight  = (EditText)findViewById(R.id.editHeight);
		final EditText edit=(EditText)findViewById(R.id.editText1);
		
		String filename = edit.getText().toString(); 
		String pelvicRotation = editPelvic.getText().toString();
		String meta = editComment.getText().toString();
		String age = editAge.getText().toString();
		String height = editHeight.getText().toString();
		String weight = editWeight.getText().toString();
		
		if(filename.isEmpty())
		{
			AlertBox("Sorry", "Enter Name.");
			return false;
		}
		
		if(pelvicRotation.isEmpty())
		{
			AlertBox("Sorry", "Enter Pelvic Rotation Data.");
			return false;
		}
		
		if(age.isEmpty())
		{
			AlertBox("Sorry", "Enter Age.");
			return false;
		}
		
		if(weight.isEmpty())
		{
			AlertBox("Sorry", "Enter Weight.");
			return false;
		}
		if(height.isEmpty())
		{
			AlertBox("Sorry", "Enter Height.");
			return false;
		}
		
		return true;
	}
	
	public MainFragment getMainFragment()
	{
		return mainFragment;
	}

	
	public final BroadcastReceiver mReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			String action = intent.getAction();

			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//	  			
				if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){
					//TODO : find and MAC 
					out.append(btDevice.getName()+"\n"+btDevice.getAddress());
					//((ArrayAdapter<String>) mNewDevicesArrayAdapter).add(btDevice.getName()+"\n"+btDevice.getAddress());
				}
				out.append(btDevice.getName()+"\n"+btDevice.getAddress());
				out.append("ACTION_FOUND\n");
			}
			else{

				out.append("ACTION_DISCOVERY\n");
			}
		}
	};
	
	 /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	switch(position) {
      
        	case 0: 
        		mainFragment = new MainFragment();
        		return mainFragment;
            case 1:
            	measureFragment = new MeasureFragment();
            	return measureFragment;
            default: 
            	mainFragment = new MainFragment();
        		return mainFragment;
            }
        	
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    };
	
}



