package com.mupro.multi_ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class BleDevice {

	private final static String TAG = "BleDevice";
	
	//private String BleTxServiceName = SampleGattAttributes.BLE_SENDDATA_SERVICE;
	//private String BleTxCharaName = SampleGattAttributes.BLE_SENDDATA_CHARACTERISTIC;
	//private String BleRxServiceName = SampleGattAttributes.BLE_RECVDATA_SERVICE;
	//private String BleRxCharaName = SampleGattAttributes.BLE_RECVDATA_CHARACTERISTIC;
	

    //private BluetoothGattService mBleTxService,mBleRxService;
    //private BluetoothGattCharacteristic mBleTxCharacterisitc;
    //private BluetoothGattCharacteristic mBleRxCharacterisitc;
	
    private Intent mGattServiceIntent;
    
	private Context mContext;
	private String mDeviceName,mDeviceAddress;
	private BluetoothLeService mBluetoothLeService;
	private BleBroadcastReceiver mBleBroadcastReceiver;

	private int BleDeviceId;
	public BluetoothDevice mDevice = null;

	public BleDevice(Context context,BluetoothDevice device,int deviceId){
		mContext = context;
		mDevice = device;
		mDeviceAddress = device.getAddress();
		mDeviceName = device.getName();
		BleDeviceId = deviceId;
		registerReceiver();
		bindService();
        
	}

	public String getAddress(){
		return mDeviceAddress; 
	}
	public String getName(){
		return mDeviceName;
	}
	
	/*
	public BleDevice(Context context, BluetoothDevice device){
		mDeviceName = device.getName();
		mDeviceAddress = device.getAddress();
		mContext = context;
		
		registerReceiver();
		bindService();
        
	}*/
	
	public void registerReceiver(){
		mContext.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());		
	}
	
	public void unregisterReceiver(){
		mContext.unregisterReceiver(mGattUpdateReceiver);
	}
	
	public void bindService(){
		mGattServiceIntent = new Intent(mContext, BluetoothLeService.class);
		mContext.bindService(mGattServiceIntent, mServiceConnection, mContext.BIND_AUTO_CREATE);
	}
    
	public void unbindService(){
		mContext.unbindService(mServiceConnection);
	}
	
	/*
	public void connectDevice(Intent data){
		mDeviceName = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME, null);
		mDeviceAddress = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS, null);

		if(mDeviceAddress == null)
			return;
        
    	if(!mBluetoothLeService.initialize()){
            Log.e(TAG, "Unable to initialize Bluetooth");
            return;
    	}
    	if(mDeviceAddress != null){
    		boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);    		
    	}		
	}
	*/
	public void connectDevice(String address){
		mDeviceAddress = address;
    	if(!mBluetoothLeService.initialize()){
            Log.e(TAG, "Unable to initialize Bluetooth");
            return;
    	}
    	if(mDeviceAddress != null){
    		boolean result = mBluetoothLeService.connect(mDeviceAddress,BleDeviceId);
            Log.d(TAG, "Connect request result=" + result);
    	}	
	}
	
	public void disconnectDevice(){
        if(mBluetoothLeService!=null){
        	mBluetoothLeService.disconnect(BleDeviceId);
        }
	}


	/**
	 * ��ȡ����ֵ
	 * 
	 * @param characteristic
	 */
	public void readValue(BluetoothGattCharacteristic characteristic) {
		if (characteristic == null) {
			Log.w(TAG, "55555555555 readValue characteristic is null");
		} else {

			mBluetoothLeService.readCharacteristic(characteristic,BleDeviceId);

		}
	}

	/**
	 * �������ֵд�����
	 * 
	 * @param characteristic
	 */
	public void writeValue(BluetoothGattCharacteristic characteristic,byte[] data) {
		if (characteristic == null) {
			Log.w(TAG, "55555555555 writeValue characteristic is null");
		} else {
			Log.d(TAG, "charaterUUID write is success  : "
					+ characteristic.getUuid().toString());
			mBluetoothLeService.sendCharacteristicData(characteristic, data,BleDeviceId);
		}
	}
	

	public void writeValue(String serviceUUID,String charaUUID,byte[] data) {
		for(BluetoothGattService gattService:mBluetoothLeService.getSupportedGattServices(BleDeviceId)){
			if(gattService.getUuid().toString().contains(serviceUUID)){
				for(BluetoothGattCharacteristic gattCharacteristic:gattService.getCharacteristics()){
					if(gattCharacteristic.getUuid().toString().contains(charaUUID)){
						mBluetoothLeService.sendCharacteristicData(gattCharacteristic, data,BleDeviceId);
					}
				}
			}
		}		
	}
	
	public boolean setAllCharacteristicNotification(boolean enabled){
		for(BluetoothGattService gattService:mBluetoothLeService.getSupportedGattServices(BleDeviceId)){
				for(BluetoothGattCharacteristic gattCharacteristic:gattService.getCharacteristics()){
					int prop = gattCharacteristic.getProperties();
					if(prop == BluetoothGattCharacteristic.PROPERTY_NOTIFY){
						return mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, enabled,BleDeviceId);
					}
				}
		}
		
		return false;
		
	}
	
	public boolean setCharacteristicNotification(String serviceUUID,String charaUUID,boolean enabled){
		for(BluetoothGattService gattService:mBluetoothLeService.getSupportedGattServices(BleDeviceId)){
			if(gattService.getUuid().toString().contains(serviceUUID)){
				for(BluetoothGattCharacteristic gattCharacteristic:gattService.getCharacteristics()){
					if(gattCharacteristic.getUuid().toString().contains(charaUUID)){
						return mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, enabled,BleDeviceId);
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled){
		if(characteristic != null){
			return mBluetoothLeService.setCharacteristicNotification(characteristic, enabled,BleDeviceId);
		}
		return false;
	}
	
	public void disableGattNotification(BluetoothGattCharacteristic characterisitc){
		if(characterisitc!=null){
	    	setCharacteristicNotification(characterisitc, false);
	    	try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public boolean enableGattNotification(BluetoothGattCharacteristic characterisitc){
		if(characterisitc!=null){
	    	return setCharacteristicNotification(characterisitc, true);
		}
		return false;
	}
	
	public void setBleBroadcastReceiver(BleBroadcastReceiver bleBroadcastReceiver){
		mBleBroadcastReceiver = bleBroadcastReceiver;
	}
	
	public interface BleBroadcastReceiver{
		public void onReceive(Context context, Intent intent, String uuid);
	}
	

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////
	
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        return intentFilter;
    }

	

	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
	    	mBluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
	    	if(!mBluetoothLeService.initialize()){
                Log.e(TAG, "Unable to initialize Bluetooth");
	    	}
	    	if(mDeviceAddress != null){
	    		mBluetoothLeService.connect(mDeviceAddress,BleDeviceId);
	    	}
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
		}
    	
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String uuid = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);
        	
            final String action = intent.getAction();
        	String address = intent.getStringExtra(BluetoothLeService.EXTRA_ADDRESS);
        	
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.i(TAG, "ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.i(TAG, "ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                //getGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //Log.i(TAG, "ACTION_DATA_AVAILABLE");
            } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                //Log.i(TAG, "ACTION_DATA_WRITE");
            	//Log.v(TAG,"BleDeviceId="+BleDeviceId);
            }
			//Log.v(TAG,"BleDeviceId="+BleDeviceId);
            if(address.equalsIgnoreCase(mDeviceAddress)){
    			intent.putExtra(BleAppManager.STRING_BLE_DEVICE_POS,BleDeviceId);
    			intent.putExtra(BleAppManager.STRING_BLE_ADDRESS,mDeviceAddress);
                mBleBroadcastReceiver.onReceive(context, intent, uuid);
            }
            
        }
    };

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void getGattServices(List<BluetoothGattService> gattServices) {
    	if(gattServices == null)
    		return;
        /*
    	for(BluetoothGattService gattService:gattServices){
			if(gattService.getUuid().toString().equalsIgnoreCase(BleTxServiceName)){
				mBleTxService = gattService; 
			}
			if(gattService.getUuid().toString().equalsIgnoreCase(BleTxServiceName)){
				mBleRxService = gattService; 
			}
    		for(BluetoothGattCharacteristic characteristic:gattService.getCharacteristics()){
    			//int charaProp = characteristic.getProperties();
    			if(characteristic.getUuid().toString().equalsIgnoreCase(BleTxCharaName)){
    				mBleTxCharacterisitc = characteristic;
    			}
    			if(characteristic.getUuid().toString().equalsIgnoreCase(BleRxCharaName)){
    				mBleRxCharacterisitc = characteristic;
    			}
    		} 
    	}
    	*/
    	
    }
    

    
}
