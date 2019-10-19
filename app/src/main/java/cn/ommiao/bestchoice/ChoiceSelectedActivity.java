package cn.ommiao.bestchoice;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class ChoiceSelectedActivity extends Activity {

    private Choice choice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_selelcted);
        choice = (Choice) getIntent().getSerializableExtra("choice");
        if(choice == null){
            finish();
            return;
        }
        initWindow();
        initViews();
    }

    private void initViews() {
        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(view -> {
            finish();
        });
        ImageView ivColor = findViewById(R.id.iv_color);
        ivColor.setColorFilter(Color.parseColor(choice.getColor()));
        TextView tvResult = findViewById(R.id.tv_result);
        tvResult.setTextColor(Color.parseColor(choice.getColor()));
        tvResult.setText(choice.getDesc());
    }

    private void initWindow(){
        Window window = getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = screenWidth - getResources().getDimensionPixelSize(R.dimen.dialog_margin) * 2;
        params.height = screenHeight;
        window.setAttributes(params);
    }
}
