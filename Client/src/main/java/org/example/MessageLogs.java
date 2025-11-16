package org.example;

public class MessageLogs {

    public static String MSG_TOO_LONG = "\nYour message wasn't sent, it is too long ... Consider making it less than 128 characters.";
    public static String CLIENT_TIMEOUT = "\nYou have been idle for too long. Get kicked. Idget.";

    public static void printLog(String log) {
        System.out.println(ANSICodes.RED + log + ANSICodes.RESET);
    }

    public static void printInfo(String log) {
        System.out.println(ANSICodes.YELLOW + log + ANSICodes.RESET);
    }

    public class ANSICodes {
        // Regular text colors
        public static final String RESET = "\u001B[0m";
        public static final String BLACK = "\u001B[30m";
        public static final String RED = "\u001B[31m";
        public static final String GREEN = "\u001B[32m";
        public static final String YELLOW = "\u001B[33m";
        public static final String BLUE = "\u001B[34m";
        public static final String MAGENTA = "\u001B[35m";
        public static final String CYAN = "\u001B[36m";
        public static final String WHITE = "\u001B[37m";

        // Bright text colors
        public static final String BRIGHT_BLACK = "\u001B[30;1m";
        public static final String BRIGHT_RED = "\u001B[31;1m";
        public static final String BRIGHT_GREEN = "\u001B[32;1m";
        public static final String BRIGHT_YELLOW = "\u001B[33;1m";
        public static final String BRIGHT_BLUE = "\u001B[34;1m";
        public static final String BRIGHT_MAGENTA = "\u001B[35;1m";
        public static final String BRIGHT_CYAN = "\u001B[36;1m";
        public static final String BRIGHT_WHITE = "\u001B[37;1m";
    }
}
