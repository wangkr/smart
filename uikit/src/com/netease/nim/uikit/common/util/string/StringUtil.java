package com.netease.nim.uikit.common.util.string;

import android.text.TextUtils;

import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class StringUtil {
	public static final String Empty = "";
	
	public static String getPercentString(float percent) {
		return String.format(Locale.US, "%d%%", (int) (percent * 100));
	}
	/**
	 * 删除字符串中的空白符
	 *
	 * @param content
	 * @return String
	 */
	public static String removeBlanks(String content) {
		if (content == null) {
			return null;
		}
		StringBuilder buff = new StringBuilder();
		buff.append(content);
		for (int i = buff.length() - 1; i >= 0; i--) {
			if (' ' == buff.charAt(i) || ('\n' == buff.charAt(i)) || ('\t' == buff.charAt(i))
					|| ('\r' == buff.charAt(i))) {
				buff.deleteCharAt(i);
			}
		}
		return buff.toString();
	}

	/**
	 * 移除类似于 "[xxx] xxxx"中的"[xxx] "
	 * @param content
	 * @param lbracket
	 * @param rbracket
     * @return
     */
	public static String removeFirstBracket(String content, char lbracket, char rbracket) {
		if (content == null) {
			return null;
		}
		StringBuilder buff = new StringBuilder();
		buff.append(content);
		boolean l = false;
		while (buff.length() > 0) {
			if (!l) {
				l = true;
				if (buff.charAt(0) == lbracket) {
					buff.deleteCharAt(0);
				} else {
					break;
				}
			} else {
				if (buff.charAt(0) == rbracket) {
					buff.deleteCharAt(0);
					break;
				}
				buff.deleteCharAt(0);
			}
		}
		// 移除第一个空格
		if (buff.length() > 0 && buff.charAt(0) == ' ') {
			buff.deleteCharAt(0);
		}
		return buff.toString();
	}
	/**
	 * 获取32位uuid
	 *
	 * @return
	 */
	public static String get32UUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	public static boolean isEmpty(String input) {
		return TextUtils.isEmpty(input);
	}
	
	/**
	 * 生成唯一号
	 *
	 * @return
	 */
	public static String get36UUID() {
		UUID uuid = UUID.randomUUID();
		String uniqueId = uuid.toString();
		return uniqueId;
	}
	
	public static String makeMd5(String source) {
		return MD5.getStringMD5(source);
	}
	
    public static final String filterUCS4(String str) {
		if (TextUtils.isEmpty(str)) {
			return str;
		}

		if (str.codePointCount(0, str.length()) == str.length()) {
			return str;
		}

		StringBuilder sb = new StringBuilder();

		int index = 0;
		while (index < str.length()) {
			int codePoint = str.codePointAt(index);
			index += Character.charCount(codePoint);
			if (Character.isSupplementaryCodePoint(codePoint)) {
				continue;
			}

			sb.appendCodePoint(codePoint);
		}

		return sb.toString();
	}

    /**
     * counter ASCII character as one, otherwise two
     *
     * @param str
     * @return count
     */
    public static int counterChars(String str) {
        // return
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            int tmp = (int) str.charAt(i);
            if (tmp > 0 && tmp < 127) {
                count += 1;
            } else {
                count += 2;
            }
        }
        return count;
    }

	/**
	 * 随机生成字符串
	 * @param length 长度
	 * @return
	 */
	public static String getRandomString(int length) { //length表示生成字符串的长度
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random(new Date().getTime());
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}
}
