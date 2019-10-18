package cn.ommiao.bestchoice;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ChoiceAddActivity extends Activity {

    private Choice choice;

    private ImageView ivColor;
    private EditText etDesc, etWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_add);
        choice = (Choice) getIntent().getSerializableExtra("choice");
        initWindow();
        initViews();
    }

    private void initViews() {
        ImageView ivClose = findViewById(R.id.iv_close);
        ivClose.setOnClickListener(view -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        ivColor = findViewById(R.id.iv_color);
        etDesc = findViewById(R.id.et_desc);
        etWeight = findViewById(R.id.et_weight);
        if(choice != null){
            etDesc.setText(choice.getDesc());
            etWeight.setText(String.valueOf(choice.getWeight()));
        } else {
            choice = new Choice();
            choice.setColor(ColorUtil.getOneColor());
        }
        ivColor.setColorFilter(Color.parseColor(choice.getColor()));
        TextView tvConfirm = findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(view -> {
            if(isDataCheck()){
                Intent result = new Intent();
                result.putExtra("choice", choice);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    private boolean isDataCheck() {
        String desc = etDesc.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        if(TextUtils.isEmpty(desc)){
            Toast.makeText(this, "请输入选项描述", Toast.LENGTH_SHORT).show();
            return false;
        } else if(TextUtils.isEmpty(weight) || Integer.parseInt(weight) <= 0){
            Toast.makeText(this, "请输入有效的权重，取值范围为1-1000", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
