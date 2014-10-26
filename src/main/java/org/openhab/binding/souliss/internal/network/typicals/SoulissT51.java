package org.openhab.binding.souliss.internal.network.typicals;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.State;

public class SoulissT51 extends SoulissGenericTypical {
//i parametri sSoulissNode, iSlot, Type, State vengono memorizzati nell'istanza della classe che estendo
	
	public SoulissT51(String sSoulissNodeIPAddress, String sSoulissNodeIPAddressOnLAN, int iIDNodo, int iSlot, String sOHType) {
		super();
		this.setSlot(iSlot);
		this.setSoulissNodeID(iIDNodo);
		this.setType(Constants.Souliss_T53_HumiditySensor);
		this.setNote(sOHType);
		
	}
	@Override
	public State getOHState() {
		String sOHState=StateTraslator.statesSoulissToOH(this.getNote(), this.getType(),(short)this.getState());
		if (sOHState == null) { 
			if (!Float.isNaN(this.getState())){
				return DecimalType.valueOf(Float.toString(this.getState()));
			} else return null;
		}
		else return DecimalType.valueOf(sOHState);
	}
}
