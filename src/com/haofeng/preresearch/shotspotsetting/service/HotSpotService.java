package com.haofeng.preresearch.shotspotsetting.service;

import com.haofeng.preresearch.shotspotsetting.HotSpotConst;
import com.haofeng.preresearch.shotspotsetting.MainActivity;
import com.haofeng.preresearch.shotspotsetting.config.HotSpotConfig;
import com.haofeng.preresearch.shotspotsetting.utils.HotSpotUtil;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

/**
 * 热点服务<br>
 * 基本功能：打开、关闭热点，监听热点状态
 * 
 * @author haofeng
 * @since Jue.26 2017
 */
public class HotSpotService extends Service {

	private HotSpotConfig mHotSpotConfig;
	/** 热点实际的开启状态 */
	public static boolean mActualState;
	/** 热点的执行状态信息 */
	public String executeStateMsg;
	private HotSpotUtil mHotSpotUtil;
	
	private ActivityToService mActivityToServiceReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("msg", "service create-------------");
		
		mHotSpotConfig = new HotSpotConfig(HotSpotService.this);
		mHotSpotUtil = new HotSpotUtil(HotSpotService.this);
		executeStateMsg = "";
		mActivityToServiceReceiver = new ActivityToService();
		
		mHotSpotUtil.showToast(this, "service create");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// 初始化配置

		Log.d("msg", "service start------------------------");
		IntentFilter intentFilter = new IntentFilter(ActivityToService.activity2serviceAction);
		registerReceiver(mActivityToServiceReceiver, intentFilter);
		
//		mActualState = 
		
		mHotSpotUtil.operationHotSpot(mHotSpotConfig.mHotSpotName, mHotSpotConfig.mPassword, mActualState, mHotSpotConfig.mState, mHotSpotConfig.mType);
		mActualState = mHotSpotConfig.mState;
		sendResult();
		
		Log.d("msg", "wifi start------------------------");

		return START_STICKY;
	}
	
	/**
	 * 设置执行状态信息
	 * @param msg
	 */
	public void setStateMsg(String msg){
		executeStateMsg = msg;
	}
	
	public String getStateMsg(){
		return executeStateMsg;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mActivityToServiceReceiver);
	}
	
	/**
	 * activity 发送来的命令<br>
	 * 执行之后发送返回结果
	 * @author haofeng
	 *
	 */
	public class ActivityToService extends BroadcastReceiver{

		public final static String activity2serviceAction = "com.haofeng.preresearch.shotspotsetting.broadcast.Activity2Service";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(activity2serviceAction)){
				Log.d("ActivityToService", "receive msg 1");
				Bundle bundle = intent.getExtras();
				
				if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_START_HOTSPOT)){
					int cmd = bundle.getInt(HotSpotConst.BUNDLE_KEY_MSG_START_HOTSPOT);
					Log.d("ActivityToService2", HotSpotConst.BUNDLE_KEY_MSG_START_HOTSPOT + "receive msg" + cmd);
					if(cmd == HotSpotConst.BUNDLE_MSG_START_HOTSPOT){
						if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_NAME_HOTSPOT)&&bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_PASSWORD_HOTSPOT)&&bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_TYPE_HOTSPOT)){
							String name = bundle.getString(HotSpotConst.BUNDLE_KEY_MSG_NAME_HOTSPOT);
							String password = bundle.getString(HotSpotConst.BUNDLE_KEY_MSG_PASSWORD_HOTSPOT);
							int type = bundle.getInt(HotSpotConst.BUNDLE_KEY_MSG_TYPE_HOTSPOT);
							
							Log.d("ActivityToService3", HotSpotConst.BUNDLE_KEY_MSG_NAME_HOTSPOT + "receive msg :" + name);
							mHotSpotUtil.operationHotSpot(name, password, mActualState, true, type);
							mActualState = true;
							
							if(mActualState){
								mHotSpotConfig.recordState(name, password, type, mActualState);
							}
						}
					}
					
				}else if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT)){
					int cmd = bundle.getInt(HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT);
					Log.d("ActivityToService4", HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT + "receive msg :" + cmd);
					if(cmd == HotSpotConst.BUNDLE_MSG_STOP_HOTSPOT){
						Log.d("ActivityToService5", HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT + "receive msg :" + mActualState);
						
						mHotSpotUtil.operationHotSpot("", "", mActualState, false, mHotSpotConfig.mType);
						mActualState = false;
						
						Log.d("ActivityToService6", HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT + "receive msg :" + mActualState);
						mHotSpotConfig.recordState(null, null, -1, mActualState);
					}
				}
				
				sendResult();
			}
			
		}
	}
	
	/**
	 * 发送消息
	 * @param bundle
	 */
	private void sendResult(){
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		if(mActualState){
			bundle.putInt(HotSpotConst.BUNDLE_KEY_MSG_STARTED_HOTSPOT, HotSpotConst.BUNDLE_MSG_STARTED_HOTSPOT);
		}else {
			bundle.putInt(HotSpotConst.BUNDLE_KEY_MSG_STOPED_HOTSPOT, HotSpotConst.BUNDLE_MSG_STOPED_HOTSPOT);
		}
		if(!TextUtils.isEmpty(getStateMsg())){
			bundle.putString(HotSpotConst.BUNDLE_KEY_CMD_STATE_TYPE_HOTSPOT, getStateMsg());
		}
		intent.setAction(MainActivity.ServiceToActivity.service2activityAction);
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}

}
