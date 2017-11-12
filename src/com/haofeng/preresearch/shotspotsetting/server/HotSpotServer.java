package com.haofeng.preresearch.shotspotsetting.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.haofeng.preresearch.rtp.AudioConfig;
import com.haofeng.preresearch.rtp.RTP;
import com.haofeng.preresearch.shotspotsetting.MainActivity;
import com.haofeng.preresearch.shotspotsetting.config.HotSpotServerConfig;
import com.haofeng.preresearch.shotspotsetting.server.command.BaseCommand;
import com.haofeng.preresearch.shotspotsetting.server.command.CMDS;
import com.haofeng.preresearch.shotspotsetting.server.command.CommandHandler;
import com.haofeng.preresearch.shotspotsetting.server.command.StateMsg;
import com.haofeng.preresearch.shotspotsetting.utils.HotSpotUtil;
import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

/**
 * 程序启动，开启此服务监听
 * @author haofeng
 * @since Jue.30 2017
 * 
 */
public class HotSpotServer {
	
	/** 所有已连接到热点的设备列表 */
	private ArrayList<ConnectedDeviceInfo> connectedDeviceInfos = null;
	private ArrayList<ConnectedDeviceInfo> connectedAuthenedDeviceInfos = null;
	private Context mContext;
	private static HotSpotServer mHotSpotServer = null;
	private HotSpotUtil mHotSpotUtil;
	
	/** 线程池 */
	private ExecutorService mExecutorService;
	private ServerSocket mHotSpotServerSocket = null;
	private Socket socket = null;
	private ServerThread serverThread = null;
	
	/** 线程列表中的状态，线程ID，线程是否执行 */
	private Map<Integer, Boolean> threadStatus = null;
	/** 服务是否启动 */
	private static boolean isStarted = false;
	private HotSpotServerConfig mHotSpotServerConfig = null;
	private Handler mHandler;
	
	//语音通话相关
	private int buffSize;
	private byte[] buffer;
	
	private HotSpotServer(Context context, Handler handler){
		this.mContext = context;
		this.mHandler = handler;
		
		init();
		
//		initServer();
	}
	
	private HotSpotServer(Context context){
		this.mContext = context;
		
		init();
		
//		initServer();
	}
	
	public static synchronized HotSpotServer getHotSpotServer(Context context){
		if(mHotSpotServer == null){
			mHotSpotServer = new HotSpotServer(context);
		}
		return mHotSpotServer;
	}
	
	public static synchronized HotSpotServer getHotSpotServer(Context context, Handler handler){
		if(mHotSpotServer == null){
			mHotSpotServer = new HotSpotServer(context, handler);
		}
		return mHotSpotServer;
	}
	
	private void init(){
		mHotSpotUtil = new HotSpotUtil(mContext);
		mHotSpotServerConfig = new HotSpotServerConfig(mContext);
		connectedDeviceInfos = new ArrayList<ConnectedDeviceInfo>();
		connectedAuthenedDeviceInfos = new ArrayList<ConnectedDeviceInfo>();
		
		connectedDeviceInfos = mHotSpotUtil.getAllConnectedDevice(false, 300);
		
		threadStatus = new HashMap<Integer, Boolean>();

		buffSize = AudioRecord.getMinBufferSize(AudioConfig.frequence, AudioConfig.channelConfig, AudioConfig.audioEncoding);
		buffer = new byte[1480];
	}
	
	/**
	 * 初始化服务器<br>
	 * IP为自身IP，端口号自设
	 */
	public void initServer(){
		serverThread = new ServerThread();
		serverThread.start();
		
		sendLog(connectedDeviceInfos.toString());
		
	}
	
	/**
	 * 连接线程,负责消息的收发，处理，认证
	 * @author fenghao
	 *
	 */
	private class ConnectionTask implements Runnable{

		private Socket socket = null;
		private int threadId;
		private InputStream inputStream = null;
		private OutputStream outputStream = null;
		private BufferedReader reader;
		private PrintWriter writer;
		private boolean isWorking = true;
		private ConnectedDeviceInfo connectedDeviceInfo = null;
		//通话计时考虑
		private Timer timer = null;
		/** 消息数目 */
		private int msgNum = 0;
		private long startTime = 0;
		private long endTime = 0;
		private CallTask callTask = null;
		
