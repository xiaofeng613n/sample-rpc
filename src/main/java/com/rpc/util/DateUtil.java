package com.rpc.util;

import java.text.SimpleDateFormat;
import java.util.*;
/**
 * Created by xiao on 2017/8/24.
 */

public class DateUtil {

	public static String format(Date date) {
		return format(date, "yyyy-MM-dd HH:mm:ss");
	}

	public static String format(Date date, String format) {
		try {
			if (format != null && !"".equals(format) && date != null) {
				SimpleDateFormat formatter = new SimpleDateFormat(format);
				return formatter.format(date);
			}
		} catch (Exception e) {
		}
		return null;
	}
}