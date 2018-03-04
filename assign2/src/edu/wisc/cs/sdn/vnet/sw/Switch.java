package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import edu.wisc.cs.sdn.vnet.Device;
import net.floodlightcontroller.packet.MACAddress;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.Date;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Map;

//import edu.wisc.cs.sdn.vnet.sw.SwitchTable.java;

/**
 * @author Aaron Gember-Jacobson
 */


public class Switch extends Device
{
	ArrayList<SwitchTable> st = new ArrayList<SwitchTable> ();	
	public static final int ML_15S = 15 * 1000; 
	//ListIterator<SwitchTable> 
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
	}

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		//byte[] mac2 = etherPacket.getSourceMACAddress();
		//System.out.println("Debug");
		//MACAddress mac = new MACAddress(mac2);
		//System.out.println(Arrays.toString(mac2));//
		//mac.toString(); 
		//System.out.println("Debug");
		//inIface.toString();
		/********************************************************************/
		/* TODO: Handle packets                                             */
		MACAddress sourceMac = etherPacket.getSourceMAC();
		MACAddress destMac = etherPacket.getDestinationMAC();
		//System.out.println(sourceMac.toString());	
		//System.out.println(destMac.toString());	
		SwitchTable temp;
		
		// checking if sourceMac is in ArrayList
		int found = foundMac(sourceMac);
		if(found == -1){
			temp = new SwitchTable(sourceMac, inIface);
			st.add(temp);
		} else {
			st.get(found).resetbirth();
		}
		
		// checking if destMac is in Arrraylist
		found = foundMac(destMac);
		if(found == -1){
			for(Map.Entry<String, Iface> entry : this.interfaces.entrySet()){
				Iface value = entry.getValue();
				if(!value.equals(inIface)){
					//System.out.println(value.toString());
					sendPacket(etherPacket, value);	
				}
			}
		} else {
			//System.out.println(st.get(found).getIface().toString());
			sendPacket(etherPacket, st.get(found).getIface());
		}
	
		// updating ArrayList entries.	
		
		ListIterator<SwitchTable> it = st.listIterator();	
		while (it.hasNext()){
			temp = it.next();
			if(temp != null) {
				if((System.currentTimeMillis() - temp.getTime()) > ML_15S) {
					it.remove();
				}
			}					
		}	
		/********************************************************************/
	}
/**	return value -1: Didn't find Mac
	else 	       : Found Mac
**/
	public int foundMac(MACAddress mac){
		SwitchTable temp;
		ListIterator<SwitchTable> it = st.listIterator();
		while (it.hasNext()){
			temp = it.next();
			if(temp != null){
				if(mac.equals(temp.getMACAddress())){
					return it.previousIndex();
				}
			}
		}
		return -1;
	}
}
