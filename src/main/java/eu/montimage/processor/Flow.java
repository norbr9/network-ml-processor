package eu.montimage.processor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Flow{

    /**
     * Internal enum to determine the state of the flow.
     */
    private enum State{
        NEW,
        OPEN,
        FIN,
        FINACK,
        CLOSED,
    }
    /**
     * Representation of a network flow.
     * It contains an ID object (used to easily identify the flow)
     * and all the other fields of the flow
     */
    private FlowID id;
    // Start packet and last analyzed packet
    private long starttsamp = 0;
    private long lastpkgtsamp = 0;
    // Packets count (0: Up, 1: Down)
    private long[] pkg = new long[2];
    // Bytes count (0: Up, 1: Down)
    private long[] bytes = new long[2];
    // Packets statistics (0: Up, 1: Down)
    private long[] pkgAvgSize = new long[2];
    private long[] pkgMaxSize = new long[2];
    private long[] pkgMinSize = new long[2];
    // TCP Window statistic (0: Up, 1: Down)
    private long[] windowAvg = new long[2];
    private long[] windowMax = new long[2];
    private long[] windowMin = new long[2];
    // TTL statistics (0: Up, 1: Down)
    private long[] ttlAvg = new long[2];
    private long[] ttlMax = new long[2];
    private long[] ttlMin = new long[2];
    // TCP flags statistic (0: Up, 1: Down)
    private long[] fin = new long[2];
    private long[] syn = new long[2];
    private long[] rst = new long[2];
    private long[] psh = new long[2];
    private long[] ack = new long[2];
    private long[] urg = new long[2];
    // SSL Packets
    private long[] sslPkg = new long[2];
    // SSL Content Type (0: Up, 1: Down)
    private long[] chgCipher = new long[2]; // 20
    private long[] alert = new long[2]; // 21
    private long[] handshake = new long[2]; // 22
    private long[] appData = new long[2]; // 23
    private long[] heartbeat = new long[2]; // 24

    // State of the flow
    private Flow.State state;

    public Flow(FlowID id, long starttsamp){
        // This constructor creates the flow, and establishes
        // the start timestamp
        this.id = id;
        this.starttsamp = starttsamp;
        this.state = Flow.State.NEW;

        // Initialize the counters of this flow
        Arrays.stream(new int[]{0, 1}).forEach(i -> {
            this.pkg[i] = 0;
            this.bytes[i] = 0;
            this.pkgAvgSize[i] = 0;
            this.pkgMaxSize[i] = 0;
            this.pkgMinSize[i] = Long.MAX_VALUE;
            this.windowAvg[i] = 0;
            this.windowMax[i] = 0;
            this.windowMin[i] = Long.MAX_VALUE;
            this.ttlAvg[i] = 0;
            this.ttlMax[i] = 0;
            this.ttlMin[i] = Long.MAX_VALUE;
            this.sslPkg[i] = 0;
            this.fin[i] = 0;
            this.syn[i] = 0;
            this.rst[i] = 0;
            this.psh[i] = 0;
            this.ack[i] = 0;
            this.urg[i] = 0;
        });
    }

    public boolean logPkg(FlowID id, String[] pkgInfo){
        if (this.state == Flow.State.CLOSED) return false; // The flow is closed, a package cannot be logged here.
        // Get the timestamp reported by MMT:
        long tstamp = Long.parseLong(pkgInfo[3].replace(".", "")); // get rid of the "." and parse the stamp in microsec.
        // This function uses all the information of the logged packet.
        // So a displacement is required to access the statistics fields
        final int displacement = 10;
        long size = Long.parseLong(pkgInfo[displacement+0]);
        long tcpwin = Long.parseLong(pkgInfo[displacement+1]);
        long ttl = Long.parseLong(pkgInfo[displacement+2]);
        int fin = Integer.parseInt(pkgInfo[displacement+3]);
        int syn = Integer.parseInt(pkgInfo[displacement+4]);
        int rst = Integer.parseInt(pkgInfo[displacement+5]);
        int psh = Integer.parseInt(pkgInfo[displacement+6]);
        int ack = Integer.parseInt(pkgInfo[displacement+7]);
        int urg = Integer.parseInt(pkgInfo[displacement+8]);
        int contType = -1;
        try{
        	if (pkgInfo.length >= 20)
        		contType = Integer.parseInt(pkgInfo[displacement+9]);
        } catch (NumberFormatException e){ } // Nothing to do; the field has no value.

        // Determine if the packet is uplink or downlink
        // NOTE: This checking assumes that the first packet seen is the "uplink"
        int sense = 1; //uplink
        if(this.id.flowSenseOf(id)){
            sense = 0; // downlink
        }

        // Update the corresponding statistics
        // Timestamp
        if(this.starttsamp == 0)
            this.starttsamp = tstamp;
        else
            this.lastpkgtsamp = tstamp;
        // Packet counting
        this.pkg[sense]++;
        // Bytes counting
        this.bytes[sense] += size;
        // Packets statistics 
        this.pkgAvgSize[sense] += (long) ((size - this.pkgAvgSize[sense])/this.pkg[sense]);
        if(this.pkgMaxSize[sense] < size) this.pkgMaxSize[sense] = size;
        if(this.pkgMinSize[sense] > size) this.pkgMinSize[sense] = size;
        // TCP Window statistics
        this.windowAvg[sense] += (long) ((tcpwin - this.windowAvg[sense])/this.pkg[sense]);
        if(this.windowMax[sense] < tcpwin) this.windowMax[sense] = tcpwin;
        if(this.windowMin[sense] > tcpwin) this.windowMin[sense] = tcpwin;
        // TTL
        this.ttlAvg[sense] += (long) ((ttl - this.ttlAvg[sense])/this.pkg[sense]);
        if(this.ttlMax[sense] < ttl) this.ttlMax[sense] = ttl;
        if(this.ttlMin[sense] > ttl) this.ttlMin[sense] = ttl;
        // TCP flags statistics
        this.fin[sense] += fin;
        this.syn[sense] += syn;
        this.rst[sense] += rst;
        this.psh[sense] += psh;
        this.ack[sense] += ack;
        this.urg[sense] += urg;
        // Number of SSL packets
        this.sslPkg[sense] +=1;
        // Type of SSL content
        switch(contType){
            case 20:
                this.chgCipher[sense]++;
                break;
            case 21:
                this.alert[sense]++;
                break;
            case 22:
                this.handshake[sense]++;
                break;
            case 23:
                this.appData[sense]++;
                break;
            case 24:
                this.heartbeat[sense]++;
                break;
            default:
                break;
        }
        // Update the stat of the flow depending on the FIN flag
        // WARNING: This behavior assumes the FIN, FIN-ACK, ACK packets arrive IN ORDER!!!!
        // At this point, the state of the connection is, at least, open
        // so if I see a FIN packet, I need to update the state.
        if(fin == 1){
            // This is a FIN Packet. Change the state depending on the ACK flag
            if(ack == 0){
                // This is a simple FIN pkg
                this.state = Flow.State.FIN;
            } else {
                // This is a FIN-ACK pkg
                this.state = Flow.State.FINACK;
            }
        }
        // Check if this pkg is the last ACK
        if(this.state == Flow.State.FINACK && ack == 1){
            // This IS the last ACK packet, therefore the connection is closed.
            this.state = Flow.State.CLOSED;
        }
        return true;
    }

    public FlowID getFlowID(){
        return this.id;
    }

    private List<String> getFlowStatistics(){
        // Create the list of results to return
        List<String> results = new LinkedList<>();
        // Duration
        long duration = this.lastpkgtsamp - this.starttsamp;
        results.add(Long.toString(duration));
        // Packets and bytes in 1 second (up and down)
        duration = duration / 1000000; // microsec to sec
        if (duration > 0) {
		    results.add(Float.toString( (float) this.pkg[0] / (float) duration));
		    results.add(Float.toString( (float)this.pkg[1] / (float)duration));
		    results.add(Float.toString((float) this.bytes[0] / (float) duration));
		    results.add(Float.toString((float) this.bytes[1] / (float)duration));
        } else {
        	results.add("0");
  		    results.add("0");
  		    results.add("0");
  		    results.add("0");
        }
        // Max, Min and Avg pkg size in up and downlink
        results.add(Long.toString(this.pkgMaxSize[0]));
        results.add(Long.toString(this.pkgMinSize[0]));
        results.add(Long.toString(this.pkgAvgSize[0]));
        results.add(Long.toString(this.pkgMaxSize[1]));
        results.add(Long.toString(this.pkgMinSize[1]));
        results.add(Long.toString(this.pkgAvgSize[1]));
        // Max, Min and Avg TCP window size in up and downlink
        results.add(Long.toString(this.windowMax[0]));
        results.add(Long.toString(this.windowMin[0]));
        results.add(Long.toString(this.windowAvg[0]));
        results.add(Long.toString(this.windowMax[1]));
        results.add(Long.toString(this.windowMin[1]));
        results.add(Long.toString(this.windowAvg[1]));
        // Max, Min and Avg TTL value in up and downlink
        results.add(Long.toString(this.ttlMax[0]));
        results.add(Long.toString(this.ttlMin[0]));
        results.add(Long.toString(this.ttlAvg[0]));
        results.add(Long.toString(this.ttlMax[1]));
        results.add(Long.toString(this.ttlMin[1]));
        results.add(Long.toString(this.ttlAvg[1]));
        // Percentage of packets for each flag in up and downlink.
        results.add(Double.toString(((double) this.fin[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.syn[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.rst[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.psh[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.ack[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.urg[0]) / ((double) this.pkg[0])));
        results.add(Double.toString(((double) this.fin[1]) / ((double) this.pkg[1])));
        results.add(Double.toString(((double) this.syn[1]) / ((double) this.pkg[1])));
        results.add(Double.toString(((double) this.rst[1]) / ((double) this.pkg[1])));
        results.add(Double.toString(((double) this.psh[1]) / ((double) this.pkg[1])));
        results.add(Double.toString(((double) this.ack[1]) / ((double) this.pkg[1])));
        results.add(Double.toString(((double) this.urg[1]) / ((double) this.pkg[1])));
        // Percentage of packets with different properties
        results.add(Double.toString(((double) this.chgCipher[0]) / ((double) this.sslPkg[0])));
        results.add(Double.toString(((double) this.alert[0]) / ((double) this.sslPkg[0])));
        results.add(Double.toString(((double) this.handshake[0]) / ((double) this.sslPkg[0])));
        results.add(Double.toString(((double) this.appData[0]) / ((double) this.sslPkg[0])));
        results.add(Double.toString(((double) this.heartbeat[0]) / ((double) this.sslPkg[0])));
        results.add(Double.toString(((double) this.chgCipher[1]) / ((double) this.sslPkg[1])));
        results.add(Double.toString(((double) this.alert[1]) / ((double) this.sslPkg[1])));
        results.add(Double.toString(((double) this.handshake[1]) / ((double) this.sslPkg[1])));
        results.add(Double.toString(((double) this.appData[1]) / ((double) this.sslPkg[1])));
        results.add(Double.toString(((double) this.heartbeat[1]) / ((double) this.sslPkg[1])));
        
   
        return results;
    }

    public static String[] getARFFHeaders(){
        List<String> results = new LinkedList<>();
        results.add("@ATTRIBUTE ipup string");
        results.add("@ATTRIBUTE ipdwn string");
        results.add("@ATTRIBUTE portup NUMERIC");
        results.add("@ATTRIBUTE portdwn NUMERIC");
        results.add("@ATTRIBUTE duration NUMERIC");
        results.add("@ATTRIBUTE pkgpersecup NUMERIC");
        results.add("@ATTRIBUTE pkgpersecdwn NUMERIC");
        results.add("@ATTRIBUTE bytespersecup NUMERIC");
        results.add("@ATTRIBUTE bytespersecdwn NUMERIC");
        results.add("@ATTRIBUTE pkgsizeup-max NUMERIC");
        results.add("@ATTRIBUTE pkgsizeup-min NUMERIC");
        results.add("@ATTRIBUTE pkgsizeup-avg NUMERIC");
        results.add("@ATTRIBUTE pkgsizedwn-max NUMERIC");
        results.add("@ATTRIBUTE pkgsizedwn-min NUMERIC");
        results.add("@ATTRIBUTE pkgsizedwn-avg NUMERIC");
        results.add("@ATTRIBUTE tcpwinup-max NUMERIC");
        results.add("@ATTRIBUTE tcpwinup-min NUMERIC");
        results.add("@ATTRIBUTE tcpwinup-avg NUMERIC");
        results.add("@ATTRIBUTE tcpwindwn-max NUMERIC");
        results.add("@ATTRIBUTE tcpwindwn-min NUMERIC");
        results.add("@ATTRIBUTE tcpwindwn-avg NUMERIC");
        results.add("@ATTRIBUTE ttlup-max NUMERIC");
        results.add("@ATTRIBUTE ttlup-min NUMERIC");
        results.add("@ATTRIBUTE ttlup-avg NUMERIC");
        results.add("@ATTRIBUTE ttldwn-max NUMERIC");
        results.add("@ATTRIBUTE ttldwn-min NUMERIC");
        results.add("@ATTRIBUTE ttldwn-avg NUMERIC");
        results.add("@ATTRIBUTE percup-fin NUMERIC");
        results.add("@ATTRIBUTE percup-syn NUMERIC");
        results.add("@ATTRIBUTE percup-rst NUMERIC");
        results.add("@ATTRIBUTE percup-psh NUMERIC");
        results.add("@ATTRIBUTE percup-sck NUMERIC");
        results.add("@ATTRIBUTE percup-urg NUMERIC");
        results.add("@ATTRIBUTE percdwn-fin NUMERIC");
        results.add("@ATTRIBUTE percdwn-syn NUMERIC");
        results.add("@ATTRIBUTE percdwn-rst NUMERIC");
        results.add("@ATTRIBUTE percdwn-psh NUMERIC");
        results.add("@ATTRIBUTE percdwn-sck NUMERIC");
        results.add("@ATTRIBUTE percdwn-urg NUMERIC");
        results.add("@ATTRIBUTE percup-chgCiph NUMERIC");
        results.add("@ATTRIBUTE percup-alert NUMERIC");
        results.add("@ATTRIBUTE percup-hand NUMERIC");
        results.add("@ATTRIBUTE percup-app NUMERIC");
        results.add("@ATTRIBUTE percup-heart NUMERIC");
        results.add("@ATTRIBUTE percdwn-chgCiph NUMERIC");
        results.add("@ATTRIBUTE percdwn-alert NUMERIC");
        results.add("@ATTRIBUTE percdwn-hand NUMERIC");
        results.add("@ATTRIBUTE percdwn-app NUMERIC");
        results.add("@ATTRIBUTE percdwn-heart NUMERIC");
        return (String[]) results.toArray();
    }
    
    
    public String[] getConversation() {
    	List<String> att = new LinkedList<>();
    	
    	// Uplink IP
    	att.add(id.getSrcIP());
    	// Downlink IP
    	att.add(id.getDstIP());
    	// Uplink Port
    	att.add(String.valueOf(id.getSrcPort()));
    	//Downlink Port
    	att.add(String.valueOf(id.getDstPort()));
    	
    	//add properties
    	att.addAll(getFlowStatistics());
    	
    	// Convert and return list as Array
        return att.toArray(new String[0]);
    	
    	
    }

    public long getAvgUPPkgSize(){
        return this.pkgAvgSize[0];
    }

    public long getMaxUPPkgSize(){
        return this.pkgMaxSize[0];
    }
}