package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    float maximamRadiation = 100;
    int[] colorArray = new int[] {Color.RED, Color.LTGRAY};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 탭 호스트 불러오기
        TabHost tabHost = findViewById(R.id.tabhost);
        tabHost.setup();
        // 1번 탭 연결
        TabHost.TabSpec tabRadiation = tabHost.newTabSpec("RADIATION").setIndicator("방사능 측정");
        tabRadiation.setContent(R.id.radiation);
        tabHost.addTab(tabRadiation);
        // 2번탭 연결
        TabHost.TabSpec tabFindLocation = tabHost.newTabSpec("LOCATION").setIndicator("위치 찾기");
        tabFindLocation.setContent(R.id.findLocation);
        tabHost.addTab(tabFindLocation);
        // 1번탭 선택
        tabHost.setCurrentTab(0);

        pieChart();
    }
    //디버깅 용 코드
    private void pieChart()
    {
        int radiation = 40;
        ArrayList<PieEntry> data = new ArrayList<PieEntry>();
        data.add(new PieEntry(radiation));
        data.add(new PieEntry(maximamRadiation - radiation));

        pieChart(data, radiation);
    }

    private void pieChart(ArrayList<PieEntry> data, float radiation)
    {
        PieChart pieChart = findViewById(R.id.piechart);

        PieDataSet pieDataSet = new PieDataSet(data, "방사능 수치");
        pieDataSet.setColors(colorArray);

        PieData pieData = new PieData(pieDataSet);

        pieData.setValueTextSize(0);

        pieChart.setHoleRadius(60);
        pieChart.setCenterText(radiation + "/" + maximamRadiation);
        pieChart.setCenterTextSize(25);



        pieChart.setData(pieData);
    }
}