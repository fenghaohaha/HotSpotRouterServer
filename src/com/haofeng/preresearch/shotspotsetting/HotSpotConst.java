package com.haofeng.preresearch.shotspotsetting;

public class HotSpotConst {
	/** 通知服务开启热点命令 */
	public final static String BUNDLE_KEY_MSG_START_HOTSPOT = "bundle_start_cmd";
	/** 通知服务关闭热点命令 */
	public final static String BUNDLE_KEY_MSG_STOP_HOTSPOT = "bundle_stop_cmd";
	/** 服务发送热点已开启命令 */
	public final static String BUNDLE_KEY_MSG_STARTED_HOTSPOT = "bundle_started_cmd";
	/** 服务发送热点已关闭命令 */
	public final static String BUNDLE_KEY_MSG_STOPED_HOTSPOT = "bundle_stoped_cmd";
	/** 通知服务断开某热点 */
	public final static String BUNDLE_KEY_MSG_DISCONNECT_CLIENT = "bundle_disconncet_client";
	/** */
//	public final static String BUNDLE_KEY_MSG_STOPED_HOTSPOT = "bundle_stoped_cmd";
	
	public final static int BUNDLE_MSG_START_HOTSPOT = 0;
	public final static int BUNDLE_MSG_STOP_HOTSPOT = 1;
	public final static int BUNDLE_MSG_STARTED_HOTSPOT = 2;
	public final static int BUNDLE_MSG_STOPED_HOTSPOT = 3;
	public final static int BUNDLE_MSG_DISCONNECT_CLIENT = 4;
	
	/** 通知服务热点的名称 */
	public final static String BUNDLE_KEY_MSG_NAME_HOTSPOT = "bundle_name";
	/** 通知服务热点的密码 */
	public final static String BUNDLE_KEY_MSG_PASSWORD_HOTSPOT = "bundle_password";
	/** 通知服务热点的类型 */
	public final static String BUNDLE_KEY_MSG_TYPE_HOTSPOT = "bundle_type";
	/** 服务发送热点操作过程中的状态，包括异常和正常信息 */
	public final static String BUNDLE_KEY_CMD_STATE_TYPE_HOTSPOT = "bundle_cmd_state";
	/** 通知服务需要断开的热点的地址 */
	public final static String BUNDLE_KEY_MSG_DISCONNECT_CLIENT_ADDR = "bundle_disconnect_client_addr";
	
}