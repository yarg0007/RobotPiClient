package com.yarg.robotpi;

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.yarg.robotpi.audio.AudioStreamClient;
import com.yarg.robotpi.input.ControllerInputThread;
import com.yarg.robotpi.input.gamepad.GamepadInput;

public class RobotPi extends JFrame implements RobotPIUIInterface, ActionListener, ChangeListener{
	
	private static final long serialVersionUID = -6126624868213630860L;

	private static final int AUDIO_PACKET_DELAY_DEFAULT = 125;
	
	private static final int AUDIO_PACKET_DELAY_MAX = 1000;
	
	private static final int AUDIO_PACKET_DELAY_MIN = 10;
	
	private Process videoReceiverProcess;
	
	private static final String REFRESH_BUTTON_LABEL = "Refresh Sounds";
	
	private static final Dimension windowSize = new Dimension(300, 300);
	
	private JLabel driveInputLabel;
	
	private JLabel turnInputLabel;
	
	private JLabel headLiftInputLabel;
	
	private JLabel headTurnInputLabel;
	
	private JLabel openMouthLabel;
	
	private JLabel talkingLabel;
	
	private JLabel playSoundLabel;
	
	private JSpinner audioPacketDelaySpinner;
	
	private JComboBox<String> audioFilesList;
	
	private JButton refreshAudioFileListButton;
	
	private String[] audioFiles = new String[0];
	
	private ControllerInputThread inputThread;
	
	private AudioStreamClient audioStreamClient;
	
	private String soundFileDirectory;
	
