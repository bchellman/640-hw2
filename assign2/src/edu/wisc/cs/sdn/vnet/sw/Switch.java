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
import java.lang.Thread;

//import edu.wisc.cs.sdn.vnet.sw.SwitchTable.java;

/**
 * @author Aaron Gember-Jacobson
 */


public class Switch extends Device
{
	private ArrayList<SwitchTable> st = new ArrayList<SwitchTable> ();	
	public static final int ML_15S = 15 * 1000; 
	//ListIterator<SwitchTable> 
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Switch(String host, DumpFile logfile)
	{
		super(host,logfile);
		// Thread thread1 = new XThread("thread1");
		// Thread thread2 = new XThread("thread2");
		
		// thread1.start();

		// long thread1ID = thread1.getId();

		// if(Thread.currentThread().getId() == thread1ID) cullTable();

		// thread2.start();
	}

/*
* Not using Threads so method below not used
*
	public void cullTable(){
		int stSize = 0;
		while(true) {
			 synchronized(st) {
				stSize = this.st.size();	
			 }	
			if (stSize == 0) {
				try {
				//The sleep() method is invoked on the main thread to cause a one second delay.
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
				}
				continue;	
			}
			 synchronized(st) {
				ListIterator<SwitchTable> it = this.st.listIterator();	
				while (it.hasNext()){
					SwitchTable temp = it.next();
					if(temp != null) {
						if((System.currentTimeMillis() - temp.getTime()) > ML_15S) {
							it.remove();
						}
					}					
				}
			}	
			try {
			//The sleep() method is invoked on the main thread to cause a one second delay.
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
			}	
		}
	}
**
*/

	/**
	 * Handle an Ethernet packet received on a specific interface.
	 * @param etherPacket the Ethernet packet that was received
	 * @param inIface the interface on which the packet was received
	 */
	public void handlePacket(Ethernet etherPacket, Iface inIface)
	{
		System.out.println("*** -> Received packet: " +
                etherPacket.toString().replace("\n", "\n\t"));
		/********************************************************************/
		MACAddress sourceMac = etherPacket.getSourceMAC();
		MACAddress destMac = etherPacket.getDestinationMAC();
		SwitchTable temp;
		
		// updating ArrayList entries.	
		
		ListIterator<SwitchTable> it = this.st.listIterator();	
		while (it.hasNext()){
			temp = it.next();
			if(temp != null) {
				if((System.currentTimeMillis() - temp.getTime()) > ML_15S) {
					it.remove();
				}
			}					
		}	

		// checking if sourceMac is in ArrayList
		int found;
		// synchronized(st) {
			found = foundMac(sourceMac);
		// }
		if(found == -1){
			temp = new SwitchTable(sourceMac, inIface);
			// synchronized(st) {
				this.st.add(temp);
			// }
		} else {
			// synchronized(st) {
				this.st.get(found).resetbirth();
			// }
		}
		
		// checking if destMac is in Arrraylist
		//synchronized(st) {
			found = foundMac(destMac);
		
			//Iface same = null;
			if(found == -1){
				for(Map.Entry<String, Iface> entry : this.interfaces.entrySet()){
					Iface value = entry.getValue();
					if(!value.equals(inIface)){
						sendPacket(etherPacket, value);
					} 
				}
			} else {
				sendPacket(etherPacket, this.st.get(found).getIface());
			}
		// }	
		/********************************************************************/
	}
/**	return value -1: Didn't find Mac
	else 	       : Found Mac
**/
	public int foundMac(MACAddress mac){
		SwitchTable temp;
		ListIterator<SwitchTable> it = this.st.listIterator();
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
