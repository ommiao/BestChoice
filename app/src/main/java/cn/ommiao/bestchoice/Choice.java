package cn.ommiao.bestchoice;

import java.io.Serializable;

public class Choice implements Serializable {

    public static float PER_ANGLE;

    private String desc;
    private String color;
    private float startAngle;
    private int weight = 1;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
