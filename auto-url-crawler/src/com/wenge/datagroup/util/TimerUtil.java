package com.wenge.datagroup.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 时间类型处理工具类
 * 
 * @author WangHL
 * @version 1.1 2015-06-03
 * @since jdk1.7
 *
 */
public class TimerUtil {

	public static Random random = new Random();
	
	/**
	 * 休眠方法（小时）
	 * 
	 * @param hour
	 */
	public static void sleep_hour(int hour) {
		try {
			Thread.sleep(1000 * 60 * 60 * hour);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 休眠方法（分钟）
	 * 
	 * @param min
	 */
	public static void sleep_min(int min) {
		try {
			Thread.sleep(1000 * 60 * min);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void sleep_millisecond(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 休眠方法（秒）
	 * 
	 * @param second
	 */
	public static void sleep_second(int second) {
		try {
			Thread.sleep(1000 * second);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 根据当前服务器时间处于站点的访问高峰或低谷时间段，返回对应的休眠时间
	 * 
	 * @return sleepMin 休眠时间（分钟）
	 */
	@SuppressWarnings("static-access")
	public static int getSleetMin() {
		/* 默认平时执行任务间隔为30分钟 */
		int sleepMin = 30;
		try {
			Date now = new Date();
			Calendar calender = Calendar.getInstance();
			calender.setTime(now);
			int nowHour = calender.get(calender.HOUR_OF_DAY);
			/* 先判断是否在访问高峰时段 */
			if (nowHour >= 20 && nowHour < 23) {
				/* 高峰时段间隔20分钟 */
				sleepMin = 20;
			}
			/* 是否在访问流量低的时间段 */
			else if (nowHour >= 23 || nowHour <= 7) {
				/* 低谷时间段间隔2个小时 */
				sleepMin = 60;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return sleepMin;
	}

	/**
	 * 休眠方法（秒） 随机分配休眠时间
	 */
	public static int randomSleepSecond() {
		int sleepTime = 1000 * 60 * 30;
		try {
			sleepTime += (random.nextInt(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sleepTime;
	}

	public static int randomSleepSecond_short() {
		int sleepTime = 3;
		try {
			sleepTime += (random.nextInt(10));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sleepTime;
	}

	public static String parseStr(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return simpleDateFormat.format(date);
	}

	public static String parseDayStr(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}

	public static String praseDateStr(String dateStr) {
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date date = simpleDateFormat1.parse(dateStr);
			return simpleDateFormat2.format(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
