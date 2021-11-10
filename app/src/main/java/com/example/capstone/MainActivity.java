package com.example.capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.*;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    float maximamRadiation = 100;
    int[] colorArray = new int[] {Color.RED, Color.LTGRAY};

    private FragmentManager fragmentManager;
    private MapFragment mapFragment;

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

        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment)fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

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
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        double latitude = 35.10637187150911;        //위도
        double longitude = 126.89515121598296;       //경도

        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("가방 위치");
        markerOptions.snippet("위도 : " + latitude + "     경도 : " +  longitude) ;
        markerOptions.position(location);
        googleMap.addMarker(markerOptions);

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
    }
}
