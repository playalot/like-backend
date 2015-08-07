package utils;

import play.Play;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * 敏感词过滤 DFA 算法
 */
public class SensitiveWord {

    //敏感词文件
//    @SuppressWarnings("ConstantConditions")
//    private static File file = Play.application().getFile("conf/sensitiveword.txt");
    //敏感词库模型
    private Map<String, Map<String, String>> sensitiveWordMap = null;

    //最小匹配规则
    public static final int MIN_MATCH_TYPE = 1;
    //最大匹配规则
    public static final int MAX_MATCH_TYPE = 2;
    //默认匹配规则
    private int current_match_type = 2;

    //敏感词结束标识
    private static final String IS_END_TRUE = "1";
    private static final String IS_END_FALSE = "0";

    public int getCurrent_match_type() {
        return current_match_type;
    }

    public void setCurrent_match_type(int current_match_type) {
        this.current_match_type = current_match_type;
    }

    /**
     * 将敏感词构造成 HashMap 的 DFA 模型
     */
    @SuppressWarnings("unchecked")
    private void addSensitiveWordToDFAModel() {
        if (null == sensitiveWordMap || sensitiveWordMap.size() == 0) {
            Set<String> wordSet = null;//读取文本中的敏感词列表
            try {
                wordSet = readSensitiveWordFile();
            } catch (IOException e) {
                System.out.println("加载敏感词库失败");
            }
            if (null == wordSet || wordSet.size() < 0) {
                System.out.println("加载敏感词库失败");
            } else {
                sensitiveWordMap = new HashMap<String, Map<String, String>>(wordSet.size());//防止扩容
                Map nowPointMap;
                Map<String, String> newWordMap;
                for (String key : wordSet) {
                    nowPointMap = sensitiveWordMap;
                    for (int i = 0; i < key.length(); i++) {
                        char keyChar = key.charAt(i);// 获取到敏感词的单个汉字
                        Object tmpMap = nowPointMap.get(String.valueOf(keyChar));
                        if (tmpMap != null) {//这个字已经放到 Map 中了，直接将指针指倒该 map，进行下个字的判断
                            nowPointMap = (Map) tmpMap;
                        } else {
                            newWordMap = new HashMap<String, String>();
                            newWordMap.put("isEnd", IS_END_FALSE);//默认为不是结束，后续判断再修改
                            nowPointMap.put(String.valueOf(keyChar), newWordMap);
                            nowPointMap = newWordMap;//移动到下一层
                        }

                        if (i == key.length() - 1) {//词结束
                            nowPointMap.put("isEnd", IS_END_TRUE);
                        }
                    }
                }
            }
        }
    }

    /**
     * 读取敏感词
     * 文件中，一个敏感词一行
     *
     * @return 敏感词集合
     */
    private Set<String> readSensitiveWordFile() throws IOException {
        Set<String> set = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
//            inputStream = new FileInputStream(file);
            inputStream = Play.application().resourceAsStream("sensitivewords.txt");
            if (inputStream.available() > 0) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
                set = new HashSet<String>();
                String word;
                while (null != (word = bufferedReader.readLine())) {
                    set.add(word);
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return set;
    }

    /**
     * 检查指定位置后是否是敏感词，如果是返回长度
     *
     * @param text       需要检查的文本
     * @param beginIndex 起始位置
     * @return 匹配到的敏感词长度
     */
    private int check(String text, int beginIndex) {
        boolean flag = false;
        int matchFlag = 0;
        char word;
        Map nowPointMap = sensitiveWordMap;
        for (int i = beginIndex; i < text.length(); i++) {
            word = text.charAt(i);
            nowPointMap = (Map) nowPointMap.get(String.valueOf(word));
            if (nowPointMap != null) {//是敏感词的一部分
                matchFlag++;
                if (IS_END_TRUE.endsWith((String) nowPointMap.get("isEnd"))) {
                    flag = true;
                    if (MIN_MATCH_TYPE == current_match_type) {//最小匹配时，停止
                        break;
                    }
                }
            } else {
                break;
            }
        }
        if (matchFlag < 2 || !flag) {//1个字不算敏感词
            matchFlag = 0;
        }
        return matchFlag;
    }

    /**
     * 判断是否包含敏感词
     *
     * @param text 判断文本
     * @return T/F
     */
    public boolean isContainSensitiveWord(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (check(text, i) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 替换敏感词，默认替换为 *
     *
     * @param text 原文
     * @return 替换后的文本
     */
    public String replaceSensitiveWord(String text) {
        return replaceSensitiveWord(text, "*");
    }

    /**
     * 用指定的字符替换敏感词
     *
     * @param text        原文
     * @param replaceChar 替换的字符
     * @return 替换后的文本
     */
    public String replaceSensitiveWord(String text, String replaceChar) {
        List<String> sensitiveWord = getSensitiveWord(text);
        String result = text;
        for (String s : sensitiveWord) {
            String replace = "";
            for (int i = 0; i < s.length(); i++) {
                replace += replaceChar;
            }
            result = result.replaceFirst(s, replace);

        }
        return result;
    }

    /**
     * 返回文本中的所有敏感词
     *
     * @param text 待检测的文本
     * @return 包含的都有敏感词集合
     */
    public List<String> getSensitiveWord(String text) {
        List<String> sensitiveWordSet = new ArrayList<String>();
        for (int i = 0; i < text.length(); i++) {
            int length = check(text, i);
            if (length > 0) {
                sensitiveWordSet.add(text.substring(i, i + length));
                i = i + length - 1;
            }
        }
        return sensitiveWordSet;
    }

    private SensitiveWord() {
        addSensitiveWordToDFAModel();
    }

    private static class SingletonHolder {
        private static final SensitiveWord INSTANCE = new SensitiveWord();
    }

    public static SensitiveWord getInstance() {
        return SingletonHolder.INSTANCE;
    }
}