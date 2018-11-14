package net.cqooc.tool.util;

import net.cqooc.tool.APIController;
import net.cqooc.tool.APIControllerV2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

public class CSVReader {

    public static void read(File f){
        try {

            CSVParser parser = CSVParser.parse(f, Charset.forName("utf8"), CSVFormat.DEFAULT);

            HashMap map = new HashMap();
            for (CSVRecord record : parser.getRecords()) {
                String key = record.get(0);
                String value = record.get(1);
                if(key != null && value != null && !key.equals("") && !value.equals(""))
                    map.put(key, value);

            }
            APIControllerV2.answersCache = map;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
