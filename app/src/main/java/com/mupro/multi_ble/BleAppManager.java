package com.mupro.multi_ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;

public class BleAppManager {
	
	private static final int REQUEST_CODE = 0x01;

    private static final long SCAN_PERIOD = 120000;

	public static final int MAX_DEVICE_NUM = 3;
	public static final String STRING_BLE_DEVICE_POS = "ble_device_pos";
	public static final String STRING_BLE_ADDRESS = "ble_device_address";
    
	private Context mContext;
	private BluetoothAdapter mBluetoothAdapter;
	//public BleDevice bleDevice = null;
	public BleDevice[] bleDevices = null;
	//private String bleDeviceName = "";

	private Handler handler = null;
	private boolean isScanning = false; // �Ƿ�����ɨ��

	private BLEManageListener listener = null;
	
	private ArrayList<BluetoothDevice> scanBlueDeviceArray = new ArrayList<BluetoothDevice>(); // ɨ�赽�����
	
	public BleAppManager(Context context){

		mContext = context;
		handler = new Handler();
	 // Use this check to determine whether BLE is supported on the device.  Then you can
     // selectively disable BLE-related features.
     if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
         Toast.makeText(mContext, "this device do not support Bluetooth 4.0", Toast.LENGTH_SHORT).show();
         return;
     }

     // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
     // BluetoothAdapter through BluetoothManager.
     final BluetoothManager bluetoothManager =
             (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
     mBluetoothAdapter = bluetoothManager.getAdapter();
     if(mBluetoothAdapter==null){
         Toast.makeText(mContext, "this device do not support Bluetooth 4.0", Toast.LENGTH_SHORT).show();
         return;
     }
		bleDevices = new BleDevice[MAX_DEVICE_NUM];
		
	}
	/**
	 * �ж��Ƿ�������Ȩ��
	 * 
	 * @return
	 */
	public boolean isEnabled(Activity activity) {
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				activity.startActivityForResult(enableBtIntent, REQUEST_CODE);
			}
			return true;
		}
		return false;
	}
	public void setBleDevice(String address,int pos){
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		bleDevices[pos] = new BleDevice(mContext,device,pos);
	}
	
	/**
	 * ����Ȩ�޺󣬷���ʱ����
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	public void onRequestResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_CODE
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mContext).finish();
			return;
		}
	}

	/**
	 * ɨ�������豸
	 */
	public void startScanBluetoothDevice() {
		if (scanBlueDeviceArray != null) {
			scanBlueDeviceArray = null;
		}
		scanBlueDeviceArray = new ArrayList<BluetoothDevice>();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				stopScanBluetoothDevice();
			}
		}, SCAN_PERIOD); // 10���ֹͣɨ��
		isScanning = true;

		mBluetoothAdapter.startLeScan(bleScanCallback);
		listener.BLEManageStartScan();
	}

	/**
	 * ֹͣɨ�������豸
	 */
	public void stopScanBluetoothDevice() {
		if (isScanning) {
			isScanning = false;
			mBluetoothAdapter.stopLeScan(bleScanCallback);
			listener.BLEManageStopScan();
		}
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				final byte[] scanRecord) {
			// TODO ���ɨ�赽��device����ˢ�����
			handler.post(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub

					if (!scanBlueDeviceArray.contains(device)) {
						scanBlueDeviceArray.add(device);

						listener.BLEManageListener(device, rssi,
								scanRecord);
					}
				}
			});
		}
	};

	/**
	 * ÿɨ�赽һ�������豸����һ��
	 * 
	 * @param listener
	 */
	public void setBLEManagerListener(BLEManageListener listener) {
		this.listener = listener;
	}

	/**
	 * ���ڴ��?ˢ�µ��豸ʱ���½���
	 * 
	 * @author Kevin.wu
	 * 
	 */
	public interface BLEManageListener {
		public void BLEManageListener(BluetoothDevice device, int rssi,
									  byte[] scanRecord);

		public void BLEManageStartScan();

		public void BLEManageStopScan();
	}
	
}
