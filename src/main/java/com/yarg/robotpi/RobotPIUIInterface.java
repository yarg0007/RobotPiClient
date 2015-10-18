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

public interface RobotPIUIInterface {

	/** 
	 * Set the drive input value to display in UI. 
	 * @param value Drive input value.
	 */
	public void setDriveInput(float value); 
	
	/** 
	 * Set the turn input value to display in UI. 
	 * @param value Turn input value.
	 */
	public void setTurnInput(float value);
	
	/** 
	 * Set the head lift input value to display in UI. 
	 * @param value Head lift input value.
	 */
	public void setHeadLiftInput(float value);
	
	/** 
	 * Set the head turn input value to display in UI. 
	 * @param value Head turn input value.
	 */
	public void setHeadTurnInput(float value);
	
	/** 
	 * Set the head turn input value to display in UI. 
	 * @param value Open mouth state (true = open, false = closed)
	 */
	public void setOpenMouth(boolean value);
	
	/**
	 * Set the talking input value to display in UI.
	 * @param value Talking state (true = talking, false = quiet)
	 */
	public void setTalking(boolean value);
	
	/**
	 * Set the play sound input value to display in UI.
	 * @param value Play sound state (true = play sound, false = quiet)
	 */
	public void setPlaySound(boolean value);
	
	/**
	 * Get the full path of the selected audio file.
	 * @return Full path of the selected audio file. Null if none selected.
	 */
	public String getSelectedAudioFilePath();
	
	/**
	 * Select the previous audio file to play back.
	 */
	public void selectPreviousAudioFile();
	
	/**
	 * Select the next audio file to play back.
	 */
	public void selectNextAudioFile();
	
}
