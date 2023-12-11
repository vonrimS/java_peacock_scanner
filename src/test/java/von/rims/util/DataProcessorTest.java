package von.rims.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataProcessorTest {
    private static List<List<String>> testUniqueRecords;

    private static Method readDataFromUrl;
    private static Method isValidLine;
    private static Method groupUniqueRecords;

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
    void testGroupUniqueRecords() throws InvocationTargetException, IllegalAccessException {
        Map<String, Map<Integer, List<List<String>>>> result = (Map<String, Map<Integer, List<List<String>>>>) groupUniqueRecords.invoke(null, testUniqueRecords);

        // Проверка на наличие и корректность группы "Value1"
        assertTrue(result.containsKey("Value1"));
        Map<Integer, List<List<String>>> value1Group = result.get("Value1");
        assertNotNull(value1Group);
        assertEquals(2, value1Group.size()); // Должно быть два индекса: 0 и 1

        // Проверка на наличие и корректность группы "Value2"
        assertTrue(result.containsKey("Value2"));
        Map<Integer, List<List<String>>> value2Group = result.get("Value2");
        assertNotNull(value2Group);
        assertEquals(2, value2Group.size()); // Должно быть два индекса: 0 и 1

        // Проверка содержимого групп
        assertEquals(2, value1Group.get(0).size()); // Две записи в первой группе "Value1"
        assertEquals(1, value1Group.get(1).size()); // Одна запись во второй группе "Value1"

        assertEquals(1, value2Group.get(0).size()); // Одна запись в первой группе "Value2"
        assertEquals(1, value2Group.get(1).size()); // Одна запись во второй группе "Value2"

//        // Добавьте здесь проверки на корректность результатов
//        assertNotNull(result);
//        assertTrue(result.size() > 0);
//
//        assertEquals(2, result.size());

//        // Примеры проверок
//        assertTrue(result.containsKey("Value1"));
//        assertTrue(result.containsKey("Value2"));
//        assertTrue(result.containsKey("Value3"));
//
//        // Проверка на размер внутренних структур данных
//        assertEquals(2, result.get("Value1").size());
//        assertEquals(2, result.get("Value2").size());
//        assertEquals(1, result.get("Value3").size());
    }
}