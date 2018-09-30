package net.cqooc.tool;

import net.cqooc.tool.util.ScriptEngineUtil;
import org.junit.Test;

public class Sha256Tests {

    @Test
    public void getSHA256Str(){
//        String

        String hex = "123456ABc";

        System.out.println(ScriptEngineUtil.encode(hex));

    }
}
