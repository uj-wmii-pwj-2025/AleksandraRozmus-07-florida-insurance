package uj.wmii.pwj.w7.insurance;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.stream.Collectors;

public class FloridaInsurance {
    private final List<InsuranceEntry> entries;

    private static final String ZIP = "FL_insurance.csv.zip";
    private static final String ZIP_ENTRY = "FL_insurance.csv";
    private static final String COUNT = "count.txt";
    private static final String TIV2012 = "tiv2012.txt";
    private static final String MOST_VALUABLE = "most_valuable.txt";

    public FloridaInsurance(List<InsuranceEntry> entries) {
        this.entries = entries;
    }
    
    public static void main(String[] args) {
        FloridaInsurance insurance = new FloridaInsurance(createListFromZip());

        insurance.createCountFile();
        insurance.createTiv2012File();
        insurance.createMostValuableFile();
    }

    public static List<InsuranceEntry> createListFromZip() {
        List<InsuranceEntry> entryList = new ArrayList<>();

        try {
            ZipFile zip = new ZipFile(ZIP);
            ZipEntry entry = zip.getEntry(ZIP_ENTRY);
            BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));

            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                String country = arr[2];
                double tiv2011 = Double.parseDouble(arr[7]);
                double tiv2012 = Double.parseDouble(arr[8]);

                entryList.add(new InsuranceEntry(country, tiv2011, tiv2012));
            }
            reader.close();
            zip.close();
        } catch (IOException e) {
            System.err.println("Error reading zip");
        }

        return entryList;
    }

    private void createCountFile() {
        long count = entries.stream()
                .map(InsuranceEntry::country)
                .distinct()
                .count();

        writeToFile(COUNT, Long.toString(count));
    }

    private void createTiv2012File() {
        double sum = entries.stream()
                .mapToDouble(InsuranceEntry::tiv_2012)
                .sum();

        writeToFile(TIV2012, formatDouble(sum));
    }

    private void createMostValuableFile() {
        List<Map.Entry<String, Double>> top10Increases = entries.stream()
            .collect(Collectors.groupingBy(
                    InsuranceEntry::country,
                    Collectors.summingDouble(InsuranceEntry::increase)))
            .entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
            .limit(10)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("country,value\n");

        for (Map.Entry<String, Double> entry : top10Increases) {
            sb.append(entry.getKey()).append(",");
            sb.append(formatDouble(entry.getValue()));
            sb.append("\n");
        }

        writeToFile(MOST_VALUABLE, sb.toString().trim());
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value).replace(',', '.');
    }

    private void writeToFile(String fileName, String line) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
            writer.write(line);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing file: " + fileName);
        }
    }
}
