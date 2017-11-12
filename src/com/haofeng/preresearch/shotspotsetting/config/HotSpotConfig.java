package com.haofeng.preresearch.shotspotsetting.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

/**
 * 热点状态保存，持久化过程<br>
 * wify热点的参数有默认配置。u:haofeng p:hf654321 t:WPA2 PSK
 * @author haofeng
 *
 */
public class HotSpotConfig {
	
	/** 热点名称 */
	public static String mHotSpotName;
	/** 热点密码 */
	public static String mPassword;
	/** 热点类型 0:开放式热点 1:WPA2 PSK */
	public static int mType;
	/** 热点开启状态，打开：true 关闭：false */
	public static boolean mState;
	
	/** 状态保存 */
	public SharedPreferences mSharedPreference;
	public final static String HOTSPOT_PREDERENCE_FILE_NAME = "hotSpotConfig";
	public final static String HOTSPOT_PREDERENCE_NAME = "hotSpotConfig_name";
	public final static String HOTSPOT_PREDERENCE_PASSWORD = "hotSpotConfig_password";
	public final static String HOTSPOT_PREDERENCE_TYPE = "hotSpotConfig_type";
	public final static String HOTSPOT_PREDERENCE_STATE = "hotSpotConfig_state";
	
	private Context mContext;
	
	public HotSpotConfig(Context context) {
		this.mContext = context;
		
		mSharedPreference = mContext.getSharedPreferences(HOTSPOT_PREDERENCE_FILE_NAME, mContext.MODE_PRIVATE);
		mHotSpotName = mSharedPreference.getString(HOTSPOT_PREDERENCE_NAME, "HaoFeng");
		mPassword = mSharedPreference.getString(HOTSPOT_PREDERENCE_PASSWORD, "HF654321");
		mType = mSharedPreference.getInt(HOTSPOT_PREDERENCE_TYPE, 1);
		mState = mSharedPreference.getBoolean(HOTSPOT_PREDERENCE_STATE, true);
	}
	
	/**
	 * 记录热点状态
	 * @param name 热点名称
	 * @param password 热点密码
	 * @param type 热点类型
	 * @param state 开启状态
	 */
	public void recordState(String name, String password, int type, boolean state){
		
		if(mSharedPreference == null){
			mSharedPreference = mContext.getSharedPreferences(HOTSPOT_PREDERENCE_FILE_NAME, mContext.MODE_PRIVATE);
		}
		
		Editor editor = mSharedPreference.edit();
		
		if(!TextUtils.isEmpty(name)){
			mHotSpotName = name;
			editor.putString(HOTSPOT_PREDERENCE_NAME, name);
		}
		if(!TextUtils.isEmpty(password)){
			mPassword = password;
			editor.putString(HOTSPOT_PREDERENCE_PASSWORD, password);
		}
		if(type >= 0){
			mType = type;
			editor.putInt(HOTSPOT_PREDERENCE_TYPE, type);
		}
		
		mState = state;
		editor.putBoolean(HOTSPOT_PREDERENCE_STATE, state);
		editor.commit();
	}
	
}
