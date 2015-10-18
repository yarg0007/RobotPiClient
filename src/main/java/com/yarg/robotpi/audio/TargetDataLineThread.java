package com.yarg.robotpi.audio;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class TargetDataLineThread extends Thread {
	
	/** The datagram client. Setup to only allow a single client connection.*/
	private DatagramSocket clientDatagramSocket = null;
	
	/** Flag execution state of thread. */
	private boolean running;

	/** This is the mic audio input. */
	private TargetDataLine targetDataLine;
	
	/** Port to send datagrams over. */
	private int serverPort;
	
	/** Server address to send datagrams to. */
	private String serverAddress;
	
	/** True if microphone audio should be sent. */
	private boolean playMicrophone;
	
	/** True if should play from audio file. */
	private boolean playAudioFile;
	
	/** Audio file input stream. */
	private AudioInputStream audioFileInputStream = null;
	
	/** Audio file conversion stream. Takes input stream and writes to output.*/
	private AudioInputStream audioFileConversionStream = null;
	
	/** Delay between packets sent in milliseconds. */
	private Long streamingAudioFileDelay = 125L;

	/**
	 * Create a new target data line thread that sends the microphone data
	 * over the network to the designated address and port.
	 * @param serverAddress Server to send audio data to.
	 * @param serverPort Server port to send audio data to.
	 */
	public TargetDataLineThread(String serverAddress, int serverPort) {
		
		playAudioFile = false;
		
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
	}
	
	/**
	 * Initialize the instance. Setup Datagram client to connect and then do all
	 * the setup magic. Must be called after getting class instance.
	 */
	public void initialize() {
		
		if (running) {
			running = false;
			this.interrupt();
			
			// Let the thread terminate and then proceed.
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	
		if (clientDatagramSocket != null) {
			clientDatagramSocket.close();
		}
		
		try {
			clientDatagramSocket = new DatagramSocket();
		} catch (SocketException e1) {
			e1.printStackTrace();
			stopAudioStreamMicrophone();
			return;
		}
		
		if (targetDataLine == null) {
			
			DataLine.Info dataLineInfo = new DataLine.Info(
					TargetDataLine.class, getAudioFormat());
			
			if (!AudioSystem.isLineSupported(dataLineInfo)) {
				System.out.println("Audio capture line is not supported.");
				return;
			}

			try {
				targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
				targetDataLine.open(getAudioFormat());
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				stopAudioStreamMicrophone();
				return;
			}
			
			targetDataLine.start();
		}
	}
	
	public void startAudioStreamMicrophone() {
		
		running = true;
		this.start();
	}
	
	public void stopAudioStreamMicrophone() {
		
		running = false;
		this.interrupt();
		
		if (targetDataLine != null) {
			targetDataLine.flush();
			targetDataLine.close();
			targetDataLine = null;
		}
		
		if (clientDatagramSocket != null) {
			clientDatagramSocket.close();
			clientDatagramSocket = null;
		}
	}
	
	public void playAudioFile(File audioFile) {
		
		if (playAudioFile) {
			return;
		}

		try {
			audioFileInputStream = AudioSystem.getAudioInputStream(audioFile);
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
			playAudioFile = false;
			return;
		}

		audioFileConversionStream = 
				AudioSystem.getAudioInputStream(
						getAudioFormat(), audioFileInputStream);
		
		if (audioFileConversionStream == null) {
			playAudioFile = false;
			return;
		}
		
		playAudioFile = true;
	}
	
	public void stopAudioFile() {
		
		playAudioFile = false;
		
		try {
			if (audioFileConversionStream != null) {
				audioFileConversionStream.close();
			}
			if (audioFileInputStream != null) {
				audioFileInputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void playMicrophone() {
		playMicrophone = true;
	}
	
	public void stopMicrophone() {
		playMicrophone = false;
	}
	
	public void setStreamingAudioFileDelay(Long milliseconds) {
		streamingAudioFileDelay = milliseconds;
		System.out.println("delay: "+streamingAudioFileDelay);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {			
			
		byte[] readBuffer = new byte[getAudioBufferSizeBytes()];
		
		InetAddress address;
		try {
			address = InetAddress.getByName(serverAddress);
		} catch (UnknownHostException e) {
			System.out.println("Unreoverable error occurred during startup of audio stream. See stack trace for more information.");
			e.printStackTrace();
			return;
		}
		DatagramPacket packet = new DatagramPacket(readBuffer, readBuffer.length, 
		                                address, serverPort);
		
		int bytesRead = 0;
		
		while (running) {
			
			if (playAudioFile) {
				
				try {
					bytesRead = audioFileConversionStream.read(readBuffer);
				} catch (IOException e) {
					e.printStackTrace();
					
					System.out.println("Exception on outgoing audio stream (A). Pausing before continuing.");
					// Give it a moment to rest and then continue.
					try {
						sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						continue;
					}
				}
				
				if (bytesRead != -1) {
					packet.setData(readBuffer);
					
					// Force delay to not overfill buffer and also
					// cause stomping on stream playback at receiving end.
					try {
						sleep(streamingAudioFileDelay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						clientDatagramSocket.send(packet);
					} catch (IOException e) {
						
						System.out.println("Exception on outgoing audio stream (B). Pausing before continuing.");
						e.printStackTrace();
						
						// Give it a moment to rest and then continue.
						try {
							sleep(500);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
							continue;
						}
					}
				}
		
			} else if (playMicrophone) {
				
				int cnt = targetDataLine.read(readBuffer, 0,
						readBuffer.length);

				if (cnt > 0) {
					
					packet.setData(readBuffer);
					
					try {
						clientDatagramSocket.send(packet);
					} catch (IOException e) {

						System.out.println("Exception on outgoing audio stream (C). Pausing before continuing.");
						e.printStackTrace();
						continue;
					}
				}
			}
		}
	}

	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Get the audio format.
	 * @return Audio format to use for recording.
	 */
	private AudioFormat getAudioFormat() {
		
			float sampleRate = 44100.0f; 
			int sampleSizeInBits = 16; 
			int channels = 1; 
			boolean signed = true; 
			boolean bigEndian = true; 
			
			return new AudioFormat(
				sampleRate,
				sampleSizeInBits,
				channels,
				signed,
				bigEndian);
	}
	
	/**
	 * Size of the playback buffer in bytes.
	 * @return Size of buffer
	 */
	private int getAudioBufferSizeBytes() {

        int frameSizeInBytes = getAudioFormat().getFrameSize();
        int bufferLengthInFrames = targetDataLine.getBufferSize() / 8;
        int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
        return bufferLengthInBytes;
	}
}
