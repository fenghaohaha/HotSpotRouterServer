package com.haofeng.preresearch.shotspotsetting.server;

import java.util.ArrayList;
import com.haofeng.preresearch.shotspotsetting.server.command.BaseCommand;

import android.text.TextUtils;

/**
 * 连接到热点的设备信息
 * @author fenghao
 * @since Jue.30 2017
 * 
 */
public class ConnectedDeviceInfo {
	
	/** ip地址 */
	String ip;
	/** mac地址 */
	String macAddr;
	/** 设备名称 */
	String name;
	/** 是否认证 */
	private boolean isAuthened;
	/** 认证信息 */
	String authenMsg;
	/** 命令历史 {@link BaseCommand} */
	private ArrayList<String> cmdHistory;
	/** 读写线程的ID */
	int operateThreadId;
	/** 是否正在占用通话资源 */
	private boolean isCalling;
	/** 当前设备所要拨打的电话号码 */
	String phoneNum;
	Object arg2;
	
	public boolean isCalling() {
		return isCalling;
	}
	public void setCalling(boolean isCalling) {
		this.isCalling = isCalling;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getMacAddr() {
		return macAddr;
	}
	public void setMacAddr(String macAddr) {
		this.macAddr = macAddr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAuthened() {
		return isAuthened;
	}
	public String getAuthenMsg() {
		return authenMsg;
	}
	public void setAuthenMsg(String authenMsg) {
		this.authenMsg = authenMsg;
		isAuthened = true;
	}
	public ArrayList<String> getCmdHistory() {
		return cmdHistory;
	}
	public void setCmdHistory(ArrayList<String> cmdHistory) {
		this.cmdHistory = cmdHistory;
	}
	public void setAuthened(boolean isAuthened) {
		this.isAuthened = isAuthened;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public void setArg2(Object arg2) {
		this.arg2 = arg2;
	}
	public int getOperateThreadId() {
		return operateThreadId;
	}
	public void setOperateThreadId(int operateThreadId) {
		this.operateThreadId = operateThreadId;
	}
	
	/**
	 * 添加一条命令记录
	 * @param cmd
	 */
	public void addCmdHistory(String cmd){
		if(!TextUtils.isEmpty(cmd) && cmdHistory != null){
			cmdHistory.add(cmd);
		}
	}
	
	public ConnectedDeviceInfo() {
		super();
		cmdHistory = new ArrayList<String>();
		
	}
	
//	public ConnectedDeviceInfo(String ip, String macAddr, String name, boolean isAuthened, String authenMsg,
//			ArrayList<String> cmdHistory, int operateThreadId, Object arg0, Object phoneNum, Object arg2) {
//		super();
//		this.ip = ip;
//		this.macAddr = macAddr;
//		this.name = name;
//		this.isAuthened = isAuthened;
//		this.authenMsg = authenMsg;
//		this.cmdHistory = cmdHistory;
//		this.operateThreadId = operateThreadId;
//		this.arg0 = arg0;
//		this.phoneNum = phoneNum;
//		this.arg2 = arg2;
//		
//		cmdHistory = new ArrayList<String>();
//	}
	
	@Override
	public String toString() {
		String info = "ip=" + ip + ", macAddr=" + macAddr + ", name=" + name + ", isAuthened="
				+ isAuthened + ", authenMsg=" + authenMsg + ", isCalling=" + isCalling + ", phoneNum="
				+ phoneNum + ", arg2=" + arg2;
		String cmds = "###";
		
		for(String cmd : cmdHistory){
			cmds += (cmd + ":");
		}
		
		return info + cmds;
	}
	
}
