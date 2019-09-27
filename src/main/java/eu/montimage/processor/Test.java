package eu.montimage.processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Test{

    static String SEPARATOR = ",";
    static String INPUT_FILENAME = "dataoutput.csv";
    static String OUTPUT_FILENAME = "processed.csv";
    static boolean isARFF = false;
    static String RELATION_NAME = "processed";
    static Map<FlowID, Flow> flows = new HashMap<>();

    static public void main(String[] args){
        processOptions(args);
        // Read all the entries of the file
        List<String[]> entries = readCSVfile(Test.INPUT_FILENAME);
        
        // Process the entries
        List<String[]> processedEntries = processEntries(entries);
        
        // Create the output file
        PrintWriter outFile = null;
        try{
            outFile = new PrintWriter(new FileWriter(Test.OUTPUT_FILENAME));
        } catch (IOException e){
            System.err.println("Exception creating the output file");
            e.printStackTrace();
            System.exit(2);
        }
        // Add the ARFF headers if it was specified by the user
        if (Test.isARFF) addARFFHeaders(outFile);
        // Write the processed entries in the output file
        writeARFFEntries(outFile, processedEntries);
        // Close the writer
        outFile.close();
    }

    static public List<String[]> readCSVfile(String filename){
        List<String[]> retval = new LinkedList<String[]>();
        try(BufferedReader br = new BufferedReader(new FileReader(filename))){
            // Get a stream containing all the lines
            Stream<String> stream = br.lines().sequential();
            stream.forEach(line -> {
                // Split the line following the separator
                String[] cols = line.split(Test.SEPARATOR);
                if (cols.length < 10) return; // this is not a well-formated line.
                retval.add(cols);
            });
        } catch (IOException e){
            System.err.println("Exception while reading the CSV file");
            e.printStackTrace();
            System.exit(2);
        }
        return retval;
    }

    static public List<String[]> processEntries(List<String[]> rawEntriesList){
        rawEntriesList.forEach(entry -> {
            // Create the an ID for this packet.
            FlowID id = new FlowID(entry[6], entry[7], Integer.parseInt(entry[8]), Integer.parseInt(entry[9]));
            // Check if this packet belongs to an already-registered flow  
            if(flows.keySet().contains(id)){
            	// There is a flow with this ID
            	System.out.println("Found packet that belongs to an already-registered flow");
                Flow flow = flows.get(id);
                flow.logPkg(id, entry);
                
            } else {
                // A new flow should be created
            	System.out.println("Found packet that does not belong to any flow");
                long tstamp = Long.parseLong(entry[3].replace(".", "")); // get rid of the "." and parse the stamp in microsec.
                Flow newFlow = new Flow(id,tstamp);
                newFlow.logPkg(id, entry);
                flows.put(id, newFlow);
            }
        });
        
        // Create a List of flows' conversations
        List<String[]> flowsAsList = new ArrayList<>(flows.values().size());
        System.out.println("Number of flows: " + flows.values().size());
        flows.values().forEach(entry -> {
        	flowsAsList.add(entry.getConversation());
        });
        
        return flowsAsList;
    }

    static public void addARFFHeaders(PrintWriter pr){
        pr.println("@RELATION " + Test.RELATION_NAME);
        String[] headers = Flow.getARFFHeaders();
        Stream.of(headers).forEach(header -> pr.println(header));
    }

    static public void writeARFFEntries(PrintWriter pr, List<String[]> entries){
        entries.stream().forEach(entry -> pr.println(String.join(",", entry)));
    }

    static public void processOptions(String[] args){
        Options options = new Options();

        options.addOption("s", "separator", true, "Specifies the separator of the input file. Default is comma: \',\'");
        options.addOption("i", "filename", true, "Specifies the input filename. Default is: \'dataoutput.csv\'");
        options.addOption("o", "filename", true, "Specifies the output filename. Default is: \'processed.csv\'");
        options.addOption("a", "arff", false, "Uses ARFF file instead of CSV in the output file.");
        options.addOption("h", "help", false, "Prints this help :)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try{
            cmd = parser.parse(options, args);
            if (cmd.hasOption("h")){
                formatter.printHelp("java -jar Test.jar [-s <separator>] [-i <filename>] [-o <filename>]  [-a <filename>]  [-h]", options);
                System.exit(0);
            }
            if (cmd.hasOption("s")){
                Test.SEPARATOR = cmd.getOptionValue("s", ",");
            }
            if (cmd.hasOption("i")){
                Test.INPUT_FILENAME = cmd.getOptionValue("i", "dataoutput.csv");
            }
            if (cmd.hasOption("o")){
                Test.OUTPUT_FILENAME = cmd.getOptionValue("o", "processed.csv");
            }
            if (cmd.hasOption("a")){
                Test.SEPARATOR = cmd.getOptionValue("o", "processed.csv");
            }
        } catch (ParseException e){
            System.err.println("Error parsing the options");
            formatter.printHelp("java -jar Test.jar [-s <separator>] [-i <filename>] [-o <filename>]  [-a <filename>] [-h]", options);
            System.exit(1);
        }
    }
}