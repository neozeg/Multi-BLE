package com.mupro.multi_ble;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BleActivity{

    private final static String TAG = "MainActivity";


    private final static int REQUEST_CONNECT_DEVICE = 1;
    private final static int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
    private final static int STATE_CONNECTING = BluetoothProfile.STATE_CONNECTING;
    private final static int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;


    //private int mConnectState0 = STATE_DISCONNECTED;
    //private int mConnectState1 = STATE_DISCONNECTED;
    //private int mConnectState2 = STATE_DISCONNECTED;
    private int[] mConnectStates;
    private boolean isWorking = false;
    private String mDeviceAddress0,mDeviceName0;
    private PowerManager.WakeLock mWakeLock;

    //private TextView mTvDeviceName0,mTvDeviceName1,mTvDeviceName2;
    //private TextView mTvMsg0,mTvMsg1,mTvMsg2;
    //protected Button mBtnConnect0,mBtnConnect1,mBtnConnect2;
    //protected Button mBtnTest0,mBtnTest1,mBtnTest2;
    private TextView[] mTvDeviceNames;
    private TextView[] mTvMsgs;
    protected Button[] mBtnConnects;
    protected Button[] mBtnTests;

    private BleDevice.BleBroadcastReceiver[] bleReceivers;

    byte[] txCmd0=  {(byte) 0xaa,6,0x10, 0x00,0x00,0x00,0x00};//report normal message
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bleApp.manager.isEnabled(MainActivity.this);
        mConnectStates = new int[bleApp.manager.MAX_DEVICE_NUM];
        for(int i=0 ; i<mConnectStates.length; i++)
            mConnectStates[i] = STATE_DISCONNECTED;
        setupViewComponents();
        createBleReceivers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectDevice(0);
        disconnectDevice(1);
        disconnectDevice(2);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    //bleApp.manager.bleDevice.connectDevice(data);
                    Log.v(TAG, "connect device ok");
                    int devicePos = data.getIntExtra(bleApp.manager.STRING_BLE_DEVICE_POS,0);
                    bleApp.manager.bleDevices[devicePos].setBleBroadcastReceiver(bleReceivers[devicePos]);
                    mTvDeviceNames[devicePos].setText("...");
                    mBtnConnects[devicePos].setText("Connecting");
                    /*
                    switch(devicePos){
                        case 0:
                            mTvDeviceName0.setText("...");
                            mBtnConnect0.setText("Connecting");
                            break;
                        case 1:
                            mTvDeviceName1.setText("...");
                            mBtnConnect1.setText("Connecting");
                            break;
                        case 2:
                            mTvDeviceName2.setText("...");
                            mBtnConnect2.setText("Connecting");
                            break;
                    }*/
                }
        }
    }
    void setupViewComponents(){
        mTvDeviceNames = new TextView[BleAppManager.MAX_DEVICE_NUM];
        mTvMsgs = new TextView[BleAppManager.MAX_DEVICE_NUM];;
        mBtnConnects = new Button[BleAppManager.MAX_DEVICE_NUM];;
        mBtnTests = new Button[BleAppManager.MAX_DEVICE_NUM];;

        mTvDeviceNames[0] = (TextView) findViewById(R.id.textViewDeviceName0);
        mTvDeviceNames[1] = (TextView) findViewById(R.id.textViewDeviceName1);
        mTvDeviceNames[2] = (TextView) findViewById(R.id.textViewDeviceName2);
        mTvMsgs[0] = (TextView) findViewById(R.id.textViewMsg0);
        mTvMsgs[1] = (TextView) findViewById(R.id.textViewMsg1);
        mTvMsgs[2] = (TextView) findViewById(R.id.textViewMsg2);

        mBtnConnects[0] = (Button) findViewById( R.id.buttonConnect0);
        mBtnConnects[1] = (Button) findViewById( R.id.buttonConnect1);
        mBtnConnects[2] = (Button) findViewById( R.id.buttonConnect2);
        mBtnConnects[0].setOnClickListener(mOCLConnect);
        mBtnConnects[1].setOnClickListener(mOCLConnect);
        mBtnConnects[2].setOnClickListener(mOCLConnect);

        mBtnTests[0] = (Button) findViewById(R.id.buttonTest0);
        mBtnTests[1] = (Button) findViewById(R.id.buttonTest1);
        mBtnTests[2] = (Button) findViewById(R.id.buttonTest2);
        mBtnTests[0].setOnTouchListener(mOTLButtonTest);
        mBtnTests[1].setOnTouchListener(mOTLButtonTest);
        mBtnTests[2].setOnTouchListener(mOTLButtonTest);
    }

    private View.OnClickListener mOCLConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            /*
            if(v.getId() == mBtnConnect0.getId()){
                setupConnectDialog(0);
            }else if(v.getId() == mBtnConnect1.getId()){
                setupConnectDialog(1);
            }else if(v.getId() == mBtnConnect2.getId()){
                setupConnectDialog(2);
            }*/
            for(int i=0;i<mBtnConnects.length;i++){
                if(v.getId() == mBtnConnects[i].getId()){
                    setupConnectDialog(i);
                }
            }
        }
    };

    private View.OnTouchListener mOTLButtonTest = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(v.getId() == mBtnTests[0].getId()){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    txCmd0[3] = 0;
                    txCmd0[4] = 1;
                    sendBleData(txCmd0,0);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    txCmd0[3] = 0;
                    txCmd0[4] = 2;
                    sendBleData(txCmd0,0);
                }
            }else if(v.getId() == mBtnTests[1].getId()){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    txCmd0[3] = 1;
                    txCmd0[4] = 1;
                    sendBleData(txCmd0,1);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    txCmd0[3] = 1;
                    txCmd0[4] = 2;
                    sendBleData(txCmd0,1);
                }

            }else if(v.getId() == mBtnTests[2].getId()){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    txCmd0[3] = 2;
                    txCmd0[4] = 1;
                    sendBleData(txCmd0,2);
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    txCmd0[3] = 2;
                    txCmd0[4] = 2;
                    sendBleData(txCmd0,2);
                }

            }
            return true;
        }
    };


    private void setupConnectDialog(final int devicePos){


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialogBuilder.setTitle("Connect a device");
        alertDialogBuilder.setMessage("Connect a new device?");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                mConnectStates[devicePos] = STATE_DISCONNECTED;

                mTvDeviceNames[devicePos].setText("DeviceName");
                mBtnConnects[devicePos].setText("Disconnected");
                //updateConnectionState();
                /*
                switch(devicePos){
                    case 0:
                        mTvDeviceName0.setText("DeviceName");
                        mBtnConnect0.setText("Disconnected");
                        break;
                    case 1:mTvDeviceName1.setText("DeviceName");
                        mBtnConnect1.setText("Disconnected");
                        break;
                    case 2:mTvDeviceName2.setText("DeviceName");
                        mBtnConnect2.setText("Disconnected");
                        break;
                }*/

                if (bleApp.manager.bleDevices[devicePos] != null) {
                    isWorking = false;
                    bleApp.manager.bleDevices[devicePos].disconnectDevice();
                    bleApp.manager.bleDevices[devicePos].unbindService();
                    bleApp.manager.bleDevices[devicePos].unregisterReceiver();
                    //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
                    bleApp.manager.bleDevices[devicePos] = null;
                }
                //ReconnectTimer.removeCallbacks(ReconnectRunnable);


                Intent intent = new Intent(getApplicationContext(), ScanDeviceActivity.class);
                intent.putExtra(bleApp.manager.STRING_BLE_DEVICE_POS,devicePos);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
            }
        });
        /*
        if(mConnectStates[devicePos] == STATE_DISCONNECTED){
            alertDialogBuilder.setNeutralButton("reconnect device", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //reconnectDevice();
                }
            });
        }*/

        alertDialogBuilder.create().show();
    }



    private void bleConnected(int pos){
        //Toast.makeText(getApplicationContext(), "device connected", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Device Connected");
        mConnectStates[pos] = STATE_CONNECTING;
        //updateConnectionState();

    }
    private void bleDisconnected(int pos){
        Toast.makeText(getApplicationContext(), "Device Disconnected", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Device Disconnected");
        mConnectStates[pos] = STATE_DISCONNECTED;
        mTvDeviceNames[pos].setText("DeviceName");
        mBtnConnects[pos].setText("Disconnected");
        /*
        switch(pos){
            case 0:
                mTvDeviceName0.setText("DeviceName");
                mBtnConnect0.setText("Disconnected");
                break;
            case 1:mTvDeviceName1.setText("DeviceName");
                mBtnConnect1.setText("Disconnected");
                break;
            case 2:mTvDeviceName2.setText("DeviceName");
                mBtnConnect2.setText("Disconnected");
                break;
        }*/
        /*updateConnectionState();
        if(isWorking){
            reconnectDevice();
        }*/
    }
    private void bleServiceDiscovered(int pos){
        //Toast.makeText(getApplicationContext(), "service found", Toast.LENGTH_SHORT).show();
        Log.v(TAG, "Service found");
        bleApp.manager.bleDevices[pos].setCharacteristicNotification(SampleGattAttributes.SMARTEQUIT_SERVICE,
                SampleGattAttributes.SMARTEQUIT_SERVICE_NOTIFY, true);
        mTvDeviceNames[pos].setText(bleApp.manager.bleDevices[pos].getName());
        mBtnConnects[pos].setText("Connected");
        /*
        switch(pos){
            case 0:mTvDeviceName0.setText(bleApp.manager.bleDevices[pos].getName());
                mBtnConnect0.setText("Connected");
                break;
            case 1:mTvDeviceName1.setText(bleApp.manager.bleDevices[pos].getName());
                mBtnConnect1.setText("Connected");
                break;
            case 2:mTvDeviceName2.setText(bleApp.manager.bleDevices[pos].getName());
                mBtnConnect2.setText("Connected");
                break;
        }*/
        /*

        if(bleApp.manager.bleDevice.setCharacteristicNotification(SampleGattAttributes.SMARTEQUIT_SERVICE,
                SampleGattAttributes.SMARTEQUIT_SERVICE_NOTIFY, true))
        {
            Toast.makeText(getApplicationContext(), "Device Connected", Toast.LENGTH_SHORT).show();
            mConnectState = STATE_CONNECTED;

            updateConnectionState();
            mDeviceAddress = bleApp.manager.bleDevice.getAddress();
            mDeviceName = bleApp.manager.bleDevice.getName();
            mTvDeviceName.setText(mDeviceName);
            saveConnectConfig();
        }*/
    }
    private void bleDataAvailable(Intent intent,int pos){
        byte[] data = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
        //String address = intent.getStringExtra(BleAppManager.STRING_BLE_ADDRESS);
        //if(!address.toString().contains(bleApp.manager.bleDevices[pos].getAddress()))return;

        if(data!=null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            mTvMsgs[pos].setText(stringBuilder.toString());
            /*
            switch(pos){
                case 0:mTvMsg0.setText(stringBuilder.toString());
                    break;
                case 1:mTvMsg1.setText(stringBuilder.toString());
                    break;
                case 2:mTvMsg2.setText(stringBuilder.toString());
                    break;
            }*/
            processData(data,pos);
            /*
            mTvRxMessage.setText(stringBuilder.toString());
            mTvRxMessage.startAnimation(mBlinkAnimation);
            processData(data);*/
        }
    }
    private void bleDataWrite(int pos){
    }


    private void disconnectDevice(int pos){

        mConnectStates[pos] = STATE_DISCONNECTED;
        //updateConnectionState();
        if(bleApp.manager.bleDevices[pos]!=null) {
            isWorking = false;
            bleApp.manager.bleDevices[pos].disconnectDevice();
            bleApp.manager.bleDevices[pos].unbindService();
            bleApp.manager.bleDevices[pos].unregisterReceiver();
            //bleApp.manager.bleDevice.setBleBroadcastReceiver(null);
            bleApp.manager.bleDevices[pos] = null;
        }
    }

    private void sendBleData(byte[] bytes,int pos){

        byte[] data = new byte[bytes.length+1];
        for(int i=0;i<bytes.length;i++){
            data[i] = bytes[i];
            data[data.length-1]+=bytes[i];
        }
        if(data!=null && data.length > 0){
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            mTvMsgs[pos].setText(stringBuilder.toString());
            /*
            switch(pos){
                case 0:mTvMsg0.setText(stringBuilder.toString());
                    break;
                case 1:mTvMsg1.setText(stringBuilder.toString());
                    break;
                case 2:mTvMsg2.setText(stringBuilder.toString());
                    break;
            }*/
        }
        if(bleApp.manager.bleDevices[pos] != null)
            bleApp.manager.bleDevices[pos].writeValue(SampleGattAttributes.SMARTEQUIT_SERVICE,
                    SampleGattAttributes.SMARTEQUIT_SERVICE_WRITE,
                    data);
    }


    private void createBleReceivers(){
        bleReceivers = new BleDevice.BleBroadcastReceiver[BleAppManager.MAX_DEVICE_NUM];
        for(int i=0;i<bleReceivers.length;i++){
            bleReceivers[i] = new BleDevice.BleBroadcastReceiver(){
                @Override
                public void onReceive(Context context, Intent intent, String uuid){
                    final String action = intent.getAction();
                    int devicePos = intent.getIntExtra(BleAppManager.STRING_BLE_DEVICE_POS,0);
                    if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                        Log.v(TAG, "ACTION_GATT_CONNECTED");
                        bleConnected(devicePos);
                    } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                        Log.v(TAG, "ACTION_GATT_DISCONNECTED");
                        bleDisconnected(devicePos);
                    } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                        Log.v(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                        bleServiceDiscovered(devicePos);
                    } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                        //Log.v(TAG, "ACTION_DATA_AVAILABLE\n"+uuid);
                        bleDataAvailable(intent,devicePos);
                    } else if (BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
                        //Log.v(TAG, "ACTION_DATA_WRITE");
                        bleDataWrite(devicePos);
                    }
                };
            };

        }

    }
    private void displayInfo(String info,int pos){
        mTvMsgs[pos].setText(info);
        /*
        switch(pos){
            case 0:
                mTvMsg0.setText(info);
                break;
            case 1:
                mTvMsg1.setText(info);
                break;
            case 2:
                mTvMsg2.setText(info);
                break;
        }*/
    }
    private boolean processData(byte[] data,int pos){
        if(data.length < 4 || data==null ){
            //clearInfoDisplay();
            //displayInfo("Data Err",pos);
            return false;
        }
        int dataCount = data[1];
        if(dataCount > data.length-2)
            return false;
        /*
        byte dataSum = 0;
        for(int i=0; i< dataCount+1; i++){
            dataSum+=data[i];
        }
        if(dataSum != data[dataCount+1]){
            //displayInfo("Data Err",pos);
            return false;
        }
        */
        byte cmd = data[3];
        switch(cmd){
            case 0:
                //mTvDeviceNames[pos].setBackgroundColor(getResources().getColor(android.R.color.transparent));
                mTvDeviceNames[pos].setBackgroundResource(R.drawable.corner_round_body_off);
                /*
                switch(pos){
                    case 0:
                        mTvDeviceName0.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        break;
                    case 1:
                        mTvDeviceName1.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        break;
                    case 2:
                        mTvDeviceName2.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        break;
                }*/
                break;
            case 1:
            case 2:
                //mTvDeviceNames[pos].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark));
                mTvDeviceNames[pos].setBackgroundResource(R.drawable.corner_round_body_on);
                /*
                switch(pos){
                    case 0:
                        mTvDeviceName0.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        break;
                    case 1:
                        mTvDeviceName1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        break;
                    case 2:
                        mTvDeviceName2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                        break;
                }*/
                break;
        }/**/

        return true;
    }

}
