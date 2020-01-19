package com.ztesoft.config.compare;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Test {

    public static void test() {
        List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");

        strings.stream().filter(string -> !string.isEmpty()).collect(Collectors.toList()).forEach(System.out::println);
    }

    public static String encrypt() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        List<Character> list = new ArrayList<>();
        for (char a = 65; a < 65 + 26; a++) {
            list.add(a);
        }
        for (char a = 97; a < 97 + 26; a++) {
            list.add(a);
        }
        System.out.println("begin");
        for (char a : list) {
            System.out.println("first:" + a);
            for (char b : list) {
                for (char c : list) {
                    for (char d : list) {
                        for (char e : list) {
                            String str = "" + a + b + c + d + e;
                            MessageDigest m = MessageDigest.getInstance("MD5");
                            m.update(str.getBytes("UTF8"));
                            byte s[] = m.digest();
                            String result = "";
                            for (int i = 0; i < s.length; i++) {
                                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
                            }
                            if (result.startsWith("f28e2") && result.endsWith("22a2")) {
                                return str;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("not found");
        return null;
    }

    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println(encrypt());
//        MessageDigest m = MessageDigest.getInstance("MD5");
//        m.update("123".getBytes("UTF8"));
//        byte s[] = m.digest();
//        String result = "";
//        for (int i = 0; i < s.length; i++) {
//            result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
//        }
//        System.out.println(result);
    }
}
