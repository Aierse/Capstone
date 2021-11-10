package com.example.capstone;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabRadiation = tabHost.newTabSpec("RADIATION").setIndicator("방사능 측정");
        tabRadiation.setContent(R.id.radiation);
        tabHost.addTab(tabRadiation);

        TabHost.TabSpec tabFindLocation = tabHost.newTabSpec("LOCATION").setIndicator("위치 찾기");
        tabFindLocation.setContent(R.id.findLocation);
        tabHost.addTab(tabFindLocation);

        tabHost.setCurrentTab(0);
    }
}