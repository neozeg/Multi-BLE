package com.mupro.multi_ble;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.mupro.multi_ble.BleAppManager.BLEManageListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanDeviceActivity extends BleActivity implements BLEManageListener{
	private final static String TAG = "ScanDeviceActivity";

    public final static String STRING_NAME = "name";
    public final static String STRING_RSSI = "RSSI";
    public final static String STRING_ADDRESS = "address";
    
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private SimpleAdapter mSimpleAdapter;
    private ArrayList<String> mDeviceList;
	//private ArrayList<String> mDeviceNameList;
	private List<Map<String,Object>> mList;

    private int mDevicePositionNumber;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_scan_device);
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "this device do not support Bluetooth 4.0", Toast.LENGTH_SHORT).show();
            finish();
        }

        mDevicePositionNumber = getIntent().getIntExtra(bleApp.manager.STRING_BLE_DEVICE_POS,0);
        Log.v(TAG,"mDevicePositionNumber = "+mDevicePositionNumber);
        
        
        if(bleApp.manager.bleDevices[mDevicePositionNumber] !=null){
        	bleApp.manager.bleDevices[mDevicePositionNumber].disconnectDevice();
        	bleApp.manager.bleDevices[mDevicePositionNumber] = null;
        }
        bleApp.manager.setBLEManagerListener(this);
        
        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				bleApp.manager.stopScanBluetoothDevice();
            	bleApp.manager.startScanBluetoothDevice();
            }
        });

        // one for newly discovered devices
        //mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        mList = new ArrayList<Map<String,Object>>();
        mList.clear();
        mSimpleAdapter = new SimpleAdapter(this,mList,R.layout.list_item,
        		new String[]{STRING_NAME,STRING_RSSI,STRING_ADDRESS},
        		new int[]{R.id.deviceName,R.id.deviceRSSI,R.id.deviceAddress});
        
        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        //newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setAdapter(mSimpleAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        mDeviceList = new ArrayList<String>();
        mNewDevicesArrayAdapter.clear();
        mDeviceList.clear();
    	bleApp.manager.startScanBluetoothDevice();
	}
	/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan_device, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

	@Override
	public void BLEManageListener(BluetoothDevice device, int rssi,
			byte[] scanRecord) {
		// TODO Auto-generated method stub
		Log.v(TAG, "DeviceFound");
    	int  device_rssi = rssi;
        	if(!mDeviceList.contains(device.getAddress())){
            	Map<String,Object> item = new HashMap<String,Object>();
            	mNewDevicesArrayAdapter.add(device.getName()+"\t\t\t("+device_rssi+"dB)\n"+device.getAddress());
            	mDeviceList.add(device.getAddress());
            	
            	item.put(STRING_NAME, device.getName());
            	item.put(STRING_RSSI, Integer.toString(device_rssi)+"dBm");
            	item.put(STRING_ADDRESS, device.getAddress());
            	mList.add(item);
            	mSimpleAdapter.notifyDataSetChanged();
        	}else{
        		int position = mDeviceList.indexOf(device.getAddress());
        		HashMap<String,Object> item = (HashMap<String,Object>)mSimpleAdapter.getItem(position);
            	item.put(STRING_RSSI, Integer.toString(device_rssi)+"dBm");
            	mSimpleAdapter.notifyDataSetChanged();
        	}
    
	}

	@Override
	public void BLEManageStartScan() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void BLEManageStopScan() {
		// TODO Auto-generated method stub
		
	}
	

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            
            // Set result and finish this Activity
            Intent data = new Intent();
            data.putExtra(bleApp.manager.STRING_BLE_DEVICE_POS,mDevicePositionNumber);
            setResult(Activity.RESULT_OK,data);
        	String address = mDeviceList.get(position);
			bleApp.manager.setBleDevice(address,mDevicePositionNumber);

            finish();
        }
    };
	
}
