package com.oto.edyd.model;

public class HoursWeatherBean {

	private String weather_id; //天气标识ID
	private String weather; //天气
	private String temp_a; // 低温
	private String temp_b; //高温
	private String sh; //开始时间
	private String eh; //结束时间
	private String date; //日期
	private String sfdate; //完整开始时间
	private String efdate; //完整结束时间
	private String time;

	public String getWeather_id() {
		return weather_id;
	}

	public void setWeather_id(String weather_id) {
		this.weather_id = weather_id;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getTemp_a() {
		return temp_a;
	}

	public void setTemp_a(String temp_a) {
		this.temp_a = temp_a;
	}

	public String getTemp_b() {
		return temp_b;
	}

	public void setTemp_b(String temp_b) {
		this.temp_b = temp_b;
	}

	public String getSh() {
		return sh;
	}

	public void setSh(String sh) {
		this.sh = sh;
	}

	public String getEh() {
		return eh;
	}

	public void setEh(String eh) {
		this.eh = eh;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSfdate() {
		return sfdate;
	}

	public void setSfdate(String sfdate) {
		this.sfdate = sfdate;
	}

	public String getEfdate() {
		return efdate;
	}

	public void setEfdate(String efdate) {
		this.efdate = efdate;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
