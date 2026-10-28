import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class copyfileJJJ {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Определение путей (используем только разрешенные nio.file.Path и Paths)
        Path src1 = Paths.get("source1.txt");
        Path src2 = Paths.get("source2.txt");

        Path dest1Seq = Paths.get("dest1_seq.txt");
        Path dest2Seq = Paths.get("dest2_seq.txt");

        Path dest1Par = Paths.get("dest1_par.txt");
        Path dest2Par = Paths.get("dest2_par.txt");

        // 1. Подготовка: создаем файлы и записываем в них большой объем текста для замера времени
        System.out.println("Создание тестовых файлов...");
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 50000; i++) {
            largeText.append("Пример текста для копирования. ");
        }
        String content = largeText.toString();

        Files.writeString(src1, content);
        Files.writeString(src2, content);
        System.out.println("Файлы готовы. Начинаем тесты.\n");

        // 2. ПОСЛЕДОВАТЕЛЬНОЕ КОПИРОВАНИЕ
        long startSeq = System.currentTimeMillis();

        copyManual(src1.toFile(), dest1Seq.toFile());
        copyManual(src2.toFile(), dest2Seq.toFile());

        long endSeq = System.currentTimeMillis();
        long durationSeq = endSeq - startSeq;
        System.out.println("=== ПОСЛЕДОВАТЕЛЬНОЕ КОПИРОВАНИЕ ===");
        System.out.println("Время выполнения: " + durationSeq + " мс\n");

        // 3. ПАРАЛЛЕЛЬНОЕ КОПИРОВАНИЕ
        long startPar = System.currentTimeMillis();

        // Создаем два стандартных потока выполнения
        Thread thread1 = new Thread(() -> copyManual(src1.toFile(), dest1Par.toFile()));
        Thread thread2 = new Thread(() -> copyManual(src2.toFile(), dest2Par.toFile()));

        // Запускаем их одновременно
        thread1.start();
        thread2.start();

        // ожидание завершения обоих потоков в главном потоке
        thread1.join();
        thread2.join();

        long endPar = System.currentTimeMillis();
        long durationPar = endPar - startPar;
        System.out.println("=== ПАРАЛЛЕЛЬНОЕ КОПИРОВАНИЕ ===");
        System.out.println("Время выполнения: " + durationPar + " мс\n");

        // Очистка созданных файлов после теста
        Files.deleteIfExists(src1); Files.deleteIfExists(src2);
        Files.deleteIfExists(dest1Seq); Files.deleteIfExists(dest2Seq);
        Files.deleteIfExists(dest1Par); Files.deleteIfExists(dest2Par);
    }

    // Метод копирования
    private static void copyManual(File source, File dest) {
        // Объявляем OutputStream
        try (FileInputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {

            byte[] buffer = new byte[8192]; // Буфер на 8 КБ
            int bytesRead;

            // Побайтовое чтение и запись в поток
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            System.err.println("Ошибка копирования: " + e.getMessage());
        }
    }
}