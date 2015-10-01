package com.oto.edyd.model;

import java.util.List;

public class WeatherBean {

	//today
	private String city; //城市
	private String data; //日期
	private String week; //星期
	private String temperature; //当天温度
	private String weather; //今日天气
	private String weather_id; //天气唯一标识
	private String weather_id_b;
	private String wind; //风向风力
	private String dressing_index; //穿衣指数
	private String dressing_advice; //穿衣建议
	private String uv_index; //紫外线强度
	private String comfort_index; //舒适度指数
	private String wash_index; //洗车指数
	private String travel_index; //旅游指数
	private String exercise_index; //晨练指数
	private String drying_index; //干燥指数

	//sk
	private String temp; //当前温度
	private String wind_direction; //当前风向
	private String wind_strength;//当前风力
	private String felt_temp; //体感温度
	private String humidity; //湿度
	private String release; //发布

	//future
	private List<FutureWeatherBean> futureList; //未来天气

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getWeather() {
		return weather;
	}

	public void setWeather(String weather) {
		this.weather = weather;
	}

	public String getWeather_id() {
		return weather_id;
	}

	public void setWeather_id(String weather_id) {
		this.weather_id = weather_id;
	}

	public String getWind() {
		return wind;
	}

	public void setWind(String wind) {
		this.wind = wind;
	}

	public String getDressing_index() {
		return dressing_index;
	}

	public void setDressing_index(String dressing_index) {
		this.dressing_index = dressing_index;
	}

	public String getDressing_advice() {
		return dressing_advice;
	}

	public void setDressing_advice(String dressing_advice) {
		this.dressing_advice = dressing_advice;
	}

	public String getUv_index() {
		return uv_index;
	}

	public void setUv_index(String uv_index) {
		this.uv_index = uv_index;
	}

	public String getComfort_index() {
		return comfort_index;
	}

	public void setComfort_index(String comfort_index) {
		this.comfort_index = comfort_index;
	}

	public String getWash_index() {
		return wash_index;
	}

	public void setWash_index(String wash_index) {
		this.wash_index = wash_index;
	}

	public String getTravel_index() {
		return travel_index;
	}

	public void setTravel_index(String travel_index) {
		this.travel_index = travel_index;
	}

	public String getExercise_index() {
		return exercise_index;
	}

	public void setExercise_index(String exercise_index) {
		this.exercise_index = exercise_index;
	}

	public String getDrying_index() {
		return drying_index;
	}

	public void setDrying_index(String drying_index) {
		this.drying_index = drying_index;
	}

	public String getTemp() {
		return temp;
	}

	public void setTemp(String temp) {
		this.temp = temp;
	}

	public String getWind_direction() {
		return wind_direction;
	}

	public void setWind_direction(String wind_direction) {
		this.wind_direction = wind_direction;
	}

	public String getWind_strength() {
		return wind_strength;
	}

	public void setWind_strength(String wind_strength) {
		this.wind_strength = wind_strength;
	}

	public String getFelt_temp() {
		return felt_temp;
	}

	public void setFelt_temp(String felt_temp) {
		this.felt_temp = felt_temp;
	}

	public String getHumidity() {
		return humidity;
	}

	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}

	public String getRelease() {
		return release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	public List<FutureWeatherBean> getFutureList() {
		return futureList;
	}

	public void setFutureList(List<FutureWeatherBean> futureList) {
		this.futureList = futureList;
	}

	public String getWeather_id_b() {
		return weather_id_b;
	}

	public void setWeather_id_b(String weather_id_b) {
		this.weather_id_b = weather_id_b;
	}
}
