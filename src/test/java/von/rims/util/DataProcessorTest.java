package von.rims.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest {
    private static List<List<String>> testUniqueRecords;

    private static Method readDataFromUrl;
    private static Method isValidLine;
    private static Method groupUniqueRecords;
    private static Method findLongestRecord;

    @BeforeAll
    public static void setUp() throws NoSuchMethodException {
        testUniqueRecords = new ArrayList<>();
        testUniqueRecords.add(Arrays.asList("Value1", "Value2", "Value3"));
        testUniqueRecords.add(Arrays.asList("Value1", "Value2", "Value4"));
        testUniqueRecords.add(Arrays.asList("Value2", "Value3", "Value5"));

        readDataFromUrl = DataProcessor.class.getDeclaredMethod("readDataFromUrl");
        readDataFromUrl.setAccessible(true);

        isValidLine = DataProcessor.class.getDeclaredMethod("isValidLine", String.class);
        isValidLine.setAccessible(true);

        findLongestRecord = DataProcessor.class.getDeclaredMethod("findLongestRecord", List.class);
        findLongestRecord.setAccessible(true);

        groupUniqueRecords = DataProcessor.class.getDeclaredMethod("groupUniqueRecords", List.class);
        groupUniqueRecords.setAccessible(true);
    }

    @Test
    public void testReadDataFromUrl() throws IOException, InvocationTargetException, IllegalAccessException {
        List<List<String>> records = (List<List<String>>) readDataFromUrl.invoke(null);
        assertTrue(records.size() > 0);
    }

    @Test
    void testIsValidLine() throws InvocationTargetException, IllegalAccessException {
        boolean result = (boolean) isValidLine.invoke(null, "\"Value1\";\"Value2\";\"Value3\"");
        assertTrue(result);

        // Строки вида "8383"200000741652251" и "79855053897"83100000580443402";"200000133000191"
        // являются некорректными и должны пропускаться
        result = (boolean) isValidLine.invoke(null, "\"Valu\"e1\";\"Value2\";\"Value3\"");
        assertFalse(result);
    }

    @Test
    void testFindLongestRecord() throws InvocationTargetException, IllegalAccessException {
        // Тестовый случай 1: Самая длинная запись в середине
        List<List<String>> data1 = Arrays.asList(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("X", "Y", "Z", "W"),
                Arrays.asList("1", "2", "3")
        );
        List<String> longest1 =(List<String>) findLongestRecord.invoke(null, data1);
        assertEquals(Arrays.asList("X", "Y", "Z", "W"), longest1);

        // Тестовый случай 2: Самая длинная запись в начале
        List<List<String>> data2 = Arrays.asList(
                Arrays.asList("X", "Y", "Z", "W"),
                Arrays.asList("A", "B", "C"),
                Arrays.asList("1", "2", "3")
        );
        List<String> longest2 =(List<String>) findLongestRecord.invoke(null, data2);
        assertEquals(Arrays.asList("X", "Y", "Z", "W"), longest2);

        // Тестовый случай 3: Самая длинная запись в конце
        List<List<String>> data3 = Arrays.asList(
                Arrays.asList("1", "2", "3"),
                Arrays.asList("A", "B", "C"),
                Arrays.asList("X", "Y", "Z", "W")
        );
        List<String> longest3 =(List<String>) findLongestRecord.invoke(null, data3);
        assertEquals(Arrays.asList("X", "Y", "Z", "W"), longest3);

        // Тестовый случай 4: Единственная запись
        List<List<String>> data4 = Arrays.asList(
                Arrays.asList("A", "B", "C")
        );
        List<String> longest4 =(List<String>) findLongestRecord.invoke(null, data4);
        assertEquals(Arrays.asList("A", "B", "C"), longest4);

        // Тестовый случай 5: Все записи одной длины
        List<List<String>> data5 = Arrays.asList(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("A", "Y", "Z"),
                Arrays.asList("A", "B", "W")
        );
        List<String> longest5 =(List<String>) findLongestRecord.invoke(null, data5);
        assertEquals(Arrays.asList("A", "B", "C"), longest5);
    }


    @Test
    void testGroupUniqueRecords() throws InvocationTargetException, IllegalAccessException {
        // Тестовые данные
        List<List<String>> testData = new ArrayList<>();
        testData.add(Arrays.asList("111", "123", "222"));
        testData.add(Arrays.asList("200", "123", "100"));
        testData.add(Arrays.asList("300", "", "100"));

        // Ожида
        Map<Integer, Set<List<String>>> expected = new HashMap<>();
        Set<List<String>> group0 = new HashSet<>();
        group0.add(Arrays.asList("111", "123", "222"));
        group0.add(Arrays.asList("300", "", "100"));
        group0.add(Arrays.asList("200", "123", "100"));
        expected.put(0, group0);

        Map<Integer, Set<List<String>>> result =
                (Map<Integer, Set<List<String>>>) groupUniqueRecords.invoke(null, testData);

        assertEquals(expected, result);
    }
}