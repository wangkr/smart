package com.cqyw.smart.location.helper;


import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.cqyw.smart.common.infra.TaskExecutor;
import com.cqyw.smart.location.model.NimLocation;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyLocationManager implements BDLocationListener {
	private static final String TAG = "MyLocationManager";
	private Context mContext;
	
	/** msg handler */
	private static final int MSG_LOCATION_WITH_ADDRESS_OK = 1;
	private static final int MSG_LOCATION_POINT_OK = 2;
    private static final int MSG_LOCATION_ERROR = 3;
	
	private NimLocationListener mListener;
	
	/** Baidu location */
    private LocationClient bdLocationClient;
    private LocationClientOption bdLocationClientOpt;


	 /** google api */
    private Geocoder mGeocoder;
    
    private MsgHandler mMsgHandler = new MsgHandler();
    private TaskExecutor executor = new TaskExecutor(TAG, TaskExecutor.defaultConfig, true);
	
	public MyLocationManager(Context context, NimLocationListener oneShotListener) {
		mContext = context;
        mGeocoder = new Geocoder(mContext, Locale.getDefault());
		mListener = oneShotListener;
	}
	
	public static boolean isLocationEnable(Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		Criteria cri = new Criteria();
		cri.setAccuracy(Criteria.ACCURACY_COARSE);
		cri.setAltitudeRequired(false);
		cri.setBearingRequired(false);
		cri.setCostAllowed(false);
		String bestProvider = locationManager.getBestProvider(cri, true);
		return !TextUtils.isEmpty(bestProvider);
		
	}

    @Override
    public void onReceiveLocation(final BDLocation bdLocation) {
        LogUtil.d(TAG, "BDLocation original data:"+" lng:"+bdLocation.getLongitude()+" lat:"+bdLocation.getLatitude());
        if (bdLocation != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    getBDLocationAddress(bdLocation);
                }
            });
        }else {
            LogUtil.i(TAG, "receive system location failed");
            // 真的拿不到了
            onLocation(null, MSG_LOCATION_ERROR);
        }
    }

    public interface NimLocationListener {
		public void onLocationChanged(NimLocation location);
	}
	
	public BDLocation getLastKnownLocation() {
        try {
            return bdLocationClient.getLastKnownLocation();
		} catch (Exception e) {
			LogUtil.i(TAG, "get lastknown location failed: " + e.toString());
		}
        return null;
    }
	
	private void onLocation(NimLocation location, int what) {
        Message msg = mMsgHandler.obtainMessage();
        msg.what = what;
        msg.obj = location;
        mMsgHandler.sendMessage(msg);
    }
	
	private class MsgHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOCATION_WITH_ADDRESS_OK:
				if (mListener != null && msg.obj != null) {
                    if(msg.obj != null) {
                        NimLocation loc = (NimLocation) msg.obj;
                        loc.setStatus(NimLocation.Status.HAS_LOCATION_ADDRESS);
                        
                        // 记录地址信息
                        loc.setFromLocation(true);
                        
                        mListener.onLocationChanged(loc);
                    } else {
                    	NimLocation loc = new NimLocation();
                        mListener.onLocationChanged(loc);
                    }
                }
				break;
			case MSG_LOCATION_POINT_OK:
				if (mListener != null) {
                    if(msg.obj != null) {
                    	NimLocation loc = (NimLocation) msg.obj;
                        loc.setStatus(NimLocation.Status.HAS_LOCATION);
                        mListener.onLocationChanged(loc);
                    } else {
                    	NimLocation loc = new NimLocation();
                        mListener.onLocationChanged(loc);
                    }
                }
				break;
            case MSG_LOCATION_ERROR:
                if(mListener != null) {
                	NimLocation loc = new NimLocation();
                    mListener.onLocationChanged(loc);
                }
                break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}
	
	private void getBDLocationAddress(final BDLocation loc) {
        if (TextUtils.isEmpty(loc.getAddrStr())) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    getLocationAddress(new NimLocation(loc, NimLocation.BDMap_Location));
                }
            });
        } else {
            NimLocation location = new NimLocation(loc, NimLocation.BDMap_Location);
            location.setAddrStr(loc.getAddrStr());
            location.setProvinceName(loc.getProvince());
            location.setCityName(loc.getCity());
            location.setCityCode(loc.getCityCode());
            location.setDistrictName(loc.getDistrict());
            location.setStreetName(loc.getStreet());
            location.setStreetCode(loc.getStreetNumber());
            LogUtil.d(TAG, location.toString());

            onLocation(location, MSG_LOCATION_WITH_ADDRESS_OK);
        }
    }
	
	private boolean getLocationAddress(NimLocation location) {
        List<Address> list;
        boolean ret = false;
        try {
            list = mGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                if (address != null) {
                    location.setCountryName(address.getCountryName());
                    location.setCountryCode(address.getCountryCode());
                    location.setProvinceName(address.getAdminArea());
                    location.setCityName(address.getLocality());
                    location.setDistrictName(address.getSubLocality());
                    location.setStreetName(address.getThoroughfare());
                    location.setFeatureName(address.getFeatureName());
                }
                ret = true;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, e + "");
        }

        int what = ret ? MSG_LOCATION_WITH_ADDRESS_OK : MSG_LOCATION_POINT_OK;
        onLocation(location, what);

        return ret;
    }
	
	public void deactive() {
		stopBDLocation();
	}
	
	private void stopBDLocation() {
        if (bdLocationClient != null) {
            bdLocationClient.unRegisterLocationListener(this);
            bdLocationClient.stop();
        }
        bdLocationClient = null;
	}
	
	public void activate() {
		requestBDLocation();
	}

    // 初始化百度定位
	private void requestBDLocation() {
        // Kyrong
        if (bdLocationClient == null) {
            bdLocationClient = new LocationClient(mContext);

            bdLocationClientOpt = new LocationClientOption();
            bdLocationClientOpt.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            bdLocationClientOpt.setCoorType("bd09ll");
            bdLocationClientOpt.setScanSpan(10000);
            bdLocationClientOpt.setIsNeedAddress(true);
            bdLocationClientOpt.setTimeOut(30 * 1000);

            bdLocationClient.setLocOption(bdLocationClientOpt);
            bdLocationClient.registerLocationListener(this);
            bdLocationClient.start();
            LogUtil.d(TAG, "BDLocation start...");
        }
    }
}
