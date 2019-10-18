package cn.ommiao.bestchoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity implements AutoChoiceView.OnPointerStopListener, View.OnClickListener {

    private ArrayList<Choice> choices = new ArrayList<>();
    private ChoiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AutoChoiceView acv = findViewById(R.id.acv);
        acv.setOnPointerStopListener(this);
        ImageView ivRefresh = findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(view -> acv.refreshData(getData()));
        ImageView ivSelect = findViewById(R.id.iv_select);
        ivSelect.setOnClickListener(view -> acv.select());
        ListView lvChoice = findViewById(R.id.lv_choice);
        View emptyView = findViewById(R.id.fl_empty);
        ImageView ivAdd = emptyView.findViewById(R.id.iv_add);
        ivAdd.setOnClickListener(this);
        lvChoice.setEmptyView(emptyView);
        adapter = new ChoiceAdapter(this, R.layout.item_choice, choices);
        lvChoice.setAdapter(adapter);
    }

    private ArrayList<Choice> getData(){
        choices.clear();
        Random random = new Random();
        int length = random.nextInt(15) + 3;
        for (int i = 0; i < length; i++) {
            Choice choice = new Choice();
            choice.setColor(ColorUtil.getOneColor());
            choice.setWeight(random.nextInt(10) + 1);
            choice.setDesc((i + 1) + ". color -> " + choice.getColor());
            choices.add(choice);
        }
        ColorUtil.reset();
        adapter.notifyDataSetChanged();
        adapter.notifyDataSetInvalidated();
        return choices;
    }

    @Override
    public void onPointerStop(Choice choice) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_add:
                startActivityForResult(new Intent(this, ChoiceAddActivity.class), 666);
                break;
        }
    }
}
