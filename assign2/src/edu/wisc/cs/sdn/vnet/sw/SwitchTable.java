package edu.wisc.cs.sdn.vnet.sw;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.MACAddress;
import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.Date;

public class SwitchTable {
	public MACAddress mac;
	public Iface iface;
	public long birth;

	public SwitchTable(MACAddress mac, Iface iface){
		this.mac = mac;
		this.iface = iface;
		this.birth = System.currentTimeMillis();
	}
	public long resetbirth(){
		return (this.birth = System.currentTimeMillis());
	}
	public MACAddress getMACAddress(){
		return this.mac;
	} 
	public Iface getIface(){
		return this.iface;
	}
	public long getTime(){
		return this.birth;
	}
}
