package com.yarg.robotpi.input;

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

import com.yarg.robotpi.RobotPIUIInterface;

public class ControllerInputThread extends Thread{
	
	/** Prefix expected for audio files where mouth should move. */
	private static final String SPEAK_FILE_PREFIX = "speak";

	/** Input controller to poll for data. */
	private ControllerInputData inputData;
	
	/** UI interface to update. */
	private RobotPIUIInterface uiInterface;
	
	/** Track execution state of this thread. */
	private boolean running;
	
	private static final float EPSILON = 0.05f;
	
	/** Sleep duration in milliseconds before getting input device values. */
	private static final int SLEEP = 40;
	
	/** Drive input value. */
	private float driveInput;
	
	/** Turn input value. */
	private float turnInput;
	
	/** Head lift input value. */
	private float headLiftInput;
	
	/** Head turn input value. */
	private float headTurnInput;
	
	/** Open mouth input value. */
	private boolean openMouthInput;
	
	/** Talk input value. */
	private boolean talkingInput;
	
	/** Play sound input value. */
	private boolean playSoundInput;
	
	/** Playing of sound file should cause mouth to move. */
	private boolean soundInputShouldMoveMouth;
	
	/** Select the previous sound file to play. */
	private boolean selectPreviousSoundFile = false;
	
	/** Select the next sound file to play. */
	private boolean selectNextSoundFile = false;
	
	/** Audio controls for starting and stopping audio file play back. */
	private AudioControls audioControls;
	
	private ControllerDataClient controllerDataClient;
	
	/**
	 * Create a new controller input thread instance.
	 * @param inputData Controller input to get data from.
	 * @param uiInterface UI interface to update with controller values.
	 */
	public ControllerInputThread(
			ControllerInputData inputData, RobotPIUIInterface uiInterface) {
		
		this.inputData = inputData;
		this.uiInterface = uiInterface;
		controllerDataClient = new ControllerDataClient("robotpi.local", 49801);
		running = false;
	}
	
	/**
	 * Set the audio controls to interface with.
	 * @param audioControls Audio controls to interface with.
	 */
	public void setAudioControls(AudioControls audioControls) {
		this.audioControls = audioControls;
	}
	
	/**
	 * Start the controller input thread.
	 */
	public void startControllerInputThread() {
		running = true;
		this.start();
	}
	
	/**
	 * Stop the controller input thread and safely shut it down.
	 */
	public void stopControllerInputThread() {
		running = false;
		this.interrupt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		
		while (running) {
			
			inputData.pollDevice();
			
			driveInput = inputData.getDriveInput();
			turnInput = inputData.getTurnInput();
			headLiftInput = inputData.getHeadLiftInput();
			headTurnInput = inputData.getHeadTurnInput();
			openMouthInput = inputData.getOpenMouth();
			talkingInput = inputData.getTalking();
			playSoundInput = inputData.getPlaySound();
			
			// Clamp drive, turn, head lift and head turn values.
			if (driveInput < EPSILON && driveInput > -EPSILON) {
				driveInput = 0.0f;
			}
			
			if (turnInput < EPSILON && turnInput > -EPSILON) {
				turnInput = 0.0f;
			}
			
			if (headLiftInput < EPSILON && headLiftInput > -EPSILON) {
				headLiftInput = 0.0f;
			}
			
			if (headTurnInput < EPSILON && headTurnInput > -EPSILON) {
				headTurnInput = 0.0f;
			}
			
			soundInputShouldMoveMouth = false;
			
			if (playSoundInput) {
				String audioFilePath = uiInterface.getSelectedAudioFilePath();
				if (audioFilePath != null) {
					File audioFile = new File(audioFilePath);
					audioControls.playAudioFile(audioFile);
					
					if (audioFile.getName().startsWith(SPEAK_FILE_PREFIX)) {
						soundInputShouldMoveMouth = true;
					}
					
				}
			} else {
				audioControls.stopAudioFile();
			}
			
			if (talkingInput) {
				audioControls.playMicrophone();
			} else {
				audioControls.stopMicrophone();
			}
			
			uiInterface.setDriveInput(driveInput);
			uiInterface.setTurnInput(turnInput);
			uiInterface.setHeadLiftInput(headLiftInput);
			uiInterface.setHeadTurnInput(headTurnInput);
			uiInterface.setOpenMouth(openMouthInput);
			uiInterface.setTalking(talkingInput);
			uiInterface.setPlaySound(playSoundInput);
			
			if (selectPreviousSoundFile == false) {
				if (inputData.getPreviousAudioFile()) {
					selectPreviousSoundFile = true;
					uiInterface.selectPreviousAudioFile();
				} else {
					selectPreviousSoundFile = false;
				}
			} else {
				selectPreviousSoundFile = inputData.getPreviousAudioFile();
			}
			
			if (selectNextSoundFile == false) {
				if (inputData.getNextAudioFile()) {
					selectNextSoundFile = true;
					uiInterface.selectNextAudioFile();
				} else {
					selectNextSoundFile = false;
				}
			} else {
				selectNextSoundFile = inputData.getNextAudioFile();
			}
			
			// Override talkingInput to cause robot to speak the audio file
			// being played.
			if (soundInputShouldMoveMouth) {
				talkingInput = true;
			}
			
			String dataMsg = String.format("%d,%d,%d,%d,%d,%d:", (int)(100*driveInput), (int)(100*turnInput), (int)(100*headLiftInput), (int)(100*headTurnInput), (talkingInput ? 1 : 0), (openMouthInput ? 1 : 0));
			
			controllerDataClient.sendData(dataMsg);
			
			try {
				Thread.sleep(SLEEP);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
