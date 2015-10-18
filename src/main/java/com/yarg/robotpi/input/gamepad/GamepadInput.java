package com.yarg.robotpi.input.gamepad;

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

import com.yarg.robotpi.input.ControllerInputData;

import net.java.games.input.*;

public class GamepadInput implements ControllerListener, ControllerInputData {
	
	/** Gamepad controller instance to poll for input. */
	private Controller gamepadController;
	
	/** Move forwards and backwards using right axis vertical values. */
	private static final byte DRIVE_INDEX = 15;
	
	/** Default value for drive input. */
	private static final float DRIVE_DEFAULT_VALUE = 0.0f;
	
	/** Drive input value. */
	private float driveValue;
	
	/** Turn left and right using left axis horizontal values. */
	private static final byte TURN_INDEX = 14;
	
	/** Default value for turn input. */
	private static final float TURN_DEFAULT_VALUE = 0.0f;
	
	/** Turn input value. */
	private float turnValue;
	
	/** Look up and down using left axis vertical values. */
	private static final byte HEAD_LIFT_INDEX = 13;
	
	/** Default value for head lift. */
	private static final float HEAD_LIFT_DEFAULT_VALUE = 0.0f;
	
	/** Head lift input value. */
	private float headLiftValue;
	
	/** Turn the head left and right using right axis horizontal values. */
	private static final byte HEAD_TURN_INDEX = 12;
	
	/** Default value for head turn. */
	private static final float HEAD_TURN_DEFAULT_VALUE = 0.0f;
	
	/** Head turn input value. */
	private float headTurnValue;
	
	/** Open the mouth. */
	private static final byte OPEN_MOUTH_INDEX = 4;
	
	/** Default value for open mouth. */
	private static final boolean OPEN_MOUTH_DEFAULT_VALUE = false;
	
	/** Open mouth input value. */
	private boolean openMouthValue;
	
	/** Allows audio to play through puppet when talking. */
	private static final byte TALK_INDEX = 5;
	
	/** Default value for talking. */
	private static final boolean TALK_DEFAULT_VALUE = false;
	
	/** Taling input value. */
	private boolean talkValue;
	
	/** Plays the fart sound. */
	private static final byte SOUND_INDEX = 7;
	
	/** Default value for farting. */
	private static final boolean SOUND_DEFAULT_VALUE = false;
	
	/** Farting input value. */
	private boolean soundValue;
	
	/** Keeps drive input from being sent out. */
	private static final byte STATIONARY_INDEX = 6;

	/** Default value for stationary input. */
	private static final boolean STATIONARY_DEFAULT_VALUE = true;
	
	/** Stationary input value. */
	private boolean stationaryValue;
	
	/** Directional pad input index. */
	private static final byte DIRECTIONAL_PAD_INDEX = 16;
	
	/** Value for when d-pad up is being pressed. */
	private static final float DIRECTIONAL_PAD_UP = 0.25f;
	
	/** Value for when d-pad down is being pressed. */
	private static final float DIRECTONAL_PAD_DOWN = 0.75f;
	
	/** Directional pad up pressed flag. */
	private boolean upPressed;
	
	/** Directional pad down pressed flag. */
	private boolean downPressed;
	
