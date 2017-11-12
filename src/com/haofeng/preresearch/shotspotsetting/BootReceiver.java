package com.haofeng.preresearch.shotspotsetting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 开机自启，打开该应用
 * @author fenghao
 *
 */
public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
//		Intent intent3 = new Intent(context, HotSpotService.class);
//		context.startService(intent3);
		HotSpotApplication hotSpotApplication = HotSpotApplication.getInstance();
		
		Intent intent2 = context.getPackageManager().getLaunchIntentForPackage("com.haofeng.preresearch.shotspotsetting");
		context.startActivity(intent2);
		
	}

}