/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mupro.multi_ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    //private String mBluetoothDeviceAddress;
    //private BluetoothGatt mBluetoothGatt;
    //private int mConnectionState = STATE_DISCONNECTED;
    private String[] mBluetoothDeviceAddresses;
    private BluetoothGatt[] mBluetoothGatts;
    private int[] mConnectionStates;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_DATA_WRITE =
            "com.example.bluetooth.le.ACTION_DATA_WRITE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String EXTRA_STATUS =
            "com.example.bluetooth.le.EXTRA_STATUS";
    public final static String EXTRA_UUID = "com.example.bluetooth.le.EXTRA_UUID";
    public final static String EXTRA_ADDRESS = "ble_device_address";


    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    /**/
    private BluetoothGattCallback[] mGattCallbacks;
    private void createGattCallbacks(){
        mGattCallbacks = new BluetoothGattCallback[BleAppManager.MAX_DEVICE_NUM];
        for(int i = 0;i<mBluetoothGatts.length;i++){
            final int pos = i;
           mGattCallbacks[i] = new BluetoothGattCallback() {
               @Override
               public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                   String intentAction;
                   if(status == BluetoothGatt.GATT_SUCCESS){
                       if (newState == BluetoothProfile.STATE_CONNECTED) {
                           intentAction = ACTION_GATT_CONNECTED;
                           mConnectionStates[pos] = STATE_CONNECTED;
                           broadcastUpdate(intentAction,gatt.getDevice().getAddress());
                           Log.i(TAG, "Connected to GATT server.");
                           // Attempts to discover services after successful connection.
                           Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatts[pos].discoverServices());

                       } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                           intentAction = ACTION_GATT_DISCONNECTED;
                           mConnectionStates[pos] = STATE_DISCONNECTED;
                           Log.i(TAG, "Disconnected from GATT server.");
                           broadcastUpdate(intentAction,gatt.getDevice().getAddress());
                       }
                   }
               }

               @Override
               public void onCharacteristicWrite(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic, int status) {
                   //super.onCharacteristicWrite(gatt, characteristic, status);
                   broacastUpdate(ACTION_DATA_WRITE,status,gatt.getDevice().getAddress());
                   Log.w(TAG, "onCharacteristicWrite: " + status+ ",\tPos="+pos);
               }

               @Override
               public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                   if (status == BluetoothGatt.GATT_SUCCESS) {
                       broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getDevice().getAddress());
                   } else {
                       Log.w(TAG, "onServicesDiscovered received: " + status);
                   }
               }

               @Override
               public void onCharacteristicRead(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic,
                                                int status) {
                   if (status == BluetoothGatt.GATT_SUCCESS) {
                       Log.d(TAG, "onCharacteristicRead: " + status);
                   }
               }

               @Override
               public void onCharacteristicChanged(BluetoothGatt gatt,
                                                   BluetoothGattCharacteristic characteristic) {
            	   //Log.v(TAG,"pos="+pos+"\t"+
                   //        gatt.getDevice().getAddress());
                   broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic,gatt.getDevice().getAddress());
               }
           };
        }
    }
    /*
    private final BluetoothGattCallback mGattCallbackSpec = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(status == BluetoothGatt.GATT_SUCCESS){
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction,gatt.getDevice().getAddress());
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    //Log.i(TAG, "Attempting to start service discovery:" +      mBluetoothGatt.discoverServices());

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction,gatt.getDevice().getAddress());
                }
            }
        }

        @Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			//super.onCharacteristicWrite(gatt, characteristic, status);
        	broacastUpdate(ACTION_DATA_WRITE,status,gatt.getDevice().getAddress());
            Log.w(TAG, "onCharacteristicWrite: " + status);
		}

		@Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getDevice().getAddress());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onCharacteristicRead: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
     	   Log.v(TAG,"pos=spec");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic,gatt.getDevice().getAddress());
        }
    };*/

    private void broadcastUpdate(final String action, String address) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_ADDRESS, address);
        sendBroadcast(intent);
    }
    
    private void broacastUpdate(final String action, int status, String address){
    	final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_STATUS, status);
        intent.putExtra(EXTRA_ADDRESS, address);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, String address) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        final byte[] data = characteristic.getValue();
        intent.putExtra(EXTRA_DATA, data);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_ADDRESS, address);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close(pos);
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if(mBluetoothDeviceAddresses == null)
            mBluetoothDeviceAddresses = new String[BleAppManager.MAX_DEVICE_NUM];
        if(mBluetoothGatts == null)
            mBluetoothGatts = new BluetoothGatt[BleAppManager.MAX_DEVICE_NUM];
        if(mConnectionStates == null)
            mConnectionStates = new int[BleAppManager.MAX_DEVICE_NUM];
        if(mGattCallbacks == null)
        	createGattCallbacks();

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address,int pos) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddresses[pos] != null && address.equals(mBluetoothDeviceAddresses[pos])
                && mBluetoothGatts[pos] != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatts[pos].connect()) {
                mConnectionStates[pos] = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatts[pos] = device.connectGatt(this, false, mGattCallbacks[pos]);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddresses[pos] = address;
        mConnectionStates[pos] = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect(int pos) {
        if (mBluetoothAdapter == null || mBluetoothGatts[pos] == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatts[pos].disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close(int pos) {
        if (mBluetoothGatts[pos] == null) {
            return;
        }
        mBluetoothGatts[pos].close();
        mBluetoothGatts[pos] = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic,int pos) {
        if (mBluetoothAdapter == null || mBluetoothGatts[pos] == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        return mBluetoothGatts[pos].readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     * @return 
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled,int pos) {
        if (mBluetoothAdapter == null || mBluetoothGatts[pos] == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        
        boolean result = false;
        
        result = mBluetoothGatts[pos].setCharacteristicNotification(characteristic, enabled);
        
        if(!result)
        	return result;
        
        UUID uuid = characteristic.getUuid();
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if(enabled){
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }else{ 
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        	
        return result = mBluetoothGatts[pos].writeDescriptor(descriptor);
        
        
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices(int pos) {
        if (mBluetoothGatts[pos] == null) return null;
        return mBluetoothGatts[pos].getServices();
    }
    
    public boolean sendCharacteristicData(BluetoothGattCharacteristic characteristic,byte[] data,int pos){
    	boolean result = false;
    	if(mBluetoothAdapter == null || mBluetoothGatts[pos] == null){
    		Log.w(TAG, "BluetoothAdapter not initialized");
    		return result;
    	}
    	characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    	characteristic.setValue(data);
    	result = mBluetoothGatts[pos].writeCharacteristic(characteristic);
    	return result;
    	
    }

}