		public ConnectionTask(Socket socket, int threadId) {
			this.socket = socket;
			this.threadId = threadId;
			this.connectedDeviceInfo = getConnectedDeviceInfoByID(threadId);
			this.callTask = new CallTask(connectedDeviceInfo);
			
			try {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				
				reader = new BufferedReader(new InputStreamReader(inputStream));
				writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
				
				threadStatus.put(threadId, true);
				
			} catch (IOException e) {
				sendLog("服务器构造Socket异常：" + e.getMessage());
				e.printStackTrace();
			}
			
			callTask.setOnCallingListener(new onCallingListener() {
				
				@Override
				public void onStartCalling() {
					sendLog(connectedDeviceInfo.getIp() + " 开始拨打号码 " + connectedDeviceInfo.getPhoneNum());
					startTime = System.currentTimeMillis();
				}
				
				@Override
				public void onFinishCalling() {
					endTime = System.currentTimeMillis();
					sendLog(connectedDeviceInfo.getIp() + " 通话已挂断,耗时 " + (endTime - startTime)/1000. + "秒");
				}

				@Override
				public void onErrorCalling(String msg) {
					sendLog(msg + "通话建立异常");
				}

				@Override
				public void onCalling() {
//					sendLog("tonghua");
				}

			});
			
		}
		
