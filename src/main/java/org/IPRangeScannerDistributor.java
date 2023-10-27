package org;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPRangeScannerDistributor {

    public static void scanIPRange(String startIP, String range, int numThreads) {
        try {
            long start = ipToLong(startIP);
            long rangeSize = Long.parseLong(range);
            long end = start + rangeSize;
            long subRangeSize = rangeSize / numThreads;

            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

            for (int i = 0; i < numThreads; i++) {
                long subRangeStart = start + i * subRangeSize;
                long subRangeEnd = (i == numThreads - 1) ? end : subRangeStart + subRangeSize - 1;

                Runnable task = new IPRangeScannerTask(longToIP(subRangeStart), longToIP(subRangeEnd));
                executorService.execute(task);
            }

            executorService.shutdown();
        } catch (Exception e) {
            System.err.println("Ошибка при разделении диапазона IP-адресов: " + e.getMessage());
        }
    }

    private static long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result += Long.parseLong(octets[i]) << (24 - (8 * i));
        }
        return result;
    }

    private static String longToIP(long ipAddress) {
        StringBuilder sb = new StringBuilder(15);
        for (int i = 0; i < 4; i++) {
            sb.insert(0, ipAddress & 0xff);
            if (i < 3) {
                sb.insert(0, '.');
            }
            ipAddress >>= 8;
        }
        return sb.toString();
    }

    private static class IPRangeScannerTask implements Runnable {
        private final String startIP;
        private final String endIP;

        public IPRangeScannerTask(String startIP, String endIP) {
            this.startIP = startIP;
            this.endIP = endIP;
        }

        @Override
        public void run() {
            Set<String> domainNames = new HashSet<>();
            for (long ip = ipToLong(startIP); ip <= ipToLong(endIP); ip++) {
                String ipAddress = longToIP(ip);
                Set<String> scannedDomains = SSLScanner.scanSSLDomains(ipAddress);
                if (scannedDomains != null) {
                    domainNames.addAll(scannedDomains);
                }
            }
            FileUtil.saveDomainNamesToFile(domainNames);
        }

    }
}
