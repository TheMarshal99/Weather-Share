package com.branch.marshall.weathershare.api;

import com.google.gson.annotations.SerializedName;


public class TemperatureData {
    @SerializedName("temp")
    private Double temp;
    @SerializedName("humidity")
    private Integer humidity;
    @SerializedName("pressure")
    private Double pressure;
    @SerializedName("temp_min")
    private Double tempMin;
    @SerializedName("temp_max")
    private Double tempMax;

    /**
     * @return The temp
     */
    public Double getTemp() {
        return temp;
    }

    /**
     * @param temp The temp
     */
    public void setTemp(Double temp) {
        this.temp = temp;
    }

    /**
     * @return The humidity
     */
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * @param humidity The humidity
     */
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * @return The pressure
     */
    public Double getPressure() {
        return pressure;
    }

    /**
     * @param pressure The pressure
     */
    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    /**
     * @return The tempMin
     */
    public Double getTempMin() {
        return tempMin;
    }

    /**
     * @param tempMin The temp_min
     */
    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    /**
     * @return The tempMax
     */
    public Double getTempMax() {
        return tempMax;
    }

    /**
     * @param tempMax The temp_max
     */
    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }
}