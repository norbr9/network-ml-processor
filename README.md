# MMT Reports Processor for ML

MMT works with a proprietary format for reporting the extracted information. This Java application (based on Java 8) reads the reports and adapts the information in a more standard format: CSV or ARFF. The latter is a "standard" format used in [MOA](https://moa.cms.waikato.ac.nz/) software from Weka University.

## Compilation

This software uses [Apache Maven](https://maven.apache.org/) for compilation. Please refer to the Maven documentation to install this softwar in your machine.

To compile this processor, simply use the `mvn install` command in your favorite console. This will compile the classes and pack them in a single jar file. The compiled code can be found under the "target" folder.

In the "target" folder there will be 2 files: `pkg-processor-vX.X.jar` and `pkg-processor-vX.X-jar-with-dependencies.jar`. The former is the "original" version, that contains ONLY the compiled classes of this repository, while the latter is a complete jar that contains all other eternal libraries used.

## CSV Format
The csv input file contains columns as follows:

1. MMT Report type.
2. MMT Probe ID.
3. Network interface of the capture.
4. Timestamp. A timestamp in the format 'seconds.microsconds'
5. Report number. This is the nunmber of the customreport.
6. Network event. This is the network event that was detected.
7. Source IP.
8. Destination IP
9. TCP source port.
10. TCP destination port.
11. IP Total length. This is the value of the field "total length" from the IP headers.
12. TCP Window size. This is the size of the TCP window reported in the TCP headers.
13. IP TTL. This is the TTL (time to leave) reported in the IP headers.
14. TCP FIN flag.
15. TCP SYN flag.
16. TCP RST flag.
17. TCP PSH flag.
18. TCP ACK flag.
19. TCP URG flag.
20. TLS content type. This is the type of information reported in the TLS headers.


The output csv:
1. Uplink IP
2. Downlink IP
3. Uplink port
4. Downlink port
5. Duration of the conversation in microseconds
6. Number of packets sent in 1 second in uplink
7. Number of packets sent in 1 second in downlink
8. Number of bytes sent in 1 second in uplink
9. Number of bytes sent in 1 second in downlink
10-12. Maximal, Minimal and average packet size in uplink
13-15. Maximal, Minimal and average packet size in downlink
16-18. Maximal, Minimal and average size of TCP window in uplink
19-21. Maximal, Minimal and average size of TCP window in downlink
22-24. Maximal, Minimal and average time to live (TTL) in uplink
25-27. Maximal, Minimal and average time to live (TTL) in downlink
28-33. Percentage of packets with different TCP flags: FIN, SYN, RST, PSH, ACK and URG in uplink
34-39. Percentage of packets with different TCP flags: FIN, SYN, RST, PSH, ACK and URG in downlink
40-44. Percentage of packets with different properties: chgcipher, alert, handshake, appdata and heartbeat in uplink
45-49. Percentage of packets with different properties: chgcipher, alert, handshake, appdata and heartbeat in downlink




## Execution

To run the java application, simply go to the target folder and run the jar with dependencies using the following command:

``java -jar pkg-processorvX.X-jar.with-dependencies.jar [-s <sep_char>] [-i <input_file>] [-o <output_file>] [-a <input_file-ARFF>] [-h]``

For more informaiton about the options please run `java -jar pkg-processorvX.X-jar.with-dependencies.jar -h`.
