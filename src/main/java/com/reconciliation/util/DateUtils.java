package com.reconciliation.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {

	public final static String YYYY = "yyyy";
	public final static String MM = "MM";
	public final static String DD = "dd";
	public final static String YYYY_MM_DD = "yyyy-MM-dd";
	public final static String YYYY_MM = "yyyy-MM";
	public final static String HH_MM_SS = "HH:mm:ss";
	public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	public static String formatStr_yyyyMMddHHmmssS = "yyyy-MM-dd HH:mm:ss.S";
	public static String formatStr_yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
	public static String formatStr_yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
	public static String formatStr_yyyyMMddHH = "yyyy-MM-dd HH";
	public static String formatStr_yyyyMMdd = "yyyy-MM-dd";
	public static String formatStr_yyyymmddhhmmsss = "yyyyMMddHHmmssS";
	public static String formatYYYYMMDD = "YYYYMMDD";
	
	public static String[] formatStr = { formatStr_yyyyMMddHHmmss, formatStr_yyyyMMddHHmm, formatStr_yyyyMMddHH, formatStr_yyyyMMdd };
	public static String formatStr_yyyyMMdd2 = "yyyy/MM/dd";

	/**
	 * 构造函数
	 */
	public DateUtils() {
	}

	/**
	 * 日期格式化－将<code>Date</code>类型的日期格式化为<code>String</code>型
	 * 
	 * @param date
	 *            待格式化的日期
	 * @param pattern
	 *            时间样式
	 * @return 一个被格式化了的<code>String</code>日期
	 */
	public static String format(Date date, String pattern) {
		if (date == null)
			return "";
		else
			return getFormatter(pattern).format(date);
	}

	/**
	 * 默认把日期格式化成yyyy-mm-dd格式
	 * 
	 * @param date
	 * @return
	 */
	public static String format(Date date) {
		if (date == null)
			return "";
		else
			return getFormatter(YYYY_MM_DD).format(date);
	}

	/**
	 * 默认把日期格式化成yyyymmdd格式
	 * 
	 * @param date
	 * @return
	 */
	public static String formatYYYYMMDD(Date date) {
		if (date == null){
			return "";
		}else{
			SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
			return  f.format(date);
		}
	}
	
	/**
	 * 把字符串日期默认转换为yyyy-mm-dd格式的Data对象
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date format(String strDate) {
		Date d = null;
		if (strDate == "")
			return null;
		else
			try {
				d = getFormatter(YYYY_MM_DD).parse(strDate);
			} catch (ParseException pex) {
				return null;
			}
		return d;
	}

	/**
	 * 把字符串日期转换为f指定格式的Data对象
	 * 
	 * @param strDate
	 *            ,f
	 * @return
	 */
	public static Date format(String strDate, String f) {
		Date d = null;
		if (strDate == "")
			return null;
		else
			try {
				d = getFormatter(f).parse(strDate);
			} catch (ParseException pex) {
				return null;
			}
		return d;
	}

	/**
	 * 日期解析－将<code>String</code>类型的日期解析为<code>Date</code>型
	 * 
	 *            待格式化的日期
	 * @param pattern
	 *            日期样式
	 * @exception ParseException
	 *                如果所给的字符串不能被解析成一个日期
	 * @return 一个被格式化了的<code>Date</code>日期
	 */
	public static Date parse(String strDate, String pattern) throws ParseException {
		try {
			return getFormatter(pattern).parse(strDate);
		} catch (ParseException pe) {
			throw new ParseException("Method parse in Class DateUtils err: parse strDate fail.", pe.getErrorOffset());
		}
	}

	/**
	 * 获取当前日期
	 * 
	 * @return 一个包含年月日的<code>Date</code>型日期
	 */
	public static synchronized Date getCurrDate() {
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

	/**
	 * 获取当前日期
	 * 
	 * @return 一个包含年月日的<code>String</code>型日期，但不包含时分秒。yyyy-mm-dd
	 */
	public static String getCurrDateStr() {
		return format(getCurrDate(), YYYY_MM_DD);
	}

	/**
	 * 获取当前时间
	 * 
	 * @return 一个包含年月日时分秒的<code>String</code>型日期。hh:mm:ss
	 */
	public static String getCurrTimeStr() {
		return format(getCurrDate(), HH_MM_SS);
	}

	/**
	 * 获取当前完整时间,样式: yyyy－MM－dd hh:mm:ss
	 * 
	 * @return 一个包含年月日时分秒的<code>String</code>型日期。yyyy-MM-dd hh:mm:ss
	 */
	public static String getCurrDateTimeStr() {
		return format(getCurrDate(), YYYY_MM_DD_HH_MM_SS);
	}

	/**
	 * 获取当前年分 样式：yyyy
	 * 
	 * @return 当前年分
	 */
	public static String getYear() {
		return format(getCurrDate(), YYYY);
	}

	/**
	 * 获取当前月分 样式：MM
	 * 
	 * @return 当前月分
	 */
	public static String getMonth() {
		return format(getCurrDate(), MM);
	}

	/**
	 * 获取当前日期号 样式：dd
	 * 
	 * @return 当前日期号
	 */
	public static String getDay() {
		return format(getCurrDate(), DD);
	}

	/**
	 * 按给定日期样式判断给定字符串是否为合法日期数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @param pattern
	 *            日期样式
	 * @return true 如果是，否则返回false
	 */
	public static boolean isDate(String strDate, String pattern) {
		try {
			parse(strDate, pattern);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * 判断给定字符串是否为特定格式日期（包括：年月日yyyy-MM-dd）数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @return true 如果是，否则返回false
	 */
	// public static boolean isDate(String strDate) {
	// try {
	// parse(strDate, YYYY_MM_DD);
	// return true;
	// }
	// catch (ParseException pe) {
	// return false;
	// }
	// }

	/**
	 * 判断给定字符串是否为特定格式年份（格式：yyyy）数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @return true 如果是，否则返回false
	 */
	public static boolean isYYYY(String strDate) {
		try {
			parse(strDate, YYYY);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	public static boolean isYYYY_MM(String strDate) {
		try {
			parse(strDate, YYYY_MM);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * 判断给定字符串是否为特定格式的年月日（格式：yyyy-MM-dd）数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @return true 如果是，否则返回false
	 */
	public static boolean isYYYY_MM_DD(String strDate) {
		try {
			parse(strDate, YYYY_MM_DD);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * 判断给定字符串是否为特定格式年月日时分秒（格式：yyyy-MM-dd HH:mm:ss）数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @return true 如果是，否则返回false
	 */
	public static boolean isYYYY_MM_DD_HH_MM_SS(String strDate) {
		try {
			parse(strDate, YYYY_MM_DD_HH_MM_SS);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * 判断给定字符串是否为特定格式时分秒（格式：HH:mm:ss）数据
	 * 
	 * @param strDate
	 *            要判断的日期
	 * @return true 如果是，否则返回false
	 */
	public static boolean isHH_MM_SS(String strDate) {
		try {
			parse(strDate, HH_MM_SS);
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * 判断给定字符串是否为特定格式时间（包括：时分秒hh:mm:ss）数据
	 * 
	 * @param strTime
	 *            要判断的时间
	 * @return true 如果是，否则返回false
	 */
	// public static boolean isTime(String strTime) {
	// try {
	// parse(strTime, HH_MM_SS);
	// return true;
	// }
	// catch (ParseException pe) {
	// return false;
	// }
	// }

	/**
	 * 判断给定字符串是否为特定格式日期时间（包括：年月日时分秒 yyyy-MM-dd hh:mm:ss）数据
	 * 
	 * @param strDateTime
	 *            要判断的日期时间
	 * @return true 如果是，否则返回false
	 */
	// public static boolean isDateTime(String strDateTime) {
	// try {
	// parse(strDateTime, YYYY_MM_DD_HH_MM_SS);
	// return true;
	// }
	// catch (ParseException pe) {
	// return false;
	// }
	// }

	/**
	 * 获取一个简单的日期格式化对象
	 * 
	 * @return 一个简单的日期格式化对象
	 */
	private static SimpleDateFormat getFormatter(String parttern) {
		return new SimpleDateFormat(parttern);
	}

	/**
	 * 获取给定日前的后intevalDay天的日期
	 * 
	 * @param refenceDate
	 *            给定日期（格式为：yyyy-MM-dd）
	 * @param intevalDays
	 *            间隔天数
	 * @return 计算后的日期
	 */
	public static String getNextDate(String refenceDate, int intevalDays) {
		try {
			return getNextDate(parse(refenceDate, YYYY_MM_DD), intevalDays);
		} catch (Exception ee) {
			return "";
		}
	}

	/**
	 * 获取给定日前的后intevalDay天的日期
	 * 
	 * @param refenceDate
	 *            Date 给定日期
	 * @param intevalDays
	 *            int 间隔天数
	 * @return String 计算后的日期
	 */
	public static String getNextDate(Date refenceDate, int intevalDays) {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(refenceDate);
			calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + intevalDays);
			return format(calendar.getTime(), YYYY_MM_DD);
		} catch (Exception ee) {
			return "";
		}
	}

	public static long getIntevalDays(String startDate, String endDate) {
		try {
			return getIntevalDays(parse(startDate, YYYY_MM_DD), parse(endDate, YYYY_MM_DD));
		} catch (Exception ee) {
			return 0l;
		}
	}

	public static long getIntevalDays(Date startDate, Date endDate) {
		try {
			Calendar startCalendar = Calendar.getInstance();
			Calendar endCalendar = Calendar.getInstance();

			startCalendar.setTime(startDate);
			endCalendar.setTime(endDate);
			long diff = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();

			return (diff / (1000 * 60 * 60 * 24));
		} catch (Exception ee) {
			return 0l;
		}
	}

	/**
	 * 求当前日期和指定字符串日期的相差天数
	 *
	 * @param startDate
	 * @return
	 */
	public static long getTodayIntevalDays(String startDate) {
		try {
			// 当前时间
			Date currentDate = new Date();

			// 指定日期
			SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
			Date theDate = myFormatter.parse(startDate);

			// 两个时间之间的天数
			long days = (currentDate.getTime() - theDate.getTime()) / (24 * 60 * 60 * 1000);

			return days;
		} catch (Exception ee) {
			return 0l;
		}
	}

	public static Date parseToDate(String dateTimeStr) {
		if (dateTimeStr == null)
			return null;
		Date d = null;
		int formatStrLength = formatStr.length;
		for (int i = 0; i < formatStrLength; i++) {
			d = parseToDate2(dateTimeStr, formatStr[i]);
			if (d != null) {
				break;
			}
		}
		return d;
	}

	private static Date parseToDate2(String dateTimeStr, String formatString) {
		Date d = null;
		SimpleDateFormat sdf = new SimpleDateFormat(formatString);
		try {
			d = sdf.parse(dateTimeStr);
		} catch (ParseException pe) {

		}
		return d;
	}

	public static String dateTimeToString(Date datetime) {
		// dateTime=dateTime.substring(0,4)+dateTime.substring(5,7)+dateTime.substring(8,10)+dateTime.substring(11,13)+dateTime.substring(14,16)+dateTime.substring(17,19);
		// return dateTime;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(datetime);
		String dateTime = calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1 > 9 ? "" : "0") + (calendar.get(Calendar.MONTH) + 1) + "" + (calendar.get(Calendar.DATE) > 9 ? "" : "0") + calendar.get(Calendar.DATE) + "" + (calendar.get(Calendar.HOUR_OF_DAY) > 9 ? "" : "0") + calendar.get(Calendar.HOUR_OF_DAY) + "" + (calendar.get(Calendar.MINUTE) > 9 ? "" : "0") + calendar.get(Calendar.MINUTE) + "" + (calendar.get(Calendar.SECOND) > 9 ? "" : "0") + calendar.get(Calendar.SECOND);
		return dateTime;
	}

	/**
	 * 由年、月份，获得当前月的最后一天
	 * 
	 * @param year
	 *            month 月份
	 * @return
	 * @throws ParseException
	 */
	public static String getLastDayOfMonth(String year, String month) throws ParseException {
		String LastDay = "";
		Calendar cal = Calendar.getInstance();
		Date date_;
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(year + "-" + month + "-");
		cal.setTime(date);
		int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, value);
		date_ = cal.getTime();
		LastDay = new SimpleDateFormat("yyyy-MM-dd").format(date_);
		return LastDay;
	}

	/**
	 * 获取指定月的天数
	 * 
	 * @param year
	 * @param month
	 * @return
	 * @throws ParseException
	 */
	public static int getMaxDayOfMonth(String year, String month) {
		Calendar cal = Calendar.getInstance();
		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM").parse(year + "-" + month);
			cal.setTime(date);
			int value = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			return value;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}

	}

	/**
	 * 获取前一天的日期
	 * 
	 * @param releasetime
	 * @return
	 * @throws ParseException
	 */
	public static String getPreviousDay(String releasetime) {
		Calendar cal = Calendar.getInstance();
		Date date;
		String yestedayDate = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(releasetime);
			cal.setTime(date);
			cal.add(Calendar.DATE, -1); // 得到前一天
			yestedayDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return yestedayDate;
	}
	
	
	/**
	 * 比较日期大小
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int compare_date(long date1, long date2) {
	        try {
	            if (date1 > date2) {
	                return 1;
	            } else if (date1 < date2) {
	                return -1;
	            } else {
	                return 0;
	            }
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	        return 0;
	    }
	
	
	/**
     * 通过时间秒毫秒数获取两个时间的间隔
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1,Date date2){
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
        return days;
    }


	/**
	 * 给指定时间加一天
	 * @param date
	 * @param count
	 * @return
	 */
	public static Date addOneDay(Date date,int count){
    	Calendar   calendar   =   new   GregorianCalendar(); 
        calendar.setTime(date); 
        //calendar.add(calendar.YEAR, 1);//把日期往后增加一年.整数往后推,负数往前移动
       // calendar.add(calendar.DAY_OF_MONTH, 1);//把日期往后增加一个月.整数往后推,负数往前移动
        calendar.add(calendar.DATE,count);//把日期往后增加一天.整数往后推,负数往前移动 
        //calendar.add(calendar.WEEK_OF_MONTH, 1);//把日期往后增加一个月.整数往后推,负数往前移动
        date=calendar.getTime();   //这个时间就是日期往后推一天的结果 
        return date;
    }
    
    
    
    /** 
     * 两个时间相差距离多少天多少小时多少分多少秒 
     * @param str1 时间参数 1 格式：1990-01-01 12:00:00 
     * @param str2 时间参数 2 格式：2009-01-01 12:00:00 
     * @return long[] 返回值为：{天, 时, 分, 秒} 
     */  
    public static long getDistanceTimes(String str1, String str2) {  
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        Date one;  
        Date two;  
        long day = 0;  
        long hour = 0;  
        long min = 0;  
        long sec = 0;  
        try {  
            one = df.parse(str1);  
            two = df.parse(str2);  
            long time1 = one.getTime();  
            long time2 = two.getTime();  
            long diff ;  
            if(time1<time2) {  
                diff = time2 - time1;  
            } else {  
                diff = time1 - time2;  
            }  
            day = diff / (24 * 60 * 60 * 1000);  
            hour = (diff / (60 * 60 * 1000) - day * 24);  
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);  
            sec = (diff/1000-day*24*60*60-hour*60*60-min*60);  
        } catch (ParseException e) {  
            e.printStackTrace();  
        }  
        //long[] times = {day, hour, min, sec};  
        return sec;  
    }  
    
    
    /**
     * yyyyMMddHHmmss  格式日期  转换 yyyy-MM-dd HH:mm:ss 
     * @param strDate
     * @return
     */
    public static String convertDateFormat(String strDate){
    	SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return f1.format(f.parse(strDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
    }
    
    /**
     * 时间戳  转换   yyyy-MM-dd HH:mm:ss 
     * @param time
     * @return
     * @throws ParseException
     */
    public static Date timestampToYYYYMMDDHHMMSS(long time) throws ParseException{
    	SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
        String d = format.format(time);  
        return format.parse(d);  
    }
    
    /**
     * yyyy-MM-dd   转换    yyyyMMdd
     * @param str
     * @return
     */
    public static String formatDate(String str){
      SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd");
	  SimpleDateFormat sf2 =new SimpleDateFormat("yyyyMMdd");
	  String sfstr = "";
	  try {
	      sfstr = sf2.format(sf1.parse(str));
	  } catch (ParseException e) {
	   e.printStackTrace();
	  }
	  return sfstr;
	 }
	
    
    /**
     * 获取当前时间   yyyyMMddHHmmss
     * @return
     */
    public static String formatYYYYMMDDHHMMSS(){
	  SimpleDateFormat sf2 =new SimpleDateFormat("yyyyMMddHHmmss");
	  return sf2.format(new Date());
	 }
    
	/**
	 * yyyyMMddHHmmss字符串日期 转换 Date 格式
	 * @return
	 */
	public static Date formatStrToDate(String strDate) {
		SimpleDateFormat sf2 =new SimpleDateFormat("yyyyMMddHHmmss");
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sf1.parse(sf1.format(sf2.parse(strDate)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * yyyy-MM-dd HH:mm:ss字符串日期 转换 Date 格式
	 * @return
	 */
	public static Date formatStrToDate1(String strDate) {
		SimpleDateFormat sf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sf1.parse(strDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    
	/**
	 * 拆分一个Date 格式日期 为  [0]: yyyy-MM-dd    [1]:HH:mm:ss
	 * @param date
	 * @return  
	 */
	public static String[] getSplitDate(Date date){
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat f1 = new SimpleDateFormat("HH:mm:ss");
		String[] dateArr = {f.format(date),f1.format(date)};
		return dateArr;
	}

	/**
	 * 获取指定日期-指定日期  间隔的所有日期
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<String> getDate(String start, String end){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<String> list = new ArrayList<String>(); //保存日期集合
		try {
			Date date_start = sdf.parse(start);
			Date date_end = sdf.parse(end);
			Date date =date_start;
			Calendar cd = Calendar.getInstance();//用Calendar 进行日期比较判断
			while (date.getTime()<=date_end.getTime()){
				list.add(sdf.format(date));
				cd.setTime(date);
				cd.add(Calendar.DATE, 1);//增加一天 放入集合
				date=cd.getTime();
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return list;
	}




	public static void main(String[] args) {
		try {
			String s = "20180316165654";
			System.out.println(DateUtils.convertDateFormat(s));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}