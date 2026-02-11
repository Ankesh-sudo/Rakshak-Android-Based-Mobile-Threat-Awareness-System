package com.rakshak.security.calls.data;

import java.util.HashSet;
import java.util.Set;

public class ReputationDatabase {

    private static final Set<String> blacklist = new HashSet<>();

    static {
        blacklist.add("+234123456789");
        blacklist.add("+919999999999");
    }

    public static boolean isBlacklisted(String number) {
        return blacklist.contains(number);
    }

    public static void addToBlacklist(String number) {
        blacklist.add(number);
    }
}
