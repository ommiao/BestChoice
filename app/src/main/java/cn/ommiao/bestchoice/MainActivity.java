package cn.ommiao.bestchoice;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AutoChoiceView acv = findViewById(R.id.acv);
        Button btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acv.refreshData(getData());
            }
        });
    }

    private ArrayList<Choice> getData(){
        ArrayList<Choice> choices = new ArrayList<>();
        Random random = new Random();
        int length = random.nextInt(15) + 3;
        for (int i = 0; i < length; i++) {
            Choice choice = new Choice();
            choice.setColor(ColorUtil.getOneColor());
            choices.add(choice);
        }
        ColorUtil.reset();
        return choices;
    }
}
