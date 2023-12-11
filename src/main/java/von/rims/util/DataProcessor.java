package von.rims.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static von.rims.constants.AppConstants.ENCODING;
import static von.rims.constants.AppConstants.FILE_URL;

public class DataProcessor {

    public void processData() throws IOException {
        List<List<String>> records = readDataFromUrl();
        Map<String, Map<Integer, List<List<String>>>> uniqueRecords = groupUniqueRecords(records);
        Map<Integer, List<List<String>>> simplifiedRecords = simplifyGroupedData(uniqueRecords);
        writeResultsToFile(simplifiedRecords);
    }

    private static List<String> readDataFromFile(String fileName) throws IOException {
        List<String> validRecords = new ArrayList<>();

        int validCounter = 0;
        int totalCounter = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                totalCounter++;
                if (isValidLine(line)) {
                    validCounter++;
                    validRecords.add(line);
                }
            }
        }

        System.out.println(totalCounter);
        System.out.println(validCounter);

        return validRecords;
    }

    private static List<List<String>> readDataFromUrl() throws IOException {
        List<List<String>> records = new ArrayList<>();
        URL url = new URL(FILE_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");


        try (InputStream inputStream = connection.getInputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader reader = new InputStreamReader(gzipInputStream, ENCODING);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isValidLine(line)) {
                    List<String> recordValues = Arrays.asList(line.split(";"));
                    records.add(recordValues);
                } else {
                    System.out.println("Некорректная строка пропущена " + line);
                }
            }
        }

        // Возвращаем лист строк без дубликатов
        return records.stream().distinct().toList();
    }

    /**
     * Строка состоит из одного или более значений, разделенных точкой с запятой.
     * Каждое значение заключено в двойные кавычки.
     * Внутри значений могут быть двойные кавычки, но они должны идти парами.
     *
     * @return boolean
     */
    private static boolean isValidLine(String line) {
        String regex = "^(\"[^\"]*(\"{2}[^\"]*)*\";)*\"[^\"]*(\"{2}[^\"]*)*\"$";
        return line.matches(regex);
    }

    private static Map<String, Map<Integer, List<List<String>>>> groupUniqueRecords(List<List<String>> uniqueRecords) {
        Map<String, Map<Integer, List<List<String>>>> groupedData = new HashMap<>();

        int maxLength = uniqueRecords.stream()
                .mapToInt(List::size)
                .max()
                .orElse(0);

        for (int i = 0; i < maxLength; i++) {
            for (List<String> record : uniqueRecords) {
                if (i < record.size()) {
                    String elementValue = record.get(i);

                    if (!groupedData.containsKey(elementValue)) {
                        groupedData.put(elementValue, new HashMap<>());
                    }

                    if (!groupedData.get(elementValue).containsKey(i)) {
                        groupedData.get(elementValue).put(i, new ArrayList<>());
                    }

                    groupedData.get(elementValue).get(i).add(record);
                }
            }
        }

        return groupedData;
    }


    private static Map<Integer, List<List<String>>> simplifyGroupedData(Map<String, Map<Integer, List<List<String>>>> groupedData) {
        Map<Integer, List<List<String>>> simplifiedData = new LinkedHashMap<>();

        List<Map.Entry<String, Map<Integer, List<List<String>>>>> sortedEntries = groupedData.entrySet().stream()
                .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()))
                .collect(Collectors.toList());

        int groupNumber = 1;
        for (Map.Entry<String, Map<Integer, List<List<String>>>> entry : sortedEntries) {
            List<List<String>> mergedRecords = entry.getValue().values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            simplifiedData.put(groupNumber, mergedRecords);
            groupNumber++;
        }

        return simplifiedData;
    }


    private static int countGroupsWithMultipleRecords(Map<Integer, List<List<String>>> groupedData) {
        int groupCount = 0;
        for (Map.Entry<Integer, List<List<String>>> entry : groupedData.entrySet()) {
            if (entry.getValue().size() > 1) {
                groupCount++;
            }
        }
        return groupCount;
    }


    private static void writeResultsToFile(Map<Integer, List<List<String>>> simplifiedGroups) throws IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm");
        String filename = "output_" + dateFormat.format(new Date()) + ".txt";

        FileWriter writer = new FileWriter(filename);

        int groupCount = countGroupsWithMultipleRecords(simplifiedGroups);

        writer.write("Групп с более чем одной записью: " + groupCount);
        writer.write("\n-------------------------");

        for (Map.Entry<Integer, List<List<String>>> entry : simplifiedGroups.entrySet()) {
            int groupId = entry.getKey();
            List<List<String>> groupRecords = entry.getValue();

            writer.write("\nГруппа " + groupId + ":\n");

            for (List<String> record : groupRecords) {
                String formattedRecord = String.join("; ", record);

                writer.write(formattedRecord);
            }
        }
        writer.close();
    }
}
