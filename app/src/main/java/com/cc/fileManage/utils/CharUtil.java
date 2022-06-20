package com.cc.fileManage.utils;

import java.util.regex.Pattern;

public class CharUtil {

    // 根据Unicode编码完美的判断中文汉字和符号
    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
            return true;
        }
        return false;
    }

    // 完整的判断中文汉字和符号
    public static boolean isChinese(String strName) {
        char[] ch = strName.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            char c = ch[i];
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    // 只能判断部分CJK字符（CJK统一汉字）
    public static boolean isChineseByREG(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[\\u4E00-\\u9FBF]+");
        return pattern.matcher(str.trim()).find();
    }

    // 只能判断部分CJK字符（CJK统一汉字）
    public static boolean isChineseByName(String str) {
        if (str == null) {
            return false;
        }
        // 大小写不同：\\p 表示包含，\\P 表示不包含
        // \\p{Cn} 的意思为 Unicode 中未被定义字符的编码，\\P{Cn} 就表示 Unicode中已经被定义字符的编码
        String reg = "\\p{InCJK Unified Ideographs}&&\\P{Cn}";
        Pattern pattern = Pattern.compile(reg);
        return pattern.matcher(str.trim()).find();
    }

    public  static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s = encode;
                return s;
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return "";
    }

    //获取字符串中某个字符的数量
    public static int charNum(String str,String s){
        int i = 0;

        if(str == null || s == null){
            return i;
        }

        if(s.length() == 1){
            //字符数组
            char[] ch = str.toCharArray();
            for(char c : ch){
                if(c == s.charAt(0)){
                    i++;
                }
            }
        }else{
            while(str.indexOf(s) != -1){
                i++;
                str = str.substring(str.indexOf(s) + s.length());
            }
        }
        return i;
    }

    //验证文件名是否合法
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.length() > 255)
            return false;
        else
            return fileName.matches("[^/\\\\<>*?|\"]+");
    }
}