/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal;


import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;

import org.openhab.binding.souliss.SoulissBindingProvider;
import org.openhab.binding.souliss.internal.network.typicals.Constants;
import org.openhab.binding.souliss.internal.network.typicals.MonitorThread;
import org.openhab.binding.souliss.internal.network.typicals.RefreshHEALTYThread;
import org.openhab.binding.souliss.internal.network.typicals.RefreshSUBSCRIPTIONThread;
import org.openhab.binding.souliss.internal.network.typicals.SoulissGenericTypical;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT11;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT12;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT16;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT19;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT21;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT22;
import org.openhab.binding.souliss.internal.network.typicals.StateTraslator;

import org.openhab.binding.souliss.internal.network.typicals.SoulissNetworkParameter;
import org.openhab.binding.souliss.internal.network.udp.SendDispatcherThread;
import org.openhab.binding.souliss.internal.network.udp.UDPServerThread;

import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class load from openhab.cfg all configuration parameters 
 * Receive Command from OpenHAB, translate and send it to Souliss 
 * 
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBinding<E> extends
		AbstractActiveBinding<SoulissBindingProvider> implements ManagedService {

	private static Logger logger = LoggerFactory
			.getLogger(SoulissBinding.class);

	/**
	 * Read parameters from cfg file
	 * 
	 * @author Tonino Fazio
	 * @since 1.7.0
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		if (config != null) {
			Enumeration<String> enumConfig = config.keys();

			while (enumConfig.hasMoreElements()) {
				String sName = enumConfig.nextElement();
				logger.info("PARAMETER: " + sName + " = "
						+ (String) config.get(sName));
				if(sName.equals("IP_LAN"))
					SoulissNetworkParameter.IPAddressOnLAN = (String) config
							.get(sName);
				else if (sName.equals("REFRESH_DBSTRUCT_TIME"))
					SoulissNetworkParameter.REFRESH_DBSTRUCT_TIME = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("REFRESH_SUBSCRIPTION_TIME"))
					SoulissNetworkParameter.REFRESH_SUBSCRIPTION_TIME = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("REFRESH_HEALTY_TIME"))
					SoulissNetworkParameter.REFRESH_HEALTY_TIME = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("REFRESH_MONITOR_TIME"))
					SoulissNetworkParameter.REFRESH_MONITOR_TIME = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("SEND_DELAY"))
					SoulissNetworkParameter.SEND_DELAY = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("SEND_MIN_DELAY"))
					SoulissNetworkParameter.SEND_MIN_DELAY = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("SECURE_SEND_TIMEOUT_TO_REQUEUE"))
					SoulissNetworkParameter.SECURE_SEND_TIMEOUT_TO_REQUEUE = Long
							.parseLong((String) config.get(sName));
				else if (sName.equals("SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET"))
					SoulissNetworkParameter.SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET = Long
							.parseLong((String) config.get(sName));
				else if (sName.equals("USER_INDEX"))
					SoulissNetworkParameter.UserIndex = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("NODE_INDEX"))
					SoulissNetworkParameter.NodeIndex = Integer
							.parseInt((String) config.get(sName));
				else if (sName.equals("SERVERPORT")){
					if (config.get(sName).equals(""))
						SoulissNetworkParameter.serverPort = null;
					else
						SoulissNetworkParameter.serverPort = Integer
								.parseInt((String) config.get(sName));
				}
				
				}
			}
			SoulissNetworkParameter.setConfigured(true);
			initialize();
		}

	@Override
	/**
	 * Get the souliss's typical from the hash table and send a command
	 * @author Tonino Fazio
	 * @since 1.7.0
	 */
	public void receiveCommand(String itemName, Command command) {

		// Get the typical defined in the hash table
		SoulissGenericTypical T = SoulissGenericBindingProvider.SoulissTypicalsRecipients
				.getTypicalFromItem(itemName);
		logger.info("receiveCommand - " + itemName + " = " + command
				+ " - Typical: 0x" + Integer.toHexString(T.getType()));

		switch (T.getType()) {
		case Constants.Souliss_T11:
			SoulissT11 T11 = (SoulissT11) T;
			T11.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
					command.toString()));
			break;
		case Constants.Souliss_T12:
			SoulissT12 T12 = (SoulissT12) T;
			T12.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
					command.toString()));
			break;
		case Constants.Souliss_T16:
			SoulissT16 T16 = (SoulissT16) T;
			String cmd = command.getClass().getSimpleName();
			if (cmd.equals(Constants.Openhab_RGB_TYPE)) {
				String HSB[] = command.toString().split(",");
						 
				short RGB[] = HSBtoRGB(Float.parseFloat(HSB[0]),
						Float.parseFloat(HSB[1]), Float.parseFloat(HSB[2]));

				T16.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
						command.getClass().getSimpleName()), RGB[0], RGB[1],
						RGB[2]);
			} else
				T16.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
						command.toString()));
			break;
		case Constants.Souliss_T19:
			SoulissT19 T19 = (SoulissT19) T;
			if (command instanceof PercentType) {
				int percentToShort = (((PercentType) command).shortValue() * 255 / 100);
				T19.CommandSEND(Constants.Souliss_T1n_Set,
						Short.parseShort(String.valueOf(percentToShort)));
			} else
				T19.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
						command.toString()));
			break;
		case Constants.Souliss_T21:
			SoulissT21 T21 = (SoulissT21) T;
			T21.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
					command.toString()));
			break;
		case Constants.Souliss_T22:
			SoulissT22 T22 = (SoulissT22) T;
			T22.CommandSEND(StateTraslator.commandsOHtoSOULISS(T.getType(),
					command.toString()));
			break;
		default:
			logger.debug("Typical Unknown");
		}
	}

	/**
	 * Convert color format from HSB to RGB
	 * 
	 * @param H
	 * @param S
	 * @param B
	 * @return short RGBList[] contain RGB components
	 */
	private short[] HSBtoRGB(Float H, Float S, Float B) {
		short RGBList[] = hsvToRgb(H, S, B);
		return RGBList;
	}

	/**
	 * Start threads
	 */
	private void initialize() {
			logger.info("START");
			try {
				// Start listening on the UDP socket
				UDPServerThread Q = null;
				Q = new UDPServerThread(
						SoulissGenericBindingProvider.SoulissTypicalsRecipients);
				Q.start();

				// Start the thread that send network packets to the Souliss
				// network
				new SendDispatcherThread(
						SoulissGenericBindingProvider.SoulissTypicalsRecipients,
						SoulissNetworkParameter.SEND_DELAY,
						SoulissNetworkParameter.SEND_MIN_DELAY).start();
				// Start the thread that send back to openHAB the souliss'
				// typical values
				new MonitorThread(
						SoulissGenericBindingProvider.SoulissTypicalsRecipients,
						SoulissNetworkParameter.REFRESH_MONITOR_TIME,
						eventPublisher).start();
				// Start the thread that subscribe data from the Souliss network
				new RefreshSUBSCRIPTIONThread(Q.getSocket(),
						SoulissNetworkParameter.IPAddressOnLAN,
						SoulissNetworkParameter.REFRESH_SUBSCRIPTION_TIME)
						.start();
				new RefreshHEALTYThread(Q.getSocket(),
						SoulissNetworkParameter.IPAddressOnLAN,
						SoulissNetworkParameter.REFRESH_HEALTY_TIME).start();

			} catch (IOException e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}

	public short[] hsvToRgb(float H, float S, float V) {
		float R, G, B;

		H /= 360f;
		S /= 100f;
		V /= 100f;

		if (S == 0) {
			R = V * 255;
			G = V * 255;
			B = V * 255;
		} else {
			float var_h = H * 6;
			if (var_h == 6)
				var_h = 0; // H must be < 1
			int var_i = (int) Math.floor((double) var_h); // Or ... var_i =
															// floor( var_h )
			float var_1 = V * (1 - S);
			float var_2 = V * (1 - S * (var_h - var_i));
			float var_3 = V * (1 - S * (1 - (var_h - var_i)));

			float var_r;
			float var_g;
			float var_b;
			if (var_i == 0) {
				var_r = V;
				var_g = var_3;
				var_b = var_1;
			} else if (var_i == 1) {
				var_r = var_2;
				var_g = V;
				var_b = var_1;
			} else if (var_i == 2) {
				var_r = var_1;
				var_g = V;
				var_b = var_3;
			} else if (var_i == 3) {
				var_r = var_1;
				var_g = var_2;
				var_b = V;
			} else if (var_i == 4) {
				var_r = var_3;
				var_g = var_1;
				var_b = V;
			} else {
				var_r = V;
				var_g = var_1;
				var_b = var_2;
			}

			R = var_r * 255; // RGB results from 0 to 255
			G = var_g * 255;
			B = var_b * 255;
		}

		short RGBList[] = { (short) R, (short) G, (short) B };
		return RGBList;
	}

	@Override
	protected void execute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected long getRefreshInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
