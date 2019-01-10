package com.juyoufuli.filmselectseat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String[] textDefault = {"标签", "标签", "标签", "标签", "标签", "标签", "标签", "标签", "标签", "撒上", "撒上"
            , "我是", "标签", "标签", "标签", "标签"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




//        initSelectSeatViewGroup();

    }

    private void initSelectSeatViewGroup() {
        SelectSeatViewGroup labelsView = findViewById(R.id.LabelsView);
        labelsView.setModel(Model.SELECT);
        labelsView.setTextList(Arrays.asList(textDefault));
        labelsView.setChildClickListener(new ChildClickListener() {
            @Override
            public void onChildClick(View view, Object object, int position) {
//                Toast.makeText(MainActivity.this, object.toString() + "第几个：" + position, Toast.LENGTH_SHORT).show();
            }
        });
        labelsView.setChildSelectListener(new ChildSelectListener() {
            @Override
            public void onChildSelect(List<String> stringList, int position) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = 0; i < stringList.size(); i++) {
                    stringBuffer.append(stringList.get(i));
                }
//                Toast.makeText(MainActivity.this, stringBuffer.toString() + "第几个：" + position, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onChildSelect(Object object, int position) {
//                Toast.makeText(MainActivity.this, object.toString() + "第几个：" + position, Toast.LENGTH_SHORT).show();
            }

        });
    }
}
