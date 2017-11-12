package com.haofeng.preresearch.shotspotsetting.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import com.haofeng.preresearch.shotspotsetting.MainActivity;
import com.haofeng.preresearch.shotspotsetting.server.ConnectedDeviceInfo;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

public class HotSpotUtil {
	
	private Context mContext;
	private WifiManager mWifiManager;
	ArrayList<ConnectedDeviceInfo> connectedDevices;
	private boolean isFinishScan = false;
	
	public HotSpotUtil(Context context){
		this.mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		connectedDevices = new ArrayList<ConnectedDeviceInfo>();
		
	}
	
	/**
	 * 得到所有已连接设备
	 * @param onlyReachables 是否需要获取到连接（设备已断开），一般为false
	 * @param reachableTimeout 尝试连接设备所需的时间
	 * @return 已连接的设备列表
	 */
	public ArrayList<ConnectedDeviceInfo> getAllConnectedDevice(final boolean onlyReachables, final int reachableTimeout){
		
		getClientList(onlyReachables, reachableTimeout, new FinishScanListener() {
			
			@Override
			public void onFinishScan(ArrayList<ConnectedDeviceInfo> clients) {
				connectedDevices = clients;
			}
		});
		
		return connectedDevices;
	}
	
	/**
	 * 断开某一个热点
	 * @param connectedDevice
	 * @return 是否断开
	 */
	public boolean disConncetDevice(ConnectedDeviceInfo connectedDevice){
		
		
		
		return false;
	}
	
	
	public void getClientList(final boolean onlyReachables, final int reachableTimeout, final FinishScanListener finishListener) {

		Runnable runnable = new Runnable() {
			public void run() {

				setFinishScan(false);
				
				BufferedReader br = null;
				final ArrayList<ConnectedDeviceInfo> result = new ArrayList<ConnectedDeviceInfo>();
				
				try {
					br = new BufferedReader(new FileReader("/proc/net/arp"));
					String line;
					while ((line = br.readLine()) != null) {
						String[] splitted = line.split(" +");

						if ((splitted != null) && (splitted.length >= 4)) {
							
							String ip = splitted[0];
							String hwType = splitted[1];
							String flags = splitted[2];
							String mac = splitted[3];
							String mask = splitted[4];
							String device = splitted[5];
							
							if (mac.matches("..:..:..:..:..:..")) {
								boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

								if (!onlyReachables || isReachable) {
									ConnectedDeviceInfo connectedDeviceInfo = new ConnectedDeviceInfo();
									connectedDeviceInfo.setIp(ip);
									connectedDeviceInfo.setMacAddr(mac);
									connectedDeviceInfo.setName(device);
									
									result.add(connectedDeviceInfo);
								}
							}
						}
					}
				} catch (Exception e) {
					Log.e(this.getClass().toString(), e.toString());
				} finally {
					try {
						br.close();
					} catch (IOException e) {
						Log.e(this.getClass().toString(), e.getMessage());
					}
				}

				finishListener.onFinishScan(result);
				setFinishScan(true);
			}
		};

		Thread mythread = new Thread(runnable);
		mythread.start();
	}
	
	/**
	 * 扫描完毕监听
	 * @author haofeng
	 *
	 */
	public interface FinishScanListener {
		
		public void onFinishScan(ArrayList<ConnectedDeviceInfo> clients);

	}
	
	/**
	 * 打开/关闭 wifi热点，通过反射获取状态
	 * 
	 * @param name 热点名称
	 * @param password 热点密码
	 * @param isOpened 是否已经打开
	 * @param isOpen 打开/关闭
	 * @param type 热点类型
	 * @return
	 */
	public boolean operationHotSpot(String name, String password, Boolean isOpened, Boolean isOpen, int type){
		
		if (isOpen) {
			//关闭wifi
			mWifiManager.setWifiEnabled(false);
		}

		if(isOpen && isOpened){
			return true;
		}
		
		WifiConfiguration apConfig = new WifiConfiguration();
		apConfig.SSID = name;
		apConfig.preSharedKey = password;
		apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		apConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		
		if(type == MainActivity.HOTSPOT_TYPE_ENCRY){
			apConfig.allowedKeyManagement.set(4);
		}else if(type == MainActivity.HOTSPOT_TYPE_OPEN){
			apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		}

		Log.d("wifi setting", name + " " + password);
		
		Method method = null;
		try {
			method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
//			setStateMsg(e.getMessage());
		} catch (SecurityException e) {
			e.printStackTrace();
//			setStateMsg(e.getMessage());
		}

		//打开或关闭热点
		try {
			boolean ress = (Boolean) method.invoke(mWifiManager, apConfig, isOpen);
			
			Log.d("service operate wifi", ress + " result " + isOpen);
			
			return ress;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
//			setStateMsg(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
//			setStateMsg(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
//			setStateMsg(e.getMessage());
		}
		
		return false;
	}
	
	public boolean isFinishScan() {
		return isFinishScan;
	}

	public void setFinishScan(boolean isFinishScan) {
		this.isFinishScan = isFinishScan;
	}

	public static void showToast(Context context, String msg){
		Toast.makeText(context, msg + "-util_msg", Toast.LENGTH_SHORT).show();
	}
}
