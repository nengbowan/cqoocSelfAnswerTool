package net.cqooc.tool.cmd;

import net.cqooc.tool.API;
import net.cqooc.tool.domain.ImportUser;
import net.cqooc.tool.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CmdMain {
    private static Logger logger = LoggerFactory.getLogger(CmdMain.class);
    public static void main(String[] args) throws IOException {

        List<ImportUser> users = new ArrayList<ImportUser>();
        BufferedReader br = new BufferedReader(new FileReader(new File("test/学生账号.txt")));
        String line = null;
        while((line = br.readLine()) != null){
            if(line.startsWith("#")){
                continue;
            }
            users.add(ImportUser.builder()
                    .username(line.split(",")[0])
                    .password(line.split(",")[1])
                    .build());
        }
        if(CollectionUtils.isEmpty(users)){
            logger.error("未设置学生账号,程序退出,,,");
            System.exit(0);
        }
        for(ImportUser user : users){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new API(user.getUsername(), user.getPassword()).run();
                }
            }).start();
        }

    }



}
