import java.io.*;
import java.util.*;

public class flowLogMetrics {
    
    // Map to store Port and Protocol Combination to tag.
    // Key: String Concatenation of Port and Port Name in the format "port,portname"
    // Value: Set of all tags associated with Port and Port Name combination
    private Map<String, Set<String>> lookupTable = new HashMap<>();

    // Map to store mapping of Protocol Number to Protocol Name as per IANA's Assigned Internet Protocol Numbers
    // Key: Protocol Number
    // Value: Protocol Name
    private Map<String, String> protocolMap = new HashMap<>();

    // Map to store count for each tag value
    // Key: Tag Name
    // Value: Number of tag occurances computed from the log file
    private Map<String, Integer> tagCounts = new HashMap<>();

    // Map to store count for each port and portNumber combination.
    // Key: String Concatenation of Port and Port Name in the format "port,portname"
    // Value: Number of occurances of each Port and Port Name present in input log file.
    private Map<String, Integer> portProtocolCounts = new HashMap<>();

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println("Usage: java flowLogMetrics <flowLogFilePath> <lookupTableFilePath>");
            return;
        }
        String flowLogFilePath = args[0];
        String lookupTableFilePath = args[1];
        String protocolListFilePath = "files/protocol_list.csv";
        
        flowLogMetrics tagger = new flowLogMetrics();

        tagger.setupErrorLogging("output/error_log.txt");
        System.out.println("Intializaing Protocol Name Mapping...");
        tagger.initializeProtocolMap(protocolListFilePath);

        System.out.println("Reading Lookup table From CSV...");
        tagger.loadLookupTable(lookupTableFilePath);

        System.out.println("Processing Logs...");
        tagger.processFlowLogs(flowLogFilePath);

        System.out.println("Generating Output File...");
        tagger.generateOutput();

        System.out.println("Finished");
    }

    // Method to read IANA protocol number and Protocol Name from the csv file and store it in protocolMap data structure
    public void initializeProtocolMap(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");
                if (parts.length < 2) {
                    System.err.println("Invalid Protocol Record: " + line);
                    continue;
                }

                String protocolNumber = parts[0].trim();
                String protocolName = parts[1].trim().toLowerCase();
                if(protocolName.isEmpty()){
                    protocolName = "unknown";
                }
                protocolMap.put(protocolNumber, protocolName);
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    // Method to read Look up table from file and store it in lookUpTable data structure
    public void loadLookupTable(String lookupTableFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(lookupTableFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length != 3 || parts[2].trim().isEmpty()) {
                    System.err.println("Invalid Lookup Table Record: (Bad Format) " + line);
                    continue;
                }

                String dstPort = parts[0].trim();
                String protocol = parts[1].trim().toLowerCase();
                String tag = parts[2].trim();

                int convertedProtocolNum = strToInt(dstPort);
                if (convertedProtocolNum < 0 || convertedProtocolNum > 65535) {
                    System.err.println("Invalid Lookup Table Record: (Invalid Protocol Number) " + line);
                    continue;
                }

                if(!protocolMap.containsValue(protocol)){
                    System.err.println("Invalid Lookup Table Record: (Invalid Protocol Name) " + line);
                }

                String portProtocolKey = dstPort + "," + protocol;
                lookupTable.putIfAbsent(portProtocolKey, new HashSet<>());
                lookupTable.get(portProtocolKey).add(tag);
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    // Method to read logs from file and calculate occurances of each tag and port,protocol
    public void processFlowLogs(String flowLogFilePath) {
        Set<String> logStatusValues = new HashSet<>();
        logStatusValues.add("ok");
        logStatusValues.add("skipdata");
        logStatusValues.add("nodata");

        try (BufferedReader reader = new BufferedReader(new FileReader(flowLogFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split(" ");
                if (parts.length != 14 || !(parts[0].equals("2") && logStatusValues.contains(parts[13].toLowerCase()))) {
                    System.err.println("Invalid Flow Log Record: (Bad Format) " + line);
                    continue;
                }

                String dstPort = parts[6].trim();
                String protocolNum = parts[7].trim();
                
                int convertedDstPort = strToInt(dstPort);
                if(convertedDstPort < 0 || convertedDstPort > 65535){
                    System.err.println("Invalid Flow Log Record: (Port Number Out of Range) " + line);
                    continue;
                }
                
                int convertedProtocolNum = strToInt(protocolNum);
                if (convertedProtocolNum > 146 && convertedProtocolNum <= 255) {
                    convertedProtocolNum = 146;
                }

                if (convertedProtocolNum < 0 || convertedProtocolNum > 146) {
                    System.err.println("Invalid Flow Log Record: (Invalid Protocol Number) " + line);
                    continue;
                }

                String protocol = protocolMap.getOrDefault(protocolNum, "unknown");
                String portProtocolKey = dstPort + "," + protocol;
                incrementCount(portProtocolCounts, portProtocolKey);

                if (lookupTable.containsKey(portProtocolKey)) {
                    Set<String> tags = lookupTable.get(portProtocolKey);
                    for (String tag : tags) {
                        incrementCount(tagCounts, tag);
                    }
                } else {
                    incrementCount(tagCounts, "Untagged");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    public void incrementCount(Map<String, Integer> map, String key) {
        map.put(key, map.getOrDefault(key, 0) + 1);
    }

    public void generateOutput() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("output/tags.csv"))) {

            writer.println("Tag,Count");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                if (entry.getValue() != 0) {
                    writer.println(entry.getKey() + "," + entry.getValue());
                }
            }

        }catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter("output/port-protocol.csv"))) {

            writer.println("Port,Protocol,Count");
            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                writer.println(entry.getKey() + "," + entry.getValue());
            }

        }catch (IOException e) {
            System.out.println("Error reading the file: " + e.getMessage());
        }
    }

    public int strToInt(String str) {
        int number;
        try {
            number = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            number = -1;
        }
        return number;
    }

    public void setupErrorLogging(String logFilePath) {
        try {
            PrintStream errorLog = new PrintStream(new FileOutputStream(logFilePath, true));
            System.setErr(errorLog);
        } catch (FileNotFoundException e) {
            System.out.println("Could not set up error logging to file: " + e.getMessage());
        }
    }
}