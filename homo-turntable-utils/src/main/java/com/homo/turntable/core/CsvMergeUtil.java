package com.homo.turntable.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvMergeUtil {

    public static List<CSVRecord> mergeTable(String path){
        try {
            File file = new File(path);
            File[] files = file.listFiles();
            List<CSVRecord> all = new ArrayList<>();
            for (File f: files){
                Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "GBK" ));
                CSVParser parser = CSVParser.parse(reader, CSVFormat.EXCEL);
                List<CSVRecord> records = parser.getRecords();
                mergeRecord(all, records);
            }
            return all;
        }catch (Exception e){
            log.error("mergeTable error, path: {}", path, e);
            return null;
        }
    }

    private static void mergeRecord(List<CSVRecord> all, List<CSVRecord> records){
        for(int i = all.size() >= 4 ? 4 : 0; i < records.size(); i++){
            all.add(records.get(i));
        }
    }

}
