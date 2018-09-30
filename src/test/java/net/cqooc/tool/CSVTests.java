package net.cqooc.tool;

import net.cqooc.tool.util.CSVReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CSVTests {

    @Test
    public void test(){


        String filepath = "/Users/vincent/Desktop/t_cms_article.csv";
//
//        String in = CSVReader.read(filepath);

        try {

            CSVParser parser = CSVParser.parse(new File(filepath), Charset.forName("utf8"), CSVFormat.DEFAULT);

            HashMap map = new HashMap();
            for (CSVRecord record : parser.getRecords()) {
                String key = record.get(0);
                String value = record.get(1);
                if(key != null && value != null && !key.equals("") && !value.equals(""))
                map.put(key, value);
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    }
}
