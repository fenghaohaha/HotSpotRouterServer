package com.haofeng.preresearch.shotspotsetting;

import com.haofeng.preresearch.shotspotsetting.config.HotSpotConfig;
import com.haofeng.preresearch.shotspotsetting.config.HotSpotServerConfig;
import com.haofeng.preresearch.shotspotsetting.server.HotSpotServer;
import com.haofeng.preresearch.shotspotsetting.service.HotSpotService;
import com.haofeng.preresearch.shotspotsetting.utils.ServerUtils;
import com.haofeng.preresearch.shotspotsetting.utils.TimeUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 热点自启设置应用<br>
 * 基本功能：开机自启，自启后打开wify热点<br>
 * 
 * @author haofeng
 * @since Jue.26 2017
 */
public class MainActivity extends Activity {

	private EditText mEtHotspotName;
	private EditText mEtHotspotPassword;
	private LinearLayout mLlHotSpotPassword;
	private Spinner mSpinner;
	private TextView mTvState;
	private TextView mTvStateLog;
	private TextView mTvServerState;
	private Button mBtStart;
	private Button mBtClose;
	private Button mBtStartServer;
	private Button mBtCloseServer;

	/** 开放式热点 */
	public final static int HOTSPOT_TYPE_OPEN = 0;
	/** WPA2 PSK 加密热点 */
	public final static int HOTSPOT_TYPE_ENCRY = 1;
	/** 热点状态改变 */
	private final static int MSG_STATE_CHANGE = 0x0011;
	/** 记录热点状态 */
	private final static int MSG_STATE_RECORD = 0x0012;
	/** 开启wifi热点 */
	private final static int MSG_START_WORK = 0x0013;
	/** 服务器日志 */
	public final static int MSG_SERVER_LOG = 0x0014;
	/** 服务器启动 */
	public final static int MSG_SERVER_START = 0x0015;
	/** 服务器关闭 */
	public final static int MSG_SERVER_CLOSE = 0x0016;

	private HotSpotConfig mHotSpotConfig;
	private HotSpotServerConfig mHotSpotServerConfig;
	private ServiceToActivity mServiceToActivity;
	
	private int mType;
	
	private HotSpotApplication application = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		this.getWindow().setFlags(0x80000000, 0x80000000);//add by benson ,homekey
		
		setContentView(R.layout.activity_main);
		
		mHotSpotConfig = new HotSpotConfig(MainActivity.this);
		mHotSpotServerConfig = new HotSpotServerConfig(MainActivity.this);
		
		mServiceToActivity = new ServiceToActivity();
		IntentFilter filter = new IntentFilter(ServiceToActivity.service2activityAction);
		registerReceiver(mServiceToActivity, filter);
		
		application = HotSpotApplication.getInstance();
		
		initView();
		initListener();
		initData();
		initService();
		
