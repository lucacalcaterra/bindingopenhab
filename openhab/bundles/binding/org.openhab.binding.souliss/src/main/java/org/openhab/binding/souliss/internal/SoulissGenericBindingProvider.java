/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal;

import org.openhab.binding.souliss.SoulissBindingProvider;

import org.openhab.binding.souliss.internal.network.typicals.Constants;
import org.openhab.binding.souliss.internal.network.typicals.SoulissGenericTypical;
import org.openhab.binding.souliss.internal.network.typicals.SoulissNetworkParameter;
import org.openhab.binding.souliss.internal.network.typicals.SoulissT31;
import org.openhab.binding.souliss.internal.network.typicals.SoulissTypicals;
import org.openhab.binding.souliss.internal.network.typicals.StateTraslator;
import org.openhab.binding.souliss.internal.network.typicals.TypicalFactory;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * This class can parse information from the generic binding format and provides
 * souliss binding information from it. It registers as a
 * {@link soulissBindingProvider} service as well.
 * </p>
 * 
 * <p>
 * Here are some examples for valid binding configuration strings:
 * <ul>
 * <li><code>{ souliss="Europe/Berlin:de_DE" }</code>
 * <li><code>{ souliss="Europe/Berlin" }</code>
 * <li><code>{ souliss="" }</code>
 * </ul>
 * 
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissGenericBindingProvider extends
		AbstractGenericBindingProvider implements SoulissBindingProvider {
	private static Logger LOGGER = LoggerFactory
			.getLogger(TypicalFactory.class);
	public static SoulissTypicals SoulissTypicalsRecipients = new SoulissTypicals();

	public String getBindingType() {
		return "souliss";
	}

	/**
	 * This method create typicals and add it to hastable
	 * 
	 * @author Tonino Fazio
	 * @since 1.7.0
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		// CREAZIONE TIPICI
		super.processBindingConfiguration(context, item, bindingConfig);
		String[] sNameArray = bindingConfig.split("\\:");
		String sTypical = sNameArray[0];
		int iNodeID = Integer.parseInt(sNameArray[1]);
		int iSlot = Integer.parseInt(sNameArray[2]);
		byte iBit = 0;
		String sUseSlot = "";
		// gestisce i casi particolari per T31 e T1A, per la presenza del terzo
		// parametro
		if (sNameArray.length > 3) {
			if (StateTraslator.stringToSOULISSTypicalCode(sTypical) == Constants.Souliss_T31)
				sUseSlot = sNameArray[3];
			else
				iBit = Byte.parseByte(sNameArray[3]);
		}

		String sNote = item.getClass().getSimpleName();

		SoulissGenericTypical soulissTypicalNew = null;
		// gestisce il caso particolare del T31.
		// nel caso del T31 tre definizioni OH devono confluire in un unico
		// Tipico Souliss
		if (StateTraslator.stringToSOULISSTypicalCode(sTypical) == Constants.Souliss_T31) {
			soulissTypicalNew = SoulissTypicalsRecipients
					.getTypicalFromAddress(iNodeID, iSlot, 0);
			if (soulissTypicalNew != null) {
//in base al campo use slot inserisco nel tipico il nome item di riferimento				
				switch (sUXXXXseSlot) { qualche errore
				case Constants.Souliss_T31_Use_Of_Slot_SETPOINT:
					((SoulissT31) soulissTypicalNew).setsItemNameTemperatureSetpointValue(item.getName());
				case Constants.Souliss_T31_Use_Of_Slot_SWITCH:
					((SoulissT31) soulissTypicalNew).setsItemNameCommandState(item.getName());
				case Constants.Souliss_T31_Use_Of_Slot_VALUE:
					((SoulissT31) soulissTypicalNew).setsItemNameTemperatureMeasuredValue(item.getName());
					
				break;
				}
				LOGGER.info("Add parameter to T31 : " + sUseSlot);
			}
			}
		
			//creazione tipico, solo se non si tratta di un T31 al quale è stato aggiunto un parametro
			if(soulissTypicalNew==null){
				soulissTypicalNew = TypicalFactory.getClass(
						StateTraslator.stringToSOULISSTypicalCode(sTypical),
						SoulissNetworkParameter.datagramsocket,
						SoulissNetworkParameter.IPAddressOnLAN, iNodeID, iSlot,
						sNote, iBit, sUseSlot);
			}
		

		if (soulissTypicalNew != null) {
			SoulissTypicalsRecipients.addTypical(item.getName(),
					soulissTypicalNew);
			SoulissNetworkParameter.nodes = SoulissTypicalsRecipients
					.getNodeNumbers();
		} 
	}

	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
	}

}
