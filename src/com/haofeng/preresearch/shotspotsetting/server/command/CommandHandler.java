package com.haofeng.preresearch.shotspotsetting.server.command;

import java.io.IOException;

public class CommandHandler {
	
	/**
	 * 封装接收到的字符串命令
	 * @param receivedCmd
	 * @return
	 */
	public static BaseCommand packageReceivedCmd(String receivedCmd) throws IOException{
		BaseCommand command = null;
		
		if(!isLegalCommand(receivedCmd)){
			throw new IOException(" 收到了非法信息");
		}
		
		String[] cmds = receivedCmd.split(",");
		if(cmds.length == 5){
			String cmd = cmds[0].split("=")[1];
			String ip = cmds[1].split("=")[1];
			String macAddr = cmds[2].split("=")[1];
			String authenMsg = cmds[3].split("=")[1];
			
			String stateMsg = "";
			
			if(receivedCmd.endsWith("=")){
				stateMsg = "";
			}else {
				stateMsg = cmds[4].split("=")[1] + "";
			}
			
			command = new BaseCommand(cmd, ip, macAddr, authenMsg, stateMsg);
		}
		
		return command;
	}
	
	/**
	 * 收到的消息是否合法<br>
	 * 随后再用正则表达式匹配
	 * @param cmd
	 * @return
	 * 
	 */
	public static boolean isLegalCommand(String cmd){
		
		if(cmd.contains("cmd=")&&cmd.contains(", stateMsg=")){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 解析命令为字符串
	 * @param command
	 * @return
	 */
	public static String parsePackagedCmd(BaseCommand command){
		String cmd = "";
		cmd = command.toString();
		
		return cmd;
	}
}