		@Override
		public void run() {
			
			sendLog("客户端 " + connectedDeviceInfo.getIp() + " 的监听线程已启动");
			
			while(isWorking && !threadStatus.isEmpty() && threadStatus.containsKey(threadId) && threadStatus.get(threadId)){
				try {
					
					if(socket == null || !socket.isConnected() || isSocketClose() || reader == null){
						sendLog(connectedDeviceInfo.getIp() + " 的连接已断开");
						stop();
						break;
					}
					
					String cmdStr = reader.readLine();
					
					if(!TextUtils.isEmpty(cmdStr)){
						
						sendLog("客户端 " + connectedDeviceInfo.getIp() + " 发来消息第 " + msgNum++ + " 条消息："+ cmdStr);
						
						BaseCommand command = CommandHandler.packageReceivedCmd(cmdStr);
						String cmd = command.getCmd();
						String ip = command.getIp();
						String macAddr = command.getMacAddr();
						String authenMsg = command.getAuthenMsg();
						String stateMsg = command.getStateMsg();
						
						//确定是正确的对应设备
						if(ip.equals(connectedDeviceInfo.getIp()) && macAddr.equals(connectedDeviceInfo.getMacAddr())){
							
							if(cmd.equals(CMDS.APPLY_FOR_AUTHEN)){
								
								sendLog(connectedDeviceInfo.getIp() + " 请求认证");
								
								//vertify authen msg
								if(vertifyAuthenMsg(authenMsg)){
									
									changeAuthenState(threadId, true, authenMsg);
									
									write(CMDS.AUTHEN_SUCCESS, ip, macAddr, authenMsg, "");
									
									connectedAuthenedDeviceInfos.add(connectedDeviceInfo);
									
									sendLog(connectedDeviceInfo.getIp() + " 认证通过");
								}else {
									write(CMDS.AUTHEN_FAILED, ip, macAddr, authenMsg, "");
									
									changeAuthenState(threadId, false, authenMsg);
									
									sendLog(connectedDeviceInfo.getIp() + " 认证失败" + authenMsg);
									
								}
							}else if(cmd.equals(CMDS.APPLY_FOR_CALL)){
								
								sendLog(connectedDeviceInfo.getIp() + " 请求通话");
								
								if(isDeviceAuthened(threadId)){
									if(isCalling()){
										write(CMDS.CALL_FAILED, ip, macAddr, authenMsg, StateMsg.STATE_MSG_CALL_IS_WORKING);
										
										sendLog(connectedDeviceInfo.getIp() + " 请求通话失败，通话资源正占用");
									}else {
										write(CMDS.CALL_SUCCESS, ip, macAddr, authenMsg, "");
										
										sendLog(connectedDeviceInfo.getIp() + " 请求通话成功");
										
										changePhoneNum(threadId, stateMsg);
										//等待接收客户端的拨打信息
//										call(connectedDeviceInfo, true, callThread);
									}
									
								}else {
									write(CMDS.AUTHEN_FAILED, ip, macAddr, authenMsg, StateMsg.STATE_MSG_CLIENT_NOT_AUTHEN);
									sendLog(connectedDeviceInfo.getIp() + " 通话请求失败，未认证设备");
								}
									
							}else if(cmd.equals(CMDS.CALL_START)){
								//客户端拨号
								if(isDeviceAuthened(connectedDeviceInfo.getOperateThreadId())){
									write(CMDS.CALL_START, ip, macAddr, authenMsg, "");
									
									callTask.call();
								}else {
									write(CMDS.AUTHEN_FAILED, ip, macAddr, authenMsg, StateMsg.STATE_MSG_CLIENT_NOT_AUTHEN);
								}
								
							}else if(cmd.equals(CMDS.CALL_STOP)){
								//客户端挂断电话
								if(isDeviceAuthened(connectedDeviceInfo.getOperateThreadId())){
									callTask.hangUp();
									write(CMDS.CALL_STOP, ip, macAddr, authenMsg, " 通话已挂断,耗时 " + (endTime - startTime)/1000. + "秒");
								}else {
									write(CMDS.AUTHEN_FAILED, ip, macAddr, authenMsg, StateMsg.STATE_MSG_CLIENT_NOT_AUTHEN);
								}
								
							}
							
							addCmdHistory(threadId, cmdStr);
						}else {
							sendLog("客户端 " + connectedDeviceInfo.getIp() + " 是非法设备！");
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					sendLog("服务器连接Socket异常：" + e.getMessage());
				}
				
			}
		}
		
		/**
		 * 发送紧急信息，判断远端Socket是否还在连接
		 * @return
		 */
		public boolean isSocketClose(){
			try {
				if(socket != null){
					socket.sendUrgentData(0xFF);
					return false;
				}else {
					return true;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return true;
			}
		}
		
		public void start(){
			isWorking = true;
		}
		
		public void pause(){
			isWorking = false;
		}
		
		public void stop(){
			isWorking = false;
			//threadStatus.replace(threadId, false);
			if (threadStatus.containsKey(threadId)) {
				threadStatus.put(threadId, false);
			}
			
			if(socket != null && socket.isConnected() && callTask != null){
				try {
					reader.close();
					reader = null;
					
					writer.close();
					writer = null;
					
					socket.close();
					socket = null;
					
					callTask.hangUp();
					
					callTask = null;
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}

		public int getThreadId() {
			return threadId;
		}
		
		public void write(BaseCommand cmd){
			connectedDeviceInfo.addCmdHistory(cmd.toString());
			writer.write(cmd.toString() + "\n");
			writer.flush();
		}
		
		public void write(String cmd, String ip, String macAddr, String authenMsg, String stateMsg){
			
			BaseCommand command = new BaseCommand(cmd, ip, macAddr, authenMsg, stateMsg);
			write(command);
			
		}
		
	}
	
	/**
	 * 通话线程
	 * 
	 * @author haofeng
	 *
	 */
	private class CallTask{
		
		private RTP rtp;
		private ConnectedDeviceInfo deviceInfo;
		private AudioRecord audioRecord;
		private onCallingListener callingListener;
		
		public CallTask(ConnectedDeviceInfo deviceInfo) {
			this.deviceInfo = deviceInfo;
		}
		
		/**
		 * 拨打电话
		 */
		public void call(){

			new Thread(new Runnable() {
				public void run() {
					
					String phoneNum = deviceInfo.getPhoneNum();
					//拨打电话号码
					
					callingListener.onStartCalling();
					
					if(deviceInfo != null){
						changeCallingState(deviceInfo.getOperateThreadId(), true);
						
						audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AudioConfig.frequence, AudioConfig.channelConfig, AudioConfig.audioEncoding, buffSize);
						rtp = new RTP(deviceInfo.getIp());
					}
					
					if(audioRecord != null){
						audioRecord.startRecording();
					}
					
					while (isCalling() && rtp != null) {
						try {
							audioRecord.read(buffer, 0, buffer.length);
							rtp.rtpSession.sendData(buffer);
							
							callingListener.onCalling();
							
						} catch (Exception e) {
							
							e.printStackTrace();
							callingListener.onErrorCalling(e.getMessage());
							
//							rtp = null;
							audioRecord = null;
						}
					}
					
				}
			}).start();
			
		}
		
		/**
		 * 挂断
		 */
		public void hangUp(){
			changeCallingState(deviceInfo.getOperateThreadId(), false);
			
			if(rtp != null && rtp.rtpSession != null){
				rtp.rtpSession.endSession();
				rtp = null;
			}
			
			if(audioRecord != null){
				audioRecord.stop();
				audioRecord.release();
				audioRecord = null;
			}
			
			callingListener.onFinishCalling();
		}
		
		public void setOnCallingListener(onCallingListener onCallingListener){
			this.callingListener = onCallingListener;
		}
	}
	
	/**
	 * 电话监听器
	 * @author haofeng
	 *
	 */
	interface onCallingListener {
		void onStartCalling();
		void onCalling();
		void onFinishCalling();
		void onErrorCalling(String msg);
	}
	
	/**
	 * 服务器线程
	 * @author haofeng
	 *
	 */
	class ServerThread extends Thread{
		@Override
		public void run() {
			try {
				
				mExecutorService = Executors.newCachedThreadPool();
				
				mHotSpotServerSocket = new ServerSocket(HotSpotServerConfig.port, 30);
				
				sendMsg(MainActivity.MSG_SERVER_START, "服务已启动 " + mHotSpotServerConfig.ip);
				
				setStarted(true);
				
				while(isStarted){
					
					socket = mHotSpotServerSocket.accept();
					
					String ip = socket.getInetAddress().toString();
					
					ip = ip.substring(1);
					sendLog("客户端：" + ip + " 已连接" );
					
					if(socket.isConnected()){
						int id = generateOperateId();
						
						connectedDeviceInfos = mHotSpotUtil.getAllConnectedDevice(false, 300);
						
						sendLog("客户端：" + ip + " id已生成 " + id );
						
						while(!mHotSpotUtil.isFinishScan()){
						}
							
						for(ConnectedDeviceInfo connectedDeviceInfo : connectedDeviceInfos){
								
							sendLog("已连接的设备：" + connectedDeviceInfo.toString());
							
							if(connectedDeviceInfo.getIp().equals(ip)){
								connectedDeviceInfo.setOperateThreadId(id);
								
								mExecutorService.execute(new ConnectionTask(socket, id));
							}
						}
						
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 重启服务器
	 */
	public void restartServer(){
		
		closeServer();
		initServer();
		
	}
	
	/**
	 * 关闭服务器
	 */
	public void closeServer(){
		
		if(!mHotSpotServerSocket.isClosed() || mHotSpotServerSocket != null){
			try {
				setStarted(false);
				mHotSpotServerSocket.close();
				mHotSpotServerSocket = null;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(mExecutorService != null || !mExecutorService.isShutdown()){
			mExecutorService.shutdownNow();
			mExecutorService = null;
		}
		
		if(!serverThread.isInterrupted() || serverThread != null){
			serverThread.interrupt();
			serverThread = null;
		}
		
		for(Map.Entry<Integer, Boolean> entry : threadStatus.entrySet()){
			int id = entry.getKey();
			changeCallingState(id, false);
			changeAuthenState(id, false, "");
		}
		connectedAuthenedDeviceInfos.clear();
		connectedDeviceInfos.clear();
		threadStatus.clear();
		
		sendMsg(MainActivity.MSG_SERVER_CLOSE, "服务器已关闭");
		
	}
	
	public static boolean isStarted() {
		return isStarted;
	}

	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	/**
	 * 通话资源是否被占用
	 * @return
	 */
	public boolean isCalling(){
		//后续可以开辟多个通话线程时，可以考虑
		for(ConnectedDeviceInfo connectedDeviceInfo : connectedDeviceInfos){
			if(connectedDeviceInfo.isCalling()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 生成读写线程id<br>
	 * 暂且以时间为种，随后需要考虑校验一些实际列表
	 * @return 读写线程ID
	 */
	private int generateOperateId(){
		
		Random random = new Random();
		random.setSeed(System.currentTimeMillis());
		return random.nextInt();
		
	}
	
	/**
	 * 设备是否已认证
	 * @param connectedDeviceInfo
	 * @return
	 */
	private boolean isDeviceAuthened(int id){
		
		for(int i = 0; i < connectedDeviceInfos.size(); i++){
			if(connectedDeviceInfos.get(i).getOperateThreadId() == id){
				if(connectedDeviceInfos.get(i).isAuthened()){
					return true;
				}else {
					return false;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 校验认证信息，暂且非空即可
	 * @param authenmsg
	 * @return
	 */
	private boolean vertifyAuthenMsg(String authenMsg){
		
		if(!TextUtils.isEmpty(authenMsg)){
			
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * 通过ID获取连接信息
	 * @param id
	 * @return 连接信息
	 */
	private ConnectedDeviceInfo getConnectedDeviceInfoByID(int id){
		
		for(ConnectedDeviceInfo connectedDeviceInfo : connectedDeviceInfos){
			if(connectedDeviceInfo.getOperateThreadId() == id){
				return connectedDeviceInfo;
			}
		}
		return null;
		
	}
	
	/**
	 * 改变设备的电话号码
	 * @param id
	 * @param phoneNum
	 */
	private void changePhoneNum(int id, String phoneNum){
		for(int i = 0; i < connectedDeviceInfos.size(); i++){
			if(connectedDeviceInfos.get(i).getOperateThreadId() == id){
				connectedDeviceInfos.get(i).setPhoneNum(phoneNum);
			}
		}
	}
	
	/**
	 * 添加命令历史记录
	 * @param id
	 * @param cmd
	 */
	private void addCmdHistory(int id, String cmd){
		for(int i = 0; i < connectedDeviceInfos.size(); i++){
			if(connectedDeviceInfos.get(i).getOperateThreadId() == id){
				connectedDeviceInfos.get(i).addCmdHistory(cmd);
			}
		}
	}
	
	/**
	 * 改变设备的通话状态
	 * @param id
	 * @param isCalling
	 */
	private void changeCallingState(int id, boolean isCalling){
		for(int i = 0; i < connectedDeviceInfos.size(); i++){
			if(i < connectedAuthenedDeviceInfos.size()){
				if(connectedAuthenedDeviceInfos.get(i).getOperateThreadId() == id)
					connectedAuthenedDeviceInfos.get(i).setCalling(isCalling);
			}
		}
	}
	
	/**
	 * 改变设备的认证状态
	 * @param id
	 * @param isAuthen
	 */
	private void changeAuthenState(int id, boolean isAuthen, String authenMsg){
		for(int i = 0; i < connectedDeviceInfos.size(); i++){
			if(connectedDeviceInfos.get(i).getOperateThreadId() == id){
				connectedDeviceInfos.get(i).setAuthened(isAuthen);
				if(!TextUtils.isEmpty(authenMsg)){
					connectedDeviceInfos.get(i).setAuthenMsg(authenMsg);
				}else {
					connectedDeviceInfos.get(i).setAuthenMsg("");
				}
			}
		}
	}
	
	/**
	 * 通过IP获取连接信息
	 * @param ip
	 * @return
	 */
	private ConnectedDeviceInfo getConnectedDeviceInfoByIp(String ip){
		
		for(ConnectedDeviceInfo connectedDeviceInfo : connectedDeviceInfos){
			if(connectedDeviceInfo.getIp() == ip){
				return connectedDeviceInfo;
			}
		}
		return null;
		
	}
	
	/**
	 * 刷新认证设备列表
	 */
	private void refreshAllAuthenedDevices(){
		
		if(connectedAuthenedDeviceInfos == null){
			connectedAuthenedDeviceInfos = new ArrayList<ConnectedDeviceInfo>();
		}
		//去除已经断开wifi连接的设备
		connectedDeviceInfos = mHotSpotUtil.getAllConnectedDevice(false, 300);
		
		for(int i = 0; i < connectedAuthenedDeviceInfos.size(); i++){
			ConnectedDeviceInfo connectedDeviceInfo = connectedAuthenedDeviceInfos.get(i);
			
			String ip = connectedDeviceInfo.getIp();
			String macAddr = connectedDeviceInfo.getMacAddr();
			
			boolean hasAuthenedDevice = false;
			
			for(ConnectedDeviceInfo connectedDeviceInfo2 : connectedDeviceInfos){
				String ip2 = connectedDeviceInfo2.getIp();
				String macAddr2 = connectedDeviceInfo2.getMacAddr();
				if(ip2.equals(ip) && macAddr2.equals(macAddr)){
					hasAuthenedDevice = true;
					break;
				}
			}
			
			if(!hasAuthenedDevice){
				connectedAuthenedDeviceInfos.remove(i);
				i--;
			}
		}
		
	}
	
	/**
	 * 返回所有已认证的设备
	 * @return
	 */
	public ArrayList<ConnectedDeviceInfo> getAllAuthenedDevices(){
//		if(connectedDeviceInfos == null){//是否需要判空
			refreshAllAuthenedDevices();
//		}
		return connectedAuthenedDeviceInfos;
	}

	/**
	 * 发送log信息
	 * @param log
	 */
	private void sendLog(String log){
		
		Message message = new Message();
		message.what = MainActivity.MSG_SERVER_LOG;
		message.obj = log;
	
		if(mHandler != null){
			mHandler.sendMessage(message);
		}
		
	}
	
	/**
	 * 发送信息
	 * @param what
	 * @param msg
	 */
	private void sendMsg(int what, String msg){
		
		Message message = new Message();
		message.what = what;
		message.obj = msg;
		
		if(mHandler != null){
			mHandler.sendMessage(message);
		}
		
	}
	
}