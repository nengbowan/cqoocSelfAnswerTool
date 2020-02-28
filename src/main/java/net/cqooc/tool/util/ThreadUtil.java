package net.cqooc.tool.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtil {
    private static Logger logger = LoggerFactory.getLogger(ThreadUtil.class);
    /**
     * 休眠三十秒
     */
    public static void sleep30s() {
        logger.info("准备重试...等候三十秒..");
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
