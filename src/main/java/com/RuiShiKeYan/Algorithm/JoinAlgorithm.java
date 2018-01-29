package com.RuiShiKeYan.Algorithm;

import net.sourceforge.pinyin4j.*;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created with IntelliJ IDEA User:huangming Date:2017/9/20 Time:下午4:52
 */
public class JoinAlgorithm {

	private HanyuPinyinOutputFormat format = null;
	static JoinAlgorithm joinAlgorithm;
	private JoinAlgorithm() {
		initParam();
	}
    public static JoinAlgorithm getNewInstance()
	{
		return new JoinAlgorithm();
	}

	public static JoinAlgorithm getInstance()
	{
		if(joinAlgorithm ==null)
           initInstance();
		return joinAlgorithm;
	}
	private static synchronized  void  initInstance()
	{
		if(joinAlgorithm == null)
			joinAlgorithm= new JoinAlgorithm();
	}

	private void initParam() {
		format = new HanyuPinyinOutputFormat();
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	}

	// jin改 并不希望改变原始String
	public JoinResultEnum joinAlgorithm(String source0, String dest0) {
		if (source0 == null && dest0 == null)
			return JoinResultEnum.matched; // 这个可以讨论下是返回啥结果

		if ((source0 == null && dest0 != null) || (source0 != null && dest0 == null))
			return JoinResultEnum.notMatched;
		if ((source0.equals("") && !dest0.equals("")) || (!source0.equals("") && dest0.equals("")))
			return JoinResultEnum.notMatched;
		// jin改 通常情况需要小写化
		String source = source0.trim().toLowerCase();
		String dest = dest0.trim().toLowerCase();

		if (source.equals(dest))
			return JoinResultEnum.matched;

        if (lengthEqualMatch(source, dest))
            return JoinResultEnum.partlyMatched;
        if (startEndMatch(source, dest))
            return JoinResultEnum.partlyMatched;

        if (lengthOneMatch(source, dest))
            return JoinResultEnum.partlyMatched;
		// 拼音是否一样  ,白医生需求删掉
		if (pinyinMatched(source, dest))
			return JoinResultEnum.partlyMatched;

		return JoinResultEnum.notMatched;
	}

	// 长度相同，拼音相同
	private boolean pinyinMatched(String source, String dest) {
		if (source.length() >= 3 && source.length() == dest.length()) {
			if (getStringPinYin(source).equals(getStringPinYin(dest))) {
				// jin改，拼音相同的情况下并不希望他们差异字太多
				int diff = 0;
				for (int i = 0; i < source.length(); i++) {
					if (source.charAt(i) != dest.charAt(i))
						diff++;
					if(diff>1)
						break;
				}
				if (diff <= (source.length() / 3))
					return true;
			}
		}
		return false;
	}

	// 长度相同，且长度大于等于5，他们有一个字的差别且不在开头
	private boolean lengthEqualMatch(String source, String dest) {
		if (source.length() == dest.length() && source.length() >= 5) {
			int diff = 0;
			for(int i=0;i<source.length();i++)
			{
				if(source.charAt(i)!=dest.charAt(i))
				{
					diff+= (i==0 ? 2 : 1);
				}
				if(diff>1)
					break;
			}
			if(diff<=1)
				return true;
		}
		return false;
	}

	// 长度相差为1，且短字符串长度大于等于4，短字符串是长字符串之中删去一个字
	private boolean lengthOneMatch(String source, String dest) {
		if (Math.abs(source.length() - dest.length()) != 1)
			return false;

		boolean flag = source.length() > dest.length() ? true : false;
		if (!flag) {
			String tempStr = source;
			source = dest;
			dest = tempStr;
		}
		for (int i = 0; i < source.length(); i++) {
			if (dest.equals(source.substring(0, i) + source.substring(i + 1, source.length())))
				return true;
		}
		return false;
	}

	// 短字符长度大于等于4，且与较长字符串字符相差在(length/3个),且较短字符串是较长字符串的开头或结尾
	private boolean startEndMatch(String source, String dest) {
		int sourceLength = source.length();
		int destLength = dest.length();
		int tempLength = sourceLength > destLength ? sourceLength / 3 : destLength / 3;

		if (Math.abs(sourceLength - destLength) <= tempLength) {
			if (source.startsWith(dest) || dest.startsWith(source) || source.endsWith(dest) || dest.endsWith(source)) {
				return true;
			}
		}
		return false;
	}

	private String getStringPinYin(String pinyin) {
		StringBuilder sb = new StringBuilder();
		String tempPinyin = null;
		for (int i = 0; i < pinyin.length(); ++i) {
			tempPinyin = getCharacterPinYin(pinyin.charAt(i));
			if (tempPinyin == null) {
				// 如果str.charAt(i)非汉字，则保持原样
				sb.append(pinyin.charAt(i));
			} else {
				// jin改 前后鼻音相同
				if (tempPinyin.endsWith("ng"))
					tempPinyin = tempPinyin.substring(0, tempPinyin.length() - 1);
				sb.append(tempPinyin);
			}
		}
		return sb.toString();
	}

	// 转换单个字符
	private String getCharacterPinYin(char c) {
		String[] pinyin = null;
		try {
			pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		// 如果c不是汉字，toHanyuPinyinStringArray会返回null
		if (pinyin == null)
			return null;
		// 只取一个发音，如果是多音字，仅取第一个发音
		return pinyin[0];
	}

}
