package eu.montimage.processor;

public class FlowID{
    private String srcIP;
    private String dstIP;
    private int srcPort;
    private int dstPort;

 

	public FlowID(String sourceIP, String destIP, int sourcePort, int destPort){
        this.srcIP = sourceIP;
        this.dstIP = destIP;
        this.srcPort = sourcePort;
        this.dstPort = destPort;
    }

    /**
     * Helper method to determine the sense of a given flow with respect
     * to the current one. This method assumes both flows are the same. Use the
     * <code>equals</code> method to check this before
     * @return True if both flows are on the same sense. False otherwise
     */
    public boolean flowSenseOf(FlowID cmp){
        if ((this.srcIP.equals(cmp.srcIP) && this.dstIP.equals(cmp.dstIP)) &&
            ((this.srcPort == cmp.srcPort && this.dstPort == cmp.dstPort))){
            return true;
        }
        return false;
    }



	/**
     * Determines if the current flow is the same as a given one.
     * Two FlowIDs are the same if destination socket is equal to source socket,
     * that means source and destination ip/port may be swapped and still the same FlowID
     * @param o The object to compare with. If the given Object is not of class FlowID,
     * this function will return false.
     * @return True if the flows are the same (without considering the sense).
     * False otherwise.
     */
    @Override
    public boolean equals(Object o){
        try{
            FlowID cmp = (FlowID) o;
            
            if ((this.srcIP.equals(cmp.dstIP) && this.dstIP.equals(cmp.srcIP)) &&
                    ((this.srcPort == cmp.dstPort && this.dstPort == cmp.srcPort)))
                    return true;
                
            
            if ((this.srcIP.equals(cmp.srcIP) && this.dstIP.equals(cmp.dstIP)) &&
                    ((this.srcPort == cmp.srcPort && this.dstPort == cmp.dstPort)))
                    return true;
                
            
          
        
        } catch (ClassCastException e){/*Given object is not comparable*/}
        
        
        return false;
    }

    
    @Override
	public int hashCode() {
		return dstIP.hashCode() * srcIP.hashCode() * dstPort * srcPort;
	}
    
    
    public String getSrcIP() {
 		return srcIP;
 	}

 	public String getDstIP() {
 		return dstIP;
 	}

 	public int getSrcPort() {
 		return srcPort;
 	}

 	public int getDstPort() {
 		return dstPort;
 	}
    
    
    
}