		acquireWakeLock();
		
//		mHandler.sendEmptyMessageDelayed(MSG_START_WORK, 3000);
	}
	
	private void initView() {
		mEtHotspotName = (EditText) findViewById(R.id.et_hotspot_name);
		mEtHotspotPassword = (EditText) findViewById(R.id.et_hotspot_password);
		mSpinner = (Spinner) findViewById(R.id.sp_hotspot_type);
		mLlHotSpotPassword = (LinearLayout) findViewById(R.id.ll_hotspot_password);
		mTvState = (TextView) findViewById(R.id.tv_hotspot_msg);
		mTvServerState = (TextView) findViewById(R.id.tv_hotspot_server);
		mTvStateLog = (TextView) findViewById(R.id.tv_state_log);
		mBtStart = (Button) findViewById(R.id.bt_start_hostspot);
		mBtClose = (Button) findViewById(R.id.bt_close_hostspot);
		mBtStartServer = (Button) findViewById(R.id.bt_start_hostspot_server);
		mBtCloseServer = (Button) findViewById(R.id.bt_close_hostspot_server);

	}

	private void initListener() {

		mBtStart.setOnClickListener(clickListener);
		mBtClose.setOnClickListener(clickListener);
		mBtStartServer.setOnClickListener(clickListener);
		mBtCloseServer.setOnClickListener(clickListener);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				mType = position;
				changeMode();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	private void initData() {
		
		mEtHotspotName.setText(HotSpotConfig.mHotSpotName);
		mEtHotspotPassword.setText(HotSpotConfig.mPassword);
		mType = mHotSpotConfig.mType;
		
		changeMode();
		
		if(HotSpotServer.isStarted()){
			mHandler.sendEmptyMessage(MSG_SERVER_START);
		}else {
			mHandler.sendEmptyMessage(MSG_SERVER_CLOSE);
		}
		
	}
	
	private void initService(){
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, HotSpotService.class);
		startService(intent);
		
	}

	/** 初始化热点工作 */
	private void initWork() {
		if (HotSpotConfig.mState) {
			openHotSpot(HotSpotConfig.mHotSpotName, HotSpotConfig.mPassword, HotSpotConfig.mType);
		} else {
			closeHotSpot();
		}
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_STATE_CHANGE:{
				
				changeMode();
				mHandler.sendEmptyMessage(MSG_STATE_RECORD);
				
			}
				break;
			case MSG_STATE_RECORD:
				
				mHotSpotConfig.recordState(HotSpotConfig.mHotSpotName, HotSpotConfig.mPassword, HotSpotConfig.mType, HotSpotConfig.mState);
				
				break;
			case MSG_START_WORK:
				
				initWork();
				
				break;
			case MSG_SERVER_LOG:
				
				appendLog(msg.obj.toString());
				
				break;	
			case MSG_SERVER_START:

				if(msg.obj != null){
					appendLog(msg.obj.toString());
				}
				
				String ip = mHotSpotServerConfig.ip;
				mTvServerState.setText("");
				mTvServerState.append("Host:" + ip + "\n");
				mTvServerState.append("Port:" + HotSpotServerConfig.port +"\n");
				mTvServerState.append("连接到热点的设备数：\n" + "已认证的设备数：\n" + "更多信息");
				
				break;
			case MSG_SERVER_CLOSE:
				
				if(msg.obj != null){
					appendLog(msg.obj.toString());
				}
				
				mTvServerState.setText("服务器已关闭");
				
				break;
				
			default:
				break;
			}
			
		};
	};
	
	OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_start_hostspot: {

				String name = mEtHotspotName.getText().toString().trim();
				String password = mEtHotspotPassword.getText().toString().trim();
				
				if (TextUtils.isEmpty(name)) {
					
					showToast("热点名称不能为空");
					
				} else {
					
					if (mType == HOTSPOT_TYPE_OPEN) {

						openHotSpot(name, password, mType);

					} else if (mType == HOTSPOT_TYPE_ENCRY) {
						
						if (TextUtils.isEmpty(password)) {
							
							showToast("密码不能为空");
							
						} else if(password.length() < 8){
							
							showToast("密码不能少于8位");
							
						}else {
							openHotSpot(name, password , mType);
						}
						
					}
				}

			}
				break;
			case R.id.bt_close_hostspot: {
				
				closeHotSpot();
				
			}
				break;
			case R.id.bt_start_hostspot_server: {
//				application.initHotSpotServer();
				
				HotSpotServer.getHotSpotServer(MainActivity.this, mHandler).initServer();
				String ip = mHotSpotServerConfig.ip;
				
				mTvServerState.setText("");
				mTvServerState.append("Host:" + ip + "\n");
				mTvServerState.append("Port:" + mHotSpotServerConfig.port +"\n");
				mTvServerState.append("连接到热点的设备数：\n" + "已认证的设备数：\n" + "更多信息");
				mBtStartServer.setVisibility(View.GONE);
				mBtCloseServer.setVisibility(View.VISIBLE);
				
			}
				break;
			case R.id.bt_close_hostspot_server: {
				
//				application.closeHotSpotServer();
				HotSpotServer.getHotSpotServer(MainActivity.this).closeServer();
				
				mBtStartServer.setVisibility(View.VISIBLE);
				mBtCloseServer.setVisibility(View.GONE);
				mTvServerState.setText("服务器已关闭");
				
			}
				break;
			default:
				break;
			}
		}
	};
	
	
	/**
	 * 打开热点
	 * 
	 * @param name
	 * @param password
	 */
	private void openHotSpot(String name, String password, int type) {
//		mHotSpotConfig.mState = mHotSpotService.operationHotSpot(mHotSpotConfig.mHotSpotName, mHotSpotConfig.mPassword, mHotSpotService.mActualState, true);
		
		Bundle bundle = new Bundle();
		bundle.putInt(HotSpotConst.BUNDLE_KEY_MSG_START_HOTSPOT, HotSpotConst.BUNDLE_MSG_START_HOTSPOT);
		bundle.putString(HotSpotConst.BUNDLE_KEY_MSG_NAME_HOTSPOT, name);
		bundle.putString(HotSpotConst.BUNDLE_KEY_MSG_PASSWORD_HOTSPOT, password);
		bundle.putInt(HotSpotConst.BUNDLE_KEY_MSG_TYPE_HOTSPOT, type);
		sendMsg(bundle);
		
//		mHandler.sendEmptyMessage(MSG_STATE_CHANGE);
	}

	/**
	 * 关闭热点
	 * 
	 */
	private void closeHotSpot() {
//		mHotSpotConfig.mState = mHotSpotService.operationHotSpot(mHotSpotConfig.mHotSpotName, mHotSpotConfig.mPassword, mHotSpotService.mActualState, false);
		
		Bundle bundle = new Bundle();
		bundle.putInt(HotSpotConst.BUNDLE_KEY_MSG_STOP_HOTSPOT, HotSpotConst.BUNDLE_MSG_STOP_HOTSPOT);
		sendMsg(bundle);
		
	}
	
	/**
	 * 改变热点安全类型
	 */
	private void changeMode() {
		if (mType == HOTSPOT_TYPE_OPEN) {
			mSpinner.setSelection(HOTSPOT_TYPE_OPEN);
			mLlHotSpotPassword.setVisibility(View.GONE);
		} else if (mType == HOTSPOT_TYPE_ENCRY) {
			mSpinner.setSelection(HOTSPOT_TYPE_ENCRY);
			mLlHotSpotPassword.setVisibility(View.VISIBLE);
		}

		if (HotSpotConfig.mState) {
			mBtStart.setVisibility(View.GONE);
			mBtClose.setVisibility(View.VISIBLE);
		} else {
			mBtStart.setVisibility(View.VISIBLE);
			mBtClose.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mServiceToActivity);
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int code = event.getKeyCode();
		switch(code){
		case KeyEvent.KEYCODE_HOME:{
			
		}
			break;
		case KeyEvent.KEYCODE_BACK:{
			
		}
			break;
		
		default:{
			
			return true;
		}
		}
		return super.dispatchKeyEvent(event);
//		return true;
	}
	
	public void showToast(String msg) {
		Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	
	public class ServiceToActivity extends BroadcastReceiver{

		public final static String service2activityAction = "com.haofeng.preresearch.shotspotsetting.broadcast.ServiceToActivity";
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			if(intent.getAction().equals(service2activityAction)){
				Log.d("ServiceToActivity", "receive msg");
				Bundle bundle = intent.getExtras();
				
				//命令执行异常
				if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_CMD_STATE_TYPE_HOTSPOT)){
					String state = bundle.getString(HotSpotConst.BUNDLE_KEY_CMD_STATE_TYPE_HOTSPOT);
					setStateMsg("执行异常:\n" + state, true, R.color.red);
				}
				
				//命令执行正常
				if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_STARTED_HOTSPOT)){
					
					int cmd = bundle.getInt(HotSpotConst.BUNDLE_KEY_MSG_STARTED_HOTSPOT);
					
					Log.d("ServiceToActivity", HotSpotConst.BUNDLE_KEY_MSG_STARTED_HOTSPOT + "receive service msg" + cmd);
					
					if(cmd == HotSpotConst.BUNDLE_MSG_STARTED_HOTSPOT){
					
						setStateMsg("热点已启动\n" + "名称：" + HotSpotConfig.mHotSpotName + "\n密码：" + HotSpotConfig.mPassword + "\n类型："
								+ (HotSpotConfig.mType == HOTSPOT_TYPE_OPEN ? "开放式热点" : "WPA2 PSK"), true, R.color.green);
						mBtClose.setVisibility(View.VISIBLE);
						mBtStart.setVisibility(View.GONE);
						setStateHotSpot(true);
						
					}
				}else if(bundle.containsKey(HotSpotConst.BUNDLE_KEY_MSG_STOPED_HOTSPOT)){
					
					int cmd = bundle.getInt(HotSpotConst.BUNDLE_KEY_MSG_STOPED_HOTSPOT);
					
					Log.d("ServiceToActivity", HotSpotConst.BUNDLE_KEY_MSG_STOPED_HOTSPOT + "receive msg" + cmd);
					
					if(cmd == HotSpotConst.BUNDLE_MSG_STOPED_HOTSPOT){
						
						setStateMsg("热点已关闭", true, -1);
						mBtClose.setVisibility(View.GONE);
						mBtStart.setVisibility(View.VISIBLE);
						setStateHotSpot(false);
						
					}
				}
				
			}
		}

	}
	
	/**
	 * 设置状态信息
	 * @param msg 信息内容
	 * @param isVisible 是否可见
	 * @param resId 颜色
	 */
	private void setStateMsg(String msg, Boolean isVisible, int resId){
		mTvState.setText(msg);
		
		if(isVisible){
			mTvState.setVisibility(View.VISIBLE);
		}else {
			mTvState.setVisibility(View.GONE);
		}
		if(resId != -1){
			mTvState.setTextColor(getResources().getColor(resId));
		}
		
	}
	
	private void appendLog(String log){
		
		String state = TimeUtils.getSystemTime() + ":" + log + "\n";
		mTvStateLog.append(state + "\n");
		
	}
	
