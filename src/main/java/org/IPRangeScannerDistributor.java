package org;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IPRangeScannerDistributor {

    public static void scanIPRange(String ipWithMask, int numThreads) {
        try {
            List<String> ips = getIps(ipWithMask);
            if (ips == null) {
                System.out.println("Incorrect ip");
                return;
            }
            int subRangeSize = ips.size() / numThreads;

            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

            for (int i = 0; i < numThreads; i++) {
                int rightBound = (i + 1) * subRangeSize;
                if (rightBound > ips.size())
                    rightBound = ips.size();

                List<String> threadIps = ips.subList(i * subRangeSize, rightBound);
                Runnable task = new IPRangeScannerTask(threadIps);
                executorService.execute(task);

                if ((i + 1) * subRangeSize > ips.size() - 1) break;
            }
            executorService.shutdown();
        } catch (Exception e) {
            System.err.println("Ошибка при разделении диапазона IP-адресов: " + e.getMessage());
        }
    }

    private static List<String> getIps(String ipWithMask) {
        try {
            String[] parts = ipWithMask.split("/");
            String baseIP = parts[0];
            int subnetMask = Integer.parseInt(parts[1]);

            InetAddress inetAddress = InetAddress.getByName(baseIP);
            byte[] ipBytes = inetAddress.getAddress();

            int[] ip = new int[4];
            for (int i = 0; i < 4; i++) {
                ip[i] = ipBytes[i] & 0xFF;
            }

            int numberOfAddresses = (int) Math.pow(2, 32 - subnetMask);

            List<String> ips = new ArrayList<String>();

            for (int i = 0; i < numberOfAddresses; i++) {
                ips.add(ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3]);
                ip[3]++;
                for (int j = 3; j >= 0; j--) {
                    if (ip[j] > 255) {
                        ip[j] = 0;
                        ip[j - 1]++;
                    }
                }
            }
            return ips;
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
