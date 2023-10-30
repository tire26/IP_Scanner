package org;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FileUtil {
    public static synchronized void saveDomainNamesToFile(Map<String, String> domainNames) {
        try {
            File file = new File("domain_names.txt");

            if (!file.exists()) {
                if (!file.createNewFile()) {
                    System.err.println("Не удалось создать файл для сохранения доменных имен.");
                    return;
                }
            }

            try (FileWriter fileWriter = new FileWriter(file, true)) {
                if (domainNames != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String currentTime = dateFormat.format(new Date());

                    for (String domainName : domainNames.keySet()) {
                        String ip = domainNames.get(domainName);
                        fileWriter.write(currentTime + " : " + domainName +" : " + ip +"\n");
                    }
                }
            } catch (IOException e) {
                System.err.println("Ошибка при записи доменных имен в файл: " + e.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при проверке наличия файла: " + e.getMessage());
        }
    }
}