	/**
	 * Create a new GamepadInput instance.
	 */
	public GamepadInput() {
		gamepadController = null;
		initializeDevice();
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Initialize a gamepad controller device. Output all of the details about
	 * the device to stdout.
	 */
	private void initializeDevice() {
		
		System.out.println("JInput version: " + Version.getVersion()); 
		
		ControllerEnvironment ce = 
				ControllerEnvironment.getDefaultEnvironment(); 
		Controller[] cs = ce.getControllers(); 
		for (int i = 0; i < cs.length; i++) {
			if (cs[i].getType() == Controller.Type.STICK) {
				synchronized (this) {
					gamepadController = cs[i];
				}
			}
		}
		
		System.out.println("GamePad identified: "+gamepadController.getName());
		
		// Examine all sub controllers - just a safety check.
		// We expect this to be empty.
		examineSubControllers(gamepadController);
		
		// Get all of the components on the controller.
		Component[] components = gamepadController.getComponents();
		
		for (Component component : components) {
			System.out.println("Component: "+component.getName());
		}
	}
	
	/**
	 * Examine the sub controllers of the device. Print to stdout anything
	 * found.
	 * @param controller Controller to inspect.
	 */
	private void examineSubControllers(Controller controller) {
		
		Controller[] controllers = controller.getControllers();
		
		for (Controller subController : controllers) {
			examineSubControllers(subController);
			System.out.println("Found subcontroller: "+subController.getName()+", of type: "+subController.getType());
		}
	}

	// -------------------------------------------------------------------------
	// Required by ControllerListener
	// Respond to controller attach and detach events.
	// -------------------------------------------------------------------------
	
	@Override
	public void controllerAdded(ControllerEvent arg0) {
		initializeDevice();
	}

	@Override
	public void controllerRemoved(ControllerEvent arg0) {
		synchronized (this) {
			gamepadController = null;
		}
	}
	
	// -------------------------------------------------------------------------
	// Required by ControllerInputData
	// Allows access to any input device data
	// -------------------------------------------------------------------------

	@Override
	public boolean controllerConnected() {
		
		if (gamepadController == null) {
			return false;
		}
		return true;
	}

	@Override
	public String getNameOfConnectedDevice() {
		
		return gamepadController.getName();
	}

	@Override
	public void pollDevice() {
		
		// Always start with default value states.
		// If we lose the device, we want to send back stable values.
		driveValue = DRIVE_DEFAULT_VALUE;
		turnValue = TURN_DEFAULT_VALUE;
		headLiftValue = HEAD_LIFT_DEFAULT_VALUE;
		headTurnValue = HEAD_TURN_DEFAULT_VALUE;
		openMouthValue = OPEN_MOUTH_DEFAULT_VALUE;
		talkValue = TALK_DEFAULT_VALUE;
		soundValue = SOUND_DEFAULT_VALUE;
		stationaryValue = STATIONARY_DEFAULT_VALUE;
		downPressed = false;
		upPressed = false;
		
		if (gamepadController != null) {
			
			if (gamepadController.poll()) {
			
				// Poll the controller state.
				Component[] components = gamepadController.getComponents();
/*				
				for (Component component : components) {
					System.out.print("["+component.getName()+":"+component.getPollData()+"]");
				}
				System.out.println("");
*/				
				// Handle analog inputs.
				
				driveValue = components[DRIVE_INDEX].getPollData();
				turnValue = components[TURN_INDEX].getPollData();
				headLiftValue = components[HEAD_LIFT_INDEX].getPollData();
				headTurnValue = components[HEAD_TURN_INDEX].getPollData();
				
				turnValue *= -1.0f;
				headTurnValue *= -1.0f;
				headLiftValue *= -1.0f;
				
				// Handle binary inputs.
				// Handle zeroing checks.
				// Need to do some sort of calibration during initialization
				
				if (components[OPEN_MOUTH_INDEX].getPollData() >= 0.9f) {
					openMouthValue = true;
				} else {
					openMouthValue = false;
				}
				
				if (components[TALK_INDEX].getPollData() >= 0.9f) {
					talkValue = true;
				} else {
					talkValue = false;
				}
				
				if (components[SOUND_INDEX].getPollData() >= 0.9f) {
					soundValue = true;
				} else {
					soundValue = false;
				}
				
				if (components[STATIONARY_INDEX].getPollData() >= 0.9f) {
					stationaryValue = true;
				} else {
					stationaryValue = false;
				}
				
				if (components[DIRECTIONAL_PAD_INDEX].getPollData() == DIRECTIONAL_PAD_UP) {
					upPressed = true;
				} else {
					upPressed = false;
				}
				
				if (components[DIRECTIONAL_PAD_INDEX].getPollData() == DIRECTONAL_PAD_DOWN) {
					downPressed = true;
				} else {
					downPressed = false;
				}
			}
		}
	}

	@Override
	public float getDriveInput() {
		
		if (stationaryValue) {
			return DRIVE_DEFAULT_VALUE;
		}
		
		return -driveValue;
	}

	@Override
	public float getTurnInput() {
		
		if (stationaryValue) {
			return TURN_DEFAULT_VALUE;
		}
		
		return turnValue;
	}

	@Override
	public float getHeadLiftInput() {
		return -headLiftValue;
	}

	@Override
	public float getHeadTurnInput() {
		return headTurnValue;
	}

	@Override
	public boolean getOpenMouth() {
		return openMouthValue;
	}

	@Override
	public boolean getTalking() {
		return talkValue;
	}

	@Override
	public boolean getPlaySound() {
		return soundValue;
	}

	@Override
	public boolean getPreviousAudioFile() {
		return upPressed;
	}

	@Override
	public boolean getNextAudioFile() {
		return downPressed;
	}
	
	@Override
	public String serializeData() {
		return String.format(
				"Input: driveDirection=%f,  " +
				"turnDirection=%f, " +
				"headLift=%f, " +
				"headTurn=%f, " +
				"openMouth=%b, " +
				"talk=%b, " +
				"playSound=%b, " +
				"stationary=%b", 
				getDriveInput(), 
				getTurnInput(), 
				getHeadLiftInput(), 
				getHeadTurnInput(), 
				getOpenMouth(), 
				getTalking(), 
				getPlaySound(), 
				stationaryValue);
	}
}
