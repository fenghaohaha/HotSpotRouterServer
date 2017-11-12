package com.haofeng.preresearch.shotspotsetting.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class HotSpotServerConfig {
	
	private Context mContext;
	
	/** ip 地址 */
	public static String ip = "192.168.43.1";
	/** 服务端口号 */
	public static int port = 9797;
	/** Socket超时时间 ,单位：秒 */
	public static int timeOut = 60;
	/** 连接的设备最大数量 */
	public static int maxDevicesNum = 100;
	/** 认证超时时间 */
	public static int authenTimeOut = 10;
	
	/** 状态保存 */
	public SharedPreferences mSharedPreferences;
	public final static String KEY_HOTSPOT_SERVER_FILE_NAME = "hotspot_server";
	public final static String KEY_HOTSPOT_SERVER_IP = "key_server_ip";
	public final static String KEY_HOTSPOT_SERVER_PORT = "key_server_port";
	public final static String KEY_HOTSPOT_SERVER_TIMEOUT = "key_server_timeout";
	public final static String KEY_HOTSPOT_SERVER_MAXDEVICES_NUM = "key_server_max_devices_num";
	public final static String KEY_HOTSPOT_SERVER_AUTHEN_TIMEOUT = "key_server_authen_timeout";
	
	public HotSpotServerConfig(Context context){
		this.mContext = context;
		
		mSharedPreferences = mContext.getSharedPreferences(KEY_HOTSPOT_SERVER_FILE_NAME, mContext.MODE_PRIVATE);
		ip = mSharedPreferences.getString(KEY_HOTSPOT_SERVER_IP, ip);
		port = mSharedPreferences.getInt(KEY_HOTSPOT_SERVER_PORT, port);
		timeOut = mSharedPreferences.getInt(KEY_HOTSPOT_SERVER_TIMEOUT, timeOut);
		maxDevicesNum = mSharedPreferences.getInt(KEY_HOTSPOT_SERVER_MAXDEVICES_NUM, maxDevicesNum);
		authenTimeOut = mSharedPreferences.getInt(KEY_HOTSPOT_SERVER_AUTHEN_TIMEOUT, authenTimeOut);
		
	}
	
	public void recordConfig(String ip, int port, int timeOut, int maxDevicesNum, int authenTimeOut){
		if(mSharedPreferences == null){
			mSharedPreferences = mContext.getSharedPreferences(KEY_HOTSPOT_SERVER_FILE_NAME, mContext.MODE_PRIVATE);
		}
		Editor editor = mSharedPreferences.edit();
		
		editor.putString(KEY_HOTSPOT_SERVER_IP, ip);
		editor.putInt(KEY_HOTSPOT_SERVER_PORT, port);
		editor.putInt(KEY_HOTSPOT_SERVER_TIMEOUT, timeOut);
		editor.putInt(KEY_HOTSPOT_SERVER_MAXDEVICES_NUM, maxDevicesNum);
		editor.putInt(KEY_HOTSPOT_SERVER_AUTHEN_TIMEOUT, authenTimeOut);
		
		editor.commit();
	}
	
	public void recordConfig(){
		if(mSharedPreferences == null){
			mSharedPreferences = mContext.getSharedPreferences(KEY_HOTSPOT_SERVER_FILE_NAME, mContext.MODE_PRIVATE);
		}
		Editor editor = mSharedPreferences.edit();
		
		editor.putString(KEY_HOTSPOT_SERVER_IP, ip);
		editor.putInt(KEY_HOTSPOT_SERVER_PORT, port);
		editor.putInt(KEY_HOTSPOT_SERVER_TIMEOUT, timeOut);
		editor.putInt(KEY_HOTSPOT_SERVER_MAXDEVICES_NUM, maxDevicesNum);
		editor.putInt(KEY_HOTSPOT_SERVER_AUTHEN_TIMEOUT, authenTimeOut);
		
		editor.commit();
	}
}
