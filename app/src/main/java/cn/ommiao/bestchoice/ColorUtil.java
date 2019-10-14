package cn.ommiao.bestchoice;

import java.util.ArrayList;
import java.util.Random;

public class ColorUtil {

    private static ArrayList<String> colors = new ArrayList<String>(){
        {
            add("#c62828");
            add("#ad1457");
            add("#6a1b9a");
            add("#4527a0");
            add("#283593");
            add("#1565c0");
            add("#0277bd");
            add("#00838f");
            add("#00695c");
            add("#2e7d32");
            add("#558b2f");
            add("#9e9d24");
            add("#f9a825");
            add("#ff8f00");
            add("#ef6c00");
            add("#d84315");
            add("#4e342e");
            add("#424242");
            add("#37474f");
        }
    };

    private static ArrayList<String> colorsSelected = new ArrayList<>();

    public static String getOneColor(){
        if(colors.size() == 0){
            return "#ff0000";
        }
        Random random = new Random();
        int r = random.nextInt(colors.size());
        String color = colors.get(r);
        colorsSelected.add(color);
        colors.remove(color);
        return color;
    }

    public static void reset(){
        colors.addAll(colorsSelected);
        colorsSelected.clear();
    }

}
