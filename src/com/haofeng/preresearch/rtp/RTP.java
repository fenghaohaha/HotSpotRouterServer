package com.haofeng.preresearch.rtp;

import java.net.DatagramSocket;

import com.haofeng.preresearch.shotspotsetting.config.HotSpotServerConfig;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import jlibrtp.DataFrame;
import jlibrtp.Participant;
import jlibrtp.RTPAppIntf;
import jlibrtp.RTPSession;

public class RTP implements RTPAppIntf{

	public static final int RTP_PORT = HotSpotServerConfig.port+1;
	public static final int RTCP_PORT = HotSpotServerConfig.port+2;
	private AudioRecord audioRecord;
	private AudioTrack audioTrack;
	private int buffSize;
	private byte[] buffer;

	public RTPSession rtpSession = null;
	
	
	
	public RTP(String desIP) {
		DatagramSocket rtpSocket = null;
		DatagramSocket rtcpSocket = null;
		
		try {
			rtpSocket = new DatagramSocket(RTP_PORT);
			rtcpSocket = new DatagramSocket(RTCP_PORT);
		} catch (Exception e) {
			System.out.println("RTPSession failed to obtain port");
		}
		
		
		rtpSession = new RTPSession(rtpSocket, rtcpSocket);
		rtpSession.naivePktReception(true);
		rtpSession.RTPSessionRegister(this,null, null);
		
		//作为接收端需初始化语音资源
		buffSize = AudioRecord.getMinBufferSize(AudioConfig.frequence, AudioConfig.channelConfig, AudioConfig.audioEncoding);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AudioConfig.frequence, AudioConfig.channelConfig, AudioConfig.audioEncoding, buffSize);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AudioConfig.frequence, AudioConfig.channelConfig, AudioConfig.audioEncoding, buffSize, AudioTrack.MODE_STREAM);
		buffer = new byte[buffSize];

		Participant p = new Participant(desIP,RTP_PORT,RTCP_PORT);
		rtpSession.addParticipant(p);
	
	}

	@Override
	public void receiveData(DataFrame frame, Participant participant) {
		audioTrack.play();
		audioTrack.write(frame.getConcatenatedData(), 0,frame.getConcatenatedData().length);
	}

	@Override
	public void userEvent(int type, Participant[] participant) {
	}

	@Override
	public int frameSize(int payloadType) {
		return 1;
	}

}
