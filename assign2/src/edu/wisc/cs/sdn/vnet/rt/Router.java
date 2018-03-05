package edu.wisc.cs.sdn.vnet.rt;

import edu.wisc.cs.sdn.vnet.Device;
import edu.wisc.cs.sdn.vnet.DumpFile;
import edu.wisc.cs.sdn.vnet.Iface;
import java.util.Map;
import java.nio.ByteBuffer;

import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.MACAddress;


/**
 * @author Aaron Gember-Jacobson and Anubhavnidhi Abhashkumar
 */
public class Router extends Device
{	
	/** Routing table for the router */
	private RouteTable routeTable;
	
	/** ARP cache for the router */
	private ArpCache arpCache;
	
	/**
	 * Creates a router for a specific host.
	 * @param host hostname for the router
	 */
	public Router(String host, DumpFile logfile)
	{
		super(host,logfile);
		this.routeTable = new RouteTable();
		this.arpCache = new ArpCache();
	}
	
	/**
	 * @return routing table for the router
	 */
	public RouteTable getRouteTable()
	{ return this.routeTable; }
	
	/**
	 * Load a new routing table from a file.
	 * @param routeTableFile the name of the file containing the routing table
	 */
	public void loadRouteTable(String routeTableFile)
	{
		if (!routeTable.load(routeTableFile, this))
		{
			System.err.println("Error setting up routing table from file "
					+ routeTableFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static route table");
		System.out.println("-------------------------------------------------");
		System.out.print(this.routeTable.toString());
		System.out.println("-------------------------------------------------");
	}
	
	/**
	 * Load a new ARP cache from a file.
	 * @param arpCacheFile the name of the file containing the ARP cache
	 */
	public void loadArpCache(String arpCacheFile)
	{
		if (!arpCache.load(arpCacheFile))
		{
			System.err.println("Error setting up ARP cache from file "
					+ arpCacheFile);
			System.exit(1);
		}
		
		System.out.println("Loaded static ARP cache");
		System.out.println("----------------------------------");
		System.out.print(this.arpCache.toString());
		System.out.println("----------------------------------");
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
		
		/********************************************************************/
		/* TODO: Handle packets                                             */
		if(etherPacket.getEtherType() != Ethernet.TYPE_IPv4){
			System.out.println("Not IPv4");
			return;
		}
		IPv4 header = (IPv4) etherPacket.getPayload();
		short chcksum;
		chcksum =  header.getChecksum();
		header.resetChecksum();
		header.serialize();
		System.out.println(header.getChecksum());
		if(chcksum != header.getChecksum())
		{	System.out.println("bad chcksum");
			return;}
		byte ttl = (byte) (header.getTtl() - 1);
		if(ttl == 0)
			{System.out.println("ttl 0");
			return;}
		header = header.setTtl(ttl);			
		int ip = header.getDestinationAddress();
		for(Map.Entry<String, Iface> entry : this.interfaces.entrySet()){
			Iface value = entry.getValue();
			if(ip == value.getIpAddress()){
				System.out.println("ip = interface");
				return;	
			}
		}
		header.resetChecksum();
		header.serialize();
		etherPacket.setPayload((IPacket) header);
		RouteEntry rentry = routeTable.lookup(ip);
		System.out.println("Using ip address: " + ip);
		if(rentry == null)
			{System.out.println("no entryy");
			return;}
		if(rentry.getGatewayAddress() == 0){
			//ip = rentry.getDestinationAddress();
			ip = header.getDestinationAddress();
			System.out.println("Using destination address: " + ip);			
		} else {
			ip = rentry.getGatewayAddress();
			System.out.println("Using gateway address: " + ip);
		}
		//System.out.println(this.arpCache.toString());	
		ArpEntry aentry = this.arpCache.lookup(ip);
		//if(aentry == null) {System.out.println("aentry null"); aentry = this.arpCache.lookup(header.getDestinationAddress());}
		MACAddress destMAC = aentry.getMac();
		ArpEntry sentry = this.arpCache.lookup(rentry.getInterface().getIpAddress());
		//MACAddress sourceMAC = sentry.getMac();
		etherPacket.setDestinationMACAddress(destMAC.toString());
		//etherPacket.setSourceMACAddress(sourceMAC.toString());
		etherPacket.setSourceMACAddress(rentry.getInterface().getMacAddress().toString());
		sendPacket(etherPacket, rentry.getInterface());	 
		/********************************************************************/
	}
}