//	/** 隐藏键盘 */
//	public void hideSoftInputView() {
//		InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
//		if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
//			if (getCurrentFocus() != null)
//				manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//		}
//	}
	
	/**
	 * 设置wify执行状态
	 * @param isWork
	 */
	private void setStateHotSpot(Boolean isWork){
		
		mEtHotspotName.setEnabled(!isWork);
		mEtHotspotPassword.setEnabled(!isWork);
		mSpinner.setEnabled(!isWork);
		
	}
	
	/**
	 * 发送消息
	 * @param bundle
	 */
	private void sendMsg(Bundle bundle){
		
		Intent intent = new Intent();
		intent.setAction(HotSpotService.ActivityToService.activity2serviceAction);
		intent.putExtras(bundle);
		sendBroadcast(intent);
		
	}
	
	private WakeLock wakeLock = null;
	
	/**
	 * 获取电源锁，保证服务在屏幕熄灭时仍然获取CPU时，保持运行
	 */
	private void acquireWakeLock(){
		
		if(null == wakeLock){
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, getClass().getCanonicalName());
			if(null != wakeLock){
				wakeLock.acquire();
			}
		}
		
	}
	
	/**
	 * 释放电源锁
	 */
	private void releaseWakeLock(){
		
		if(null != wakeLock && wakeLock.isHeld()){
			wakeLock.release();
			wakeLock = null;
		}
		
	}
	
}
