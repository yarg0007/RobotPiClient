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

/**
 * Interface for accessing device input.
 * 
 * @author Ben Yarger
 *
 */
public interface ControllerInputData {

	/**
	 * Confirm that the controller is connected.
	 * @return True if controller is connected and functioning, false otherwise.
	 */
	public boolean controllerConnected();
	
	/**
	 * Get the name of the connected device.
	 * @return Name of the connected device.
	 */
	public String getNameOfConnectedDevice();
	
	/**
	 * Poll device for data.
	 */
	public void pollDevice();
	
	/**
	 * Get drive direction input data. Values are expected to be in the range
	 * from 1.0f to -1.0f inclusive. Bounds equate to full throttle forwards and
	 * backwards respectively.
	 * @return Drive input value.
	 */
	public float getDriveInput();
	
	/**
	 * Get turn direction input data. Values are expected to be in the range
	 * from 1.0f to -1.0f inclusive. Bounds equate to full turn right or left
	 * respectively.
	 * @return Turn input value.
	 */
	public float getTurnInput();
	
	/**
	 * Get head lift direction input data. Values are expected to be in the 
	 * range from 1.0f to -1.0f inclusive. Bounds equate to full look up or down
	 * respectively.
	 * @return Head lift input value.
	 */
	public float getHeadLiftInput();
	
	/**
	 * Get head turn direction input value. Values are expected to be in the 
	 * range from 1.0f to -1.0f inclusive. Bounds equate to full turn right or
	 * left respectively.
	 * @return Head turn input value.
	 */
	public float getHeadTurnInput();
	
	/**
	 * Open mouth fully and hold it there while this returns true.
	 * @return True to open mouth, false to close mouth.
	 */
	public boolean getOpenMouth();
	
	/**
	 * Returns true when audio input should be sent to the robot.
	 * @return True to send audio input, false otherwise.
	 */
	public boolean getTalking();
	
	/**
	 * True to play sound.
	 * @return True to play sound, false otherwise.
	 */
	public boolean getPlaySound();
	
	/**
	 * True if the previous audio file should be selected for play back.
	 * False otherwise.
	 * @return True to select previous audio file.
	 */
	public boolean getPreviousAudioFile();
	
	/**
	 * True if the next audio file should be selected for play back. False
	 * otherwise.
	 * @return True to select next audio file.
	 */
	public boolean getNextAudioFile();
	
	/**
	 * Serialize input data for debugging purposes.
	 * @return Serialize data.
	 */
	public String serializeData();
	
}
