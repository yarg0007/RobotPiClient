package com.yarg.robotpi.input;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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

public class ControllerDataClient {
	
	/** The datagram client. Setup to only allow a single client connection.*/
	private DatagramSocket clientDatagramSocket;
	
	/** Data buffer sent in packet */
	private byte[] data;
	
	/** Packet sent to server. */
	private DatagramPacket clientDatagramPacket;
	
	/** Server port to send packet to. */
	private int serverPort;
	
	/** Sever address to send packet data to. */
	private String serverAddress;
	
	/** Server to send packet to. */
	private InetAddress server;
	
	/** Maximum number of characters allowed in the data package. */
	private static final int MAX_DATA_CHAR_LEN = 32;
	
	/**
	 * Default constructor.
	 * @param serverAddress Server address to send data to.
	 * @param serverPort Server port to send data to.
	 */
	public ControllerDataClient(String serverAddress, int serverPort) {
		
		this.serverPort = serverPort;
		this.serverAddress = serverAddress;
		
		init();
	}

	public void sendData(String dataString) {
		
		if (dataString.length() >= MAX_DATA_CHAR_LEN) {
			return;
		}
		
		if (clientDatagramSocket == null || server == null) {
			return;
		}
		
		// append termination character.
		dataString = dataString + "?";
		
		data = dataString.getBytes();
		
		clientDatagramPacket = new DatagramPacket(
				data, data.length, server, this.serverPort);
		
		try {
			clientDatagramSocket.send(clientDatagramPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// -------------------------------------------------------------------------
	// Private methods
	// -------------------------------------------------------------------------
	
	/**
	 * Initialize the server and socket.
	 */
	private void init() {
		
		try {
			this.server = InetAddress.getByName(serverAddress);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}
		
		try {
			clientDatagramSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}
	}
}
