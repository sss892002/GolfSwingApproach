package com.example.golfswingapproach2;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int REQUEST_CONNECT_DEVICE=1;
	private static final int REQUEST_ENABLE_BT=2;
	
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothChatService mChatService = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter==null){
			Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		
	}
public void onstart(){
	super.onStart();
	if (!mBluetoothAdapter.isEnabled()) {
		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
		Toast.makeText(this, "Bluetooth is not on", Toast.LENGTH_LONG).show();
	} 
}



public void onActivityResult(int requestCode, int resultCode, Intent data){
	switch (requestCode){
	case REQUEST_CONNECT_DEVICE:
	case REQUEST_ENABLE_BT:
	if (resultCode==Activity.RESULT_OK){
	setupChat();	
	}
	else {
		Toast.makeText(this, "Bluetooth_not_enabled_", Toast.LENGTH_SHORT).show();
		finish();
	}
	}
	
}
private void ensureDiscoverable(){
	if (mBluetoothAdapter.getScanMode()!=BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
		startActivity(discoverableIntent);
	}
	
}


	private void setupChat() {
	// TODO Auto-generated method stub
	
}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
