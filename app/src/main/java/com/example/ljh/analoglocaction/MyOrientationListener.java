package com.example.ljh.analoglocaction;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by ljh on 2017/12/27.
 */

public class MyOrientationListener implements SensorEventListener{
    private Context mContext;
    private Sensor mSensor;
//    private Sensor mAcceleration;
//    private Sensor mMagnetic;
    private SensorManager mSensorManager;
    private float lastOrientation = 0;
    OnOrientationListener listener;

    public interface OnOrientationListener{
        void onOrientationChange(float orientation);
    }

    public void setOnOrientationListener(OnOrientationListener listener){
        this.listener = listener;
    }

    public MyOrientationListener(Context context){
        this.mContext = context;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float orientation = event.values[SensorManager.DATA_X];
            if(Math.abs(orientation - lastOrientation) > 1.0){
                listener.onOrientationChange(orientation);
            }
            lastOrientation = orientation;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 开始获取方向
     */
    public void Start(){
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager != null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if(mSensor != null){
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * 停止获取方向
     */
    public void Stop(){
        mSensorManager.unregisterListener(this);
    }


}
