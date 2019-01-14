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

    private int[][] seatList = new int[6][];
    private SelectSeatView searchSeat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchSeat = findViewById(R.id.search_seat);

//外层数组
        for (int i = 0; i < 6; i++) {
            int[] indes = new int[10];
            for (int x = 0; x < 10; x++) {
                if (i == 0) {
                    if (x < 2 || x > 6) {
                        indes[x] = 0;
                    } else if (x == 5) {
                        indes[x] = 2;
                    } else {
                        indes[x] = 1;
                    }
                } else {
                    indes[x] = 1;
                }
            }
            seatList[i] = indes;
        }
        searchSeat.setSeatList(seatList);

    }
}
