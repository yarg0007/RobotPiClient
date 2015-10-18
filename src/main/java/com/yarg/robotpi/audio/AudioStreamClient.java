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

import com.yarg.robotpi.audio.SourceDataLineThread;
import com.yarg.robotpi.audio.TargetDataLineThread;
import com.yarg.robotpi.input.AudioControls;

public class AudioStreamClient implements AudioControls{
	
	private int RECEIVE_PORT = 49808;
	
	private int SEND_PORT = 49809;
	
	private String SERVER_ADDRESS = "robotpi.local";

	SourceDataLineThread incomingStream;
	TargetDataLineThread microphoneStream;
	
	public AudioStreamClient() {
		incomingStream = new SourceDataLineThread(RECEIVE_PORT);
		microphoneStream = new TargetDataLineThread(SERVER_ADDRESS, SEND_PORT);
		
		incomingStream.initialize();
		microphoneStream.initialize();
	}

	public void startAudioStream() {
		incomingStream.startAudioStreamSpeakers();
		microphoneStream.startAudioStreamMicrophone();
	}
	
	public void stopAudioStream() {
		incomingStream.stopAudioStreamSpeakers();
		microphoneStream.stopAudioStreamMicrophone();
	}

	// -------------------------------------------------------------------------
	// Methods required by AudioControls
	// -------------------------------------------------------------------------
	
	@Override
	public void playAudioFile(File audioFile) {
		stopMicrophone();
		microphoneStream.playAudioFile(audioFile);
	}

	@Override
	public void playMicrophone() {
		stopAudioFile();
		microphoneStream.playMicrophone();
	}

	@Override
	public void stopAudioFile() {
		microphoneStream.stopAudioFile();
	}

	@Override
	public void stopMicrophone() {
		microphoneStream.stopMicrophone();
	}
	
	@Override
	public void setAudioFilePacketDelay(long milliseconds) {
		microphoneStream.setStreamingAudioFileDelay(milliseconds);
	}
}
