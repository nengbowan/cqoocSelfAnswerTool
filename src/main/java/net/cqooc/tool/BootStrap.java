package net.cqooc.tool;

public class BootStrap {
    public static void main(String[] args) {
        String username = "127581104170240";
        String password = "123456";

        new APIControllerV2(username , password , false ).run();
    }
}
