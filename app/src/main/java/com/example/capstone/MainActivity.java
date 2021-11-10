package com.example.capstone;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpecColor = tabHost.newTabSpec("COLOR").setIndicator("색상");
        tabSpecColor.setContent(R.id.colorLayout);
        tabHost.addTab(tabSpecColor);

        TabHost.TabSpec tabSpecRota = tabHost.newTabSpec("ROTATION").setIndicator("회전");
        tabSpecRota.setContent(R.id.rotationLayout);
        tabHost.addTab(tabSpecRota);

        tabHost.setCurrentTab(0);
    }
}