	public RobotPi() {
		initialize();
		
		inputThread = new ControllerInputThread(new GamepadInput(), this);
		audioStreamClient = new AudioStreamClient();
		
		inputThread.setAudioControls(audioStreamClient);
		
		inputThread.startControllerInputThread();
		audioStreamClient.startAudioStream();
		audioStreamClient.setAudioFilePacketDelay(AUDIO_PACKET_DELAY_DEFAULT);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RobotPi robotPi = new RobotPi();
		robotPi.setVisible(true);
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	private void initialize() {
		
		videoReceiverProcess = null;
		
		try {
			videoReceiverProcess = Runtime.getRuntime().exec("/usr/local/bin/gst-launch-1.0 -v udpsrc port=5000 ! gdpdepay ! rtph264depay ! avdec_h264 ! videoconvert ! videoflip method=horizontal-flip ! autovideosink sync=false");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Path currentRelativePath = Paths.get("");
		String workDir = currentRelativePath.toAbsolutePath().toString();
		soundFileDirectory = workDir+File.separator+"sounds";
		System.out.println("Sound file directory: "+soundFileDirectory);
		
		// Get the menu name right for Mac.
	    System.setProperty("apple.laf.useScreenMenuBar", "true");
	    System.setProperty(
	    		"com.apple.mrj.application.apple.menu.about.name", 
	    		"RobotPI");
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setMinimumSize(windowSize);
		this.setMaximumSize(windowSize);
		this.setPreferredSize(windowSize);
		
		this.getContentPane().setLayout(
				new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		// --------------------------------------
		// Drive panel
		// --------------------------------------
		JPanel drivePanel = new JPanel();
		drivePanel.setLayout(new BoxLayout(drivePanel, BoxLayout.X_AXIS));
		
		JLabel driveInputTitle = new JLabel("Drive:");
		drivePanel.add(driveInputTitle);
		drivePanel.add(Box.createHorizontalGlue());
		
		driveInputLabel = new JLabel("0.0");
//		driveInputLabel.setForeground(Color.GREEN);
		drivePanel.add(driveInputLabel);
		this.getContentPane().add(drivePanel);
		
		// --------------------------------------
		// Turn panel
		// --------------------------------------
		JPanel turnPanel = new JPanel();
		turnPanel.setLayout(new BoxLayout(turnPanel, BoxLayout.X_AXIS));
		
		JLabel turnInputTitle = new JLabel("Turn:");
		turnPanel.add(turnInputTitle);
		turnPanel.add(Box.createHorizontalGlue());
		
		turnInputLabel = new JLabel("0.0");
//		turnInputLabel.setForeground(Color.GREEN);
		turnPanel.add(turnInputLabel);
		this.getContentPane().add(turnPanel);
		
		// --------------------------------------
		// Head lift panel
		// --------------------------------------
		JPanel headLiftPanel = new JPanel();
		headLiftPanel.setLayout(new BoxLayout(headLiftPanel, BoxLayout.X_AXIS));
		
		JLabel headLiftTitle = new JLabel("Head Lift:");
		headLiftPanel.add(headLiftTitle);
		headLiftPanel.add(Box.createHorizontalGlue());
		
		headLiftInputLabel = new JLabel("0.0");
//		headLiftInputLabel.setForeground(Color.GREEN);
		headLiftPanel.add(headLiftInputLabel);
		this.getContentPane().add(headLiftPanel);
		
		// --------------------------------------
		// Head turn panel
		// --------------------------------------
		JPanel headTurnPanel = new JPanel();
		headTurnPanel.setLayout(new BoxLayout(headTurnPanel, BoxLayout.X_AXIS));
		
		JLabel headTurnTitle = new JLabel("Head Turn:");
		headTurnPanel.add(headTurnTitle);
		headTurnPanel.add(Box.createHorizontalGlue());
		
		headTurnInputLabel = new JLabel("0.0");
//		headTurnInputLabel.setForeground(Color.GREEN);
		headTurnPanel.add(headTurnInputLabel);
		this.getContentPane().add(headTurnPanel);
		
		// -------------------------------------
		// Open mouth panel
		// -------------------------------------
		JPanel openMouthPanel = new JPanel();
		openMouthPanel.setLayout(
				new BoxLayout(openMouthPanel, BoxLayout.X_AXIS));
		
		JLabel openMouthTitle = new JLabel("Open Mouth (5):");
		openMouthPanel.add(openMouthTitle);
		openMouthPanel.add(Box.createHorizontalGlue());
		
		openMouthLabel = new JLabel("false");
		openMouthLabel.setForeground(Color.RED);
		openMouthPanel.add(openMouthLabel);
		this.getContentPane().add(openMouthPanel);
		
		// --------------------------------------
		// Talking panel
		// --------------------------------------
		JPanel talkingPanel = new JPanel();
		talkingPanel.setLayout(new BoxLayout(talkingPanel, BoxLayout.X_AXIS));
		
		JLabel talkingTitle = new JLabel("Talking (6):");
		talkingPanel.add(talkingTitle);
		talkingPanel.add(Box.createHorizontalGlue());
		
		talkingLabel = new JLabel("false");
		talkingLabel.setForeground(Color.RED);
		talkingPanel.add(talkingLabel);
		this.getContentPane().add(talkingPanel);
		
		// --------------------------------------
		// Play sound
		// --------------------------------------
		JPanel playSoundPanel = new JPanel();
		playSoundPanel.setLayout(
				new BoxLayout(playSoundPanel, BoxLayout.X_AXIS));
		
		JLabel playSoundTitle = new JLabel("Play Sound (8):");
		playSoundPanel.add(playSoundTitle);
		playSoundPanel.add(Box.createHorizontalGlue());
		
		playSoundLabel = new JLabel("false");
		playSoundLabel.setForeground(Color.RED);
		playSoundPanel.add(playSoundLabel);
		this.getContentPane().add(playSoundPanel);
		
		// --------------------------------------
		// Change audio file packet delay
		// --------------------------------------
		JPanel audioPacketDelaySpinnerPanel = new JPanel();
		audioPacketDelaySpinnerPanel.setLayout(new BoxLayout(audioPacketDelaySpinnerPanel, BoxLayout.X_AXIS));
		
		JLabel audioPacketDelayLabel = new JLabel("Audio packet delay");
		audioPacketDelaySpinnerPanel.add(audioPacketDelayLabel);
		audioPacketDelaySpinnerPanel.add(Box.createHorizontalGlue());
		
		SpinnerNumberModel spinnerNumerModel = new SpinnerNumberModel();//new SpinnerNumberModel(AUDIO_PACKET_DELAY_DEFAULT, 10, 1000, 1);
		spinnerNumerModel.setValue(AUDIO_PACKET_DELAY_DEFAULT);
		audioPacketDelaySpinner = new JSpinner(spinnerNumerModel);
		
		Dimension audioPacketDelaySpinnerDimension = new Dimension(100, 30);
		audioPacketDelaySpinner.setMinimumSize(audioPacketDelaySpinnerDimension);
		audioPacketDelaySpinner.setMaximumSize(audioPacketDelaySpinnerDimension);
		audioPacketDelaySpinner.setPreferredSize(audioPacketDelaySpinnerDimension);
		audioPacketDelaySpinner.addChangeListener(this);
		
		audioPacketDelaySpinnerPanel.add(audioPacketDelaySpinner);
		this.getContentPane().add(audioPacketDelaySpinnerPanel);
		
		// --------------------------------------
		// Refresh button panel
		// --------------------------------------
		
		this.getContentPane().add(Box.createVerticalGlue());
		
		Dimension refreshAudioFileListButtonDimension = new Dimension(windowSize.width-4, 30);
		refreshAudioFileListButton = new JButton(REFRESH_BUTTON_LABEL);
		refreshAudioFileListButton.setMinimumSize(refreshAudioFileListButtonDimension);
		refreshAudioFileListButton.setPreferredSize(refreshAudioFileListButtonDimension);
		refreshAudioFileListButton.setMaximumSize(refreshAudioFileListButtonDimension);
		refreshAudioFileListButton.addActionListener(this);
		
		JPanel refreshButtonPanel = new JPanel();
		refreshButtonPanel.setLayout(new BoxLayout(refreshButtonPanel, BoxLayout.X_AXIS));
		refreshButtonPanel.add(refreshAudioFileListButton);
		this.getContentPane().add(refreshButtonPanel);
		
		// --------------------------------------
		// Audio list panel
		// --------------------------------------
		
		Dimension audioFileListDimension = new Dimension(windowSize.width-4, 30);
		
		JPanel audioFilePanel = new JPanel();
		audioFilePanel.setLayout(
				new BoxLayout(audioFilePanel, BoxLayout.X_AXIS));
		
		audioFilesList = new JComboBox<String>();
		audioFilesList.addActionListener(this);
		audioFilesList.setMinimumSize(audioFileListDimension);
		audioFilesList.setMaximumSize(audioFileListDimension);
		audioFilesList.setPreferredSize(audioFileListDimension);
		refreshAudioFileList();
		
		audioFilePanel.add(audioFilesList);
		this.getContentPane().add(audioFilePanel);
		
		// --------------------------------------
		// Magic bit of code to close threads when exiting.
		// --------------------------------------
		Runtime.getRuntime().addShutdownHook(new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				shutdown();
			}

		});
	}
	
	/**
	 * Refresh the audio file combo box list.
	 */
	private void refreshAudioFileList() {
		
		File soundDirectory = new File(soundFileDirectory);
		File[] soundFiles = soundDirectory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".wav")){// || name.endsWith(".mp3")) {
					return true;
				}
				return false;
			}
		});
		
		if (soundFiles == null) {
			return;
		}
		
		// Clear out the old.
		audioFilesList.removeAllItems();
		
		// Create and add the new.
		audioFiles = new String[soundFiles.length];
		
		for (int i = 0; i < soundFiles.length; i++) {
			audioFiles[i] = soundFiles[i].getName();
			audioFilesList.addItem(audioFiles[i]);
		}
	}
	
	/**
	 * Perform all shutdown operations to safely close down the robot.
	 */
	private void shutdown() {
		System.out.println("Shutdown process...");
		
		videoReceiverProcess.destroy();
		
		inputThread.stopControllerInputThread();
		audioStreamClient.stopAudioStream();
		System.out.println("Everything shutdown.");
	}

	
	
	// -------------------------------------------------------------------------
	// Required by RobotPiUIInterface
	// -------------------------------------------------------------------------
	
	@Override
	public void setDriveInput(float value) {
		driveInputLabel.setText(String.format("%06f",value));
	}

	@Override
	public void setTurnInput(float value) {
		turnInputLabel.setText(String.format("%06f", value));
	}

	@Override
	public void setHeadLiftInput(float value) {
		headLiftInputLabel.setText(String.format("%06f", value));
	}

	@Override
	public void setHeadTurnInput(float value) {
		headTurnInputLabel.setText(String.format("%06f", value));
	}

	@Override
	public void setOpenMouth(boolean value) {
		openMouthLabel.setText(String.valueOf(value));
		
		if (value) {
			openMouthLabel.setForeground(Color.GREEN);
		} else {
			openMouthLabel.setForeground(Color.RED);
		}
	}

	@Override
	public void setTalking(boolean value) {
		talkingLabel.setText(String.valueOf(value));
		
		if (value) {
			talkingLabel.setForeground(Color.GREEN);
		} else {
			talkingLabel.setForeground(Color.RED);
		}
	}

	@Override
	public void setPlaySound(boolean value) {
		playSoundLabel.setText(String.valueOf(value));
		
		if (value) {
			playSoundLabel.setForeground(Color.GREEN);
		} else {
			playSoundLabel.setForeground(Color.RED);
		}
	}
	
	@Override
	public String getSelectedAudioFilePath()
	{
		String fileName = (String) audioFilesList.getSelectedItem();
		
		if (fileName.equals("")) {
			return null;
		}
		
		fileName = soundFileDirectory + File.separator + fileName;
		return fileName;
	}
	

	@Override
	public void selectPreviousAudioFile() {
		int index = audioFilesList.getSelectedIndex();
		
		// Can't select any further back than the initial item in the list.
		if (index <= 0) {
			return;
		}
		
		audioFilesList.setSelectedIndex(--index);
	}

	@Override
	public void selectNextAudioFile() {
		int index = audioFilesList.getSelectedIndex();
		
		// Can't go beyond the last item.
		if (index >= (audioFilesList.getItemCount()-1)) {
			return;
		}
		
		audioFilesList.setSelectedIndex(++index);
	}
	
	// -------------------------------------------------------------------------
	// Methods required by ActionListener
	// -------------------------------------------------------------------------

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == refreshAudioFileListButton) {
			refreshAudioFileList();
		} else if (e.getSource() == audioFilesList) {
			
		}
	}

	// -------------------------------------------------------------------------
	// Methods required by ChangeListener
	// -------------------------------------------------------------------------
	
	@Override
	public void stateChanged(ChangeEvent e) {
		
		if (e.getSource() == audioPacketDelaySpinner) {
			
			SpinnerNumberModel numberModel = (SpinnerNumberModel) audioPacketDelaySpinner.getModel();
			Number value = numberModel.getNumber();
			
			if (value.intValue() > AUDIO_PACKET_DELAY_MAX) {
				audioPacketDelaySpinner.setValue(AUDIO_PACKET_DELAY_MAX);
//				audioStreamClient.setAudioFilePacketDelay(AUDIO_PACKET_DELAY_MAX);
//				System.out.println("CAP AT MAX");
			} else if (value.intValue() < AUDIO_PACKET_DELAY_MIN) {
				audioPacketDelaySpinner.setValue(AUDIO_PACKET_DELAY_MIN);
//				audioStreamClient.setAudioFilePacketDelay(AUDIO_PACKET_DELAY_MIN);
//				System.out.println("CAP AT MIN");
			} else {
				audioStreamClient.setAudioFilePacketDelay(value.longValue());
//				System.out.println("Changed delay to: "+value.longValue());
			}
		}
	}
}
