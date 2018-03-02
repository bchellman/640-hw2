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

	public long resetbirth(){
		this.birth = System.curretTimeMillis();
	}
}
