package org;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class FileUtil {
    public static void saveDomainNamesToFile(Set<String> domainNames) {
        try (FileWriter fileWriter = new FileWriter("domain_names.txt")) {
            if (domainNames != null) {
                for (String domainName : domainNames) {
                    fileWriter.write(domainName + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении доменных имен в файл: " + e.getMessage());
        }
    }

}
