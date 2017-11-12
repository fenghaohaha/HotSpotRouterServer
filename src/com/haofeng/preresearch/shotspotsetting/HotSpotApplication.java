package com.haofeng.preresearch.shotspotsetting;

import com.haofeng.preresearch.shotspotsetting.config.HotSpotConfig;
import com.haofeng.preresearch.shotspotsetting.config.HotSpotServerConfig;
import com.haofeng.preresearch.shotspotsetting.server.HotSpotServer;
import com.haofeng.preresearch.shotspotsetting.service.HotSpotService;
import android.app.Application;
import android.content.Intent;

/**
 * 启动热点，启动服务
 * @author fenghao
 * @since Jue.30 2017
 * 
 */
public class HotSpotApplication extends Application{

	private HotSpotServer mHotSpotServer = null;
	private HotSpotConfig mHotSpotConfig = null;
	private HotSpotServerConfig mHotSpotServerConfig = null;
	
	private static HotSpotApplication mInstance = null;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mInstance = this;
		
		init();
	}
	
	public static HotSpotApplication getInstance(){
		return mInstance;
	}
	
	private void init(){
		initConfig();
		initHopSpotService();
//		initHotSpotServer();
	
	}
	
	private void initConfig(){
		mHotSpotConfig = new HotSpotConfig(this);
		mHotSpotServerConfig = new HotSpotServerConfig(this);
	}
	
	public void initHopSpotService(){
		Intent intent3 = new Intent(getInstance(), HotSpotService.class);
		getInstance().startService(intent3);
	}
	
	public void initHotSpotServer(){
		mHotSpotServer = HotSpotServer.getHotSpotServer(this);
		mHotSpotServer.initServer();
	}
	
	public void closeHotSpotServer(){
		mHotSpotServer.closeServer();
	}
	
	public void restartHotSpotServer(){
		mHotSpotServer.restartServer();
	}
	
}