package com.example.capstone;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    //방사능 차트 전역변수
    float maximamRadiation = 10.0f;
    float radiation = 0;
    PieChart pieChart;
    int[] colorArray = new int[]{Color.RED, Color.LTGRAY};
    //지도 전역변수
    private FragmentManager fragmentManager;
    private MapFragment mapFragment;
    private GoogleMap map;

    double latitude;    //위도
    double longitude;      //경도
    //블루투스 전역변수
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치

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

        Button btn = findViewById(R.id.findbag);
        //차트 탭
        pieChart = findViewById(R.id.piechart);

        pieChart.setHoleRadius(60);
        pieChart.setCenterTextSize(25);
        pieChart.setTouchEnabled(false);
        pieChart.setDescription(null);
        pieChart.setDrawEntryLabels(false);

        ArrayList<PieEntry> data = new ArrayList<PieEntry>();
        data.add(new PieEntry(radiation, "방사능 수치"));
        data.add(new PieEntry(maximamRadiation - radiation, "최대 측정 가능치"));

        pieChart(data, radiation, false);
        //지도 탭
        fragmentManager = getFragmentManager();
        mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
        // 블루투스 활성화하기
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정

        if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
            selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
        } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
            // 블루투스를 활성화 하기 위한 다이얼로그 출력
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 선택한 값이 onActivityResult 함수에서 콜백된다.
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng l = new LatLng(latitude, longitude);

                map.clear();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(l, 15));

                MarkerOptions newMarker = new MarkerOptions();
                newMarker.title("가방 위치");
                newMarker.snippet("위도 : " + latitude + "     경도 : " + longitude);
                newMarker.position(l);
                map.addMarker(newMarker);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.disable();
        writeFile("gps.txt", latitude + "," + longitude);
    }

    private void writeFile(String fileName, String msg) {
        try {
            OutputStreamWriter oStreamWriter = new OutputStreamWriter(openFileOutput(fileName,
                    Context.MODE_PRIVATE));
            oStreamWriter.write(msg);
            oStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFile(String fileName) {
        String fileContents = "";
        try {
            InputStream iStream = openFileInput(fileName);
            if(iStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(iStream));
                String temp = "";

                StringBuffer sBuffer = new StringBuffer();
                while((temp = bufferedReader.readLine()) != null) {
                    sBuffer.append(temp);
                }
                iStream.close();
                fileContents = sBuffer.toString();
            }
        } catch (FileNotFoundException e) {
            latitude = 37.5642135;
            longitude = 127.0016985;
            writeFile("gps.txt", latitude + "," + longitude);

            return readFile("gps.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContents;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        String[] matrix = readFile("gps.txt").split(",");
        latitude = Double.parseDouble(matrix[0]);
        longitude = Double.parseDouble(matrix[1]);

        LatLng location = new LatLng(latitude, longitude);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("가방 위치");
        markerOptions.snippet("위도 : " + latitude + "     경도 : " + longitude);
        markerOptions.position(location);
        googleMap.addMarker(markerOptions);

        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
            selectBluetoothDevice();
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        int pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {

        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("취소");

            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    if (charSequences[which].toString()=="취소")
                        return;
                    connectDevice(charSequences[which].toString());
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for(BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if(deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        UUID uuid = java.util.UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 데이터 송,수신 스트림을 얻어옵니다.
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            // 데이터 수신 함수 호출
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveData() {
        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        int byteAvailable = inputStream.available();

                        if(byteAvailable > 0) {
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);
                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                            for(int i = 0; i < byteAvailable; i++) {
                                if( bytes[i] == '\n')
                                    break;

                                readBuffer[readBufferPosition++] =  bytes[i];
                            }

                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);

                            final String text = new String(encodedBytes, "US-ASCII");
                            readBufferPosition = 0;

                            String[] parsing = text.split(",");

                            radiation = Float.parseFloat(parsing[0]);
                            double newlatatude = Double.parseDouble(parsing[1]);
                            double newlongitude = Double.parseDouble(parsing[2]);

                            boolean measurable = true;
                            if (radiation == -1) {
                                radiation = 0;
                                measurable = false;
                            }
                            else if (radiation >= 10.0f)
                                maximamRadiation = radiation;
                            else
                                maximamRadiation = 10.0f;

                            ArrayList<PieEntry> data = new ArrayList<PieEntry>();
                            data.add(new PieEntry(radiation, "방사능 수치"));
                            data.add(new PieEntry(maximamRadiation - radiation, "최대 측정 가능치"));

                            pieChart(data, radiation, measurable);

                            if (newlatatude != 0 && newlongitude != 0) {
                                latitude = newlatatude + 0.04148995;
                                longitude = newlongitude + 0.35801279;
                            }
                        }

                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        workerThread.start();
    }

    private void pieChart(ArrayList<PieEntry> data, float radiation, boolean measurable)
    {
        PieDataSet pieDataSet = new PieDataSet(data, "");
        pieDataSet.setColors(colorArray);

        PieData pieData = new PieData(pieDataSet);

        pieData.setValueTextSize(0);

        pieChart.setData(pieData);
        if (measurable)
            pieChart.setCenterText(radiation + " μSv/" + maximamRadiation + " μSv");
        else
            pieChart.setCenterText("가이거카운터를 확인해주세요.");
        pieChart.invalidate();
    }
}