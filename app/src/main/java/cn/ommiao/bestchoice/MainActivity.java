package cn.ommiao.bestchoice;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity implements AutoChoiceView.OnPointerStopListener, View.OnClickListener, ChoiceAdapter.OnChoiceClickListener {

    private ArrayList<Choice> choices = new ArrayList<>();
    private ChoiceAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final AutoChoiceView acv = findViewById(R.id.acv);
        acv.setOnPointerStopListener(this);
        ImageView ivRefresh = findViewById(R.id.iv_refresh);
        ivRefresh.setOnClickListener(view -> {
            if(choices.size() >= 2){
                acv.refreshData(choices);
            } else {
                Toast.makeText(this, "至少添加两个选项", Toast.LENGTH_SHORT).show();
            }
        });
        ImageView ivSelect = findViewById(R.id.iv_select);
        ivSelect.setOnClickListener(view -> acv.select());
        ListView lvChoice = findViewById(R.id.lv_choice);
        View emptyView = findViewById(R.id.fl_empty);
        ImageView ivAdd = emptyView.findViewById(R.id.iv_add);
        ivAdd.setOnClickListener(this);
        lvChoice.setEmptyView(emptyView);
        @SuppressLint("InflateParams")
        View footer = LayoutInflater.from(this).inflate(R.layout.layout_list_footer, null);
        footer.findViewById(R.id.iv_add).setOnClickListener(this);
        lvChoice.addFooterView(footer);
        adapter = new ChoiceAdapter(this, choices);
        adapter.setOnChoiceClickListener(this);
        lvChoice.setAdapter(adapter);
    }

    @Override
    public void onPointerStop(Choice choice) {
        Intent intent = new Intent(this, ChoiceSelectedActivity.class);
        intent.putExtra("choice", choice);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_add:
                startActivityForResult(new Intent(this, ChoiceAddActivity.class), 666);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            Choice choice = (Choice) data.getSerializableExtra("choice");
            if(choice != null){
                Choice old = isChoiceExist(choice.getId());
                if(old != null){
                    old.setDesc(choice.getDesc());
                    old.setWeight(choice.getWeight());
                } else {
                    choices.add(choice);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private Choice isChoiceExist(String id){
        for (Choice choice : choices) {
            if(choice.getId().equals(id)){
                return choice;
            }
        }
        return null;
    }

    @Override
    public void onChoiceContentClick(Choice choice) {
        Intent intent = new Intent(this, ChoiceAddActivity.class);
        intent.putExtra("choice", choice);
        startActivityForResult(intent, 666);
    }

    @Override
    public void onChoiceDeleteClick(int i) {
        ColorUtil.restore(choices.get(i).getColor());
        choices.remove(i);
        adapter.notifyDataSetChanged();
    }
}
