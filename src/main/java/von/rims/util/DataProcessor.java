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
//        List<List<String>> records = readDataFromUrl();
        List<List<String>> records = readDataFromFile("input_test.txt");
        Map<String, Map<Integer, List<List<String>>>> uniqueRecords = groupUniqueRecords(records);
        Map<String, Map<Integer, List<List<String>>>> sortedRecords = sortMapInDscOrder(uniqueRecords);
        Map<Integer, List<List<String>>> simplifiedRecords = simplifyGroupedData(sortedRecords);
        writeResultsToFile(simplifiedRecords);
    }

    private static List<List<String>> readDataFromFile(String fileName) throws IOException {
        List<List<String>> validRecords = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (isValidLine(line)) {
                    List<String> recordValues = Arrays.asList(line.split(";"));
                    validRecords.add(recordValues);
                }
            }
        }

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

        for (List<String> record : uniqueRecords) {
            boolean matchFound = false; // Флаг для отслеживания совпадения

            for (int i = 0; i < record.size(); i++) {
                String elementValue = record.get(i);

                if (groupedData.containsKey(elementValue)){
                    if (groupedData.get(elementValue).containsKey(i)){
                        groupedData.get(elementValue).get(i).add(record);
                        matchFound = true; // Совпадение найдено
                        break;
                    }
                }
            }

            // Если совпадение не найдено, создаем новый узел
//            if (!matchFound) {
//                for (int i = 0; i < record.size(); i++) {
//                    String elementValue = record.get(i);
//                    groupedData.putIfAbsent(elementValue, new HashMap<>());
//                    groupedData.get(elementValue).putIfAbsent(i, new ArrayList<>());
//                    groupedData.get(elementValue).get(i).add(record);
//                    break;
//                }
//            }
            // Если совпадение не найдено, создаем новый узел как во внешней, так и во внутренней Map
            if (!matchFound) {
                for (int i = 0; i < record.size(); i++) {
                    String elementValue = record.get(i);
                    groupedData.putIfAbsent(elementValue, new HashMap<>());
                    if (!groupedData.get(elementValue).containsKey(i)) {
                        groupedData.get(elementValue).put(i, new ArrayList<>());
                    }
                    groupedData.get(elementValue).get(i).add(record);
                    break;
                }
            }
        }

        return groupedData;
    }

//    private static Map<String, Map<Integer, List<List<String>>>> groupUniqueRecords(List<List<String>> uniqueRecords) {
//        Map<String, Map<Integer, List<List<String>>>> groupedData = new HashMap<>();
//
//        for (List<String> record : uniqueRecords) {
//            for (int i = 0; i < record.size(); i++) {
//                String elementValue = record.get(i);
//
//                if (groupedData.containsKey(elementValue)){
//                    if (groupedData.get(elementValue).containsKey(i)){
//                        groupedData.get(elementValue).get(i).add(record);
//                        break;
//                    }
//                }
//
//                if (!groupedData.containsKey(elementValue)) {
//                    groupedData.put(elementValue, new HashMap<>());
//                }
//
//                if (!groupedData.get(elementValue).containsKey(i)) {
//                    groupedData.get(elementValue).put(i, new ArrayList<>());
//                }
//
//
//            }
//        }
//
//        return groupedData;
//    }

    private static Map<String, Map<Integer, List<List<String>>>> sortMapInDscOrder(Map<String, Map<Integer, List<List<String>>>> groupedData) {
        Map<String, Map<Integer, List<List<String>>>> sortedData = groupedData.entrySet()
                .stream()
                .sorted(Comparator.comparing((Map.Entry<String, Map<Integer, List<List<String>>>> entry) -> calculateSize(entry.getValue())).reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return sortedData;
    }

    private static Map<Integer, List<List<String>>> simplifyGroupedData(Map<String, Map<Integer, List<List<String>>>> sortedData) {
        Map<Integer, List<List<String>>> simplifiedData = new LinkedHashMap<>();

        int groupNumber = 1;
        Iterator<Map.Entry<String, Map<Integer, List<List<String>>>>> iterator = sortedData.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Map<Integer, List<List<String>>>> entry = iterator.next();
            List<List<String>> mergedRecords = entry.getValue().values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            simplifiedData.put(groupNumber, mergedRecords);
            groupNumber++;
        }

        return simplifiedData;
    }

    private static int calculateSize(Map<Integer, List<List<String>>> innerMap) {
        return innerMap.values()
                .stream()
                .flatMap(Collection::stream)
                .mapToInt(List::size)
                .sum();
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
                String formattedRecord = String.join(";", record);

                writer.write(formattedRecord);
                writer.write("\n");
            }
        }
        writer.close();
    }
}
