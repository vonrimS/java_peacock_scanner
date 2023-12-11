package von.rims;


import von.rims.util.DataProcessor;
import java.io.*;


public class App {

    public static void main(String[] args) {
        try {
            // Время начала работы программы
            long startTime = System.currentTimeMillis();

            DataProcessor dataProcessor = new DataProcessor();
            dataProcessor.processData();

            // Время окончания работы программы
            long endTime = System.currentTimeMillis();

            double elapsedTime = (double) (endTime - startTime) /1000;
            String formattedElapsedTime = String.format("%.2f", elapsedTime);

            System.out.println("Работа программы успешно заверна");
            System.out.println("Время выполнения: " + formattedElapsedTime + " сек");
        } catch (IOException e) {
            System.err.println("Ошибка при выполнении задачи: " + e.getMessage());
        }
    }

}
