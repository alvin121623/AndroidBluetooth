package application.aku.androidbluetooth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    Button btnon, btnoff, btnpaired, btnscan;
    ListView lvbluetooth;
    TextView tvtype;

    BluetoothAdapter bluetoothAdapter;

    ActivityResultLauncher<Intent> activityResultLauncher;

    ArrayAdapter<String> adapterBluetooth;
    ArrayList<String> listBluetooth = new ArrayList<>();
    ArrayList<BluetoothDevice> listBluetoothDevice = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnon = findViewById(R.id.btnon);
        btnoff = findViewById(R.id.btnoff);
        btnpaired = findViewById(R.id.btnpaired);
        btnscan = findViewById(R.id.btnscan);
        tvtype = findViewById(R.id.tvtype);
        lvbluetooth = findViewById(R.id.lvbluetooth);

        tvtype.setText("1024 = AUDIO_VIDEO, 256 = COMPUTER, 2304 = HEALTH, 1536 = IMAGING, 0 = MISC, 768 = NETWORKING, 1280 = PERIPHERAL, 512 = PHONE, 2048 = TOY, 7936 = UNCATEGORIZED, 1792 = WEARABLE");

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        adapterBluetooth = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listBluetooth);
        lvbluetooth.setAdapter(adapterBluetooth);
        lvbluetooth.setOnItemClickListener((parent, view, position, id) -> {
            if (listBluetoothDevice.get(position).getBondState() == 10){
                pairDevice(listBluetoothDevice.get(position));
            }else if (listBluetoothDevice.get(position).getBondState() == 12){
                unpairDevice(listBluetoothDevice.get(position));
            }
        });

        if (bluetoothAdapter.isEnabled()){
            Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
            listBluetoothDevice.clear();
            listBluetooth.clear();

            if (bt.size()>0){
                for (BluetoothDevice device:bt){
                    listBluetoothDevice.add(device);
                    listBluetooth.add(device.getName()+" ("+device.getBluetoothClass().getMajorDeviceClass()+") "+device.getBondState()+"\n"+device.getAddress());
                }
            }else {
                Toast.makeText(MainActivity.this, "Bluetooth Device Not Found", Toast.LENGTH_SHORT).show();
            }

            adapterBluetooth.notifyDataSetChanged();
        }

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(MainActivity.this, "Bluetooth On", Toast.LENGTH_SHORT).show();

                        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                        listBluetoothDevice.clear();
                        listBluetooth.clear();
                        if (bt.size()>0){
                            for (BluetoothDevice device:bt){
                                listBluetoothDevice.add(device);
                                listBluetooth.add(device.getName()+" ("+device.getBluetoothClass().getMajorDeviceClass()+") "+device.getBondState()+"\n"+device.getAddress());
                            }
                        }else {
                            Toast.makeText(MainActivity.this, "Bluetooth Device Not Found", Toast.LENGTH_SHORT).show();
                        }
                        adapterBluetooth.notifyDataSetChanged();
                    }else if (result.getResultCode() == Activity.RESULT_CANCELED){
                        Toast.makeText(MainActivity.this, "Bluetooth On Canceled", Toast.LENGTH_SHORT).show();
                    }
                });

        btnon.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 31) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 77);
                }else{
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 78);
                    }else{
                        if (bluetoothAdapter == null){
                            Toast.makeText(MainActivity.this, "Bluetooth not support on this device", Toast.LENGTH_SHORT).show();
                        }else {
                            if (!bluetoothAdapter.isEnabled()){
                                Intent intentEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                activityResultLauncher.launch(intentEnable);
                            }
                        }
                    }
                }
            }else {
                if (bluetoothAdapter == null){
                    Toast.makeText(MainActivity.this, "Bluetooth not support on this device", Toast.LENGTH_SHORT).show();
                }else {
                    if (!bluetoothAdapter.isEnabled()){
                        Intent intentEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        activityResultLauncher.launch(intentEnable);
                    }
                }
            }
        });

        btnoff.setOnClickListener(v -> {
            if (bluetoothAdapter.isEnabled()){
                bluetoothAdapter.disable();
                Toast.makeText(MainActivity.this, "Bluetooth Off Success", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Bluetooth Off", Toast.LENGTH_SHORT).show();
            }
            listBluetoothDevice.clear();
            listBluetooth.clear();
            adapterBluetooth.notifyDataSetChanged();
        });

        btnpaired.setOnClickListener(v -> {
            if (bluetoothAdapter.isEnabled()){
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                listBluetoothDevice.clear();
                listBluetooth.clear();

                if (bt.size()>0){
                    for (BluetoothDevice device:bt){
                        listBluetoothDevice.add(device);
                        listBluetooth.add(device.getName()+" ("+device.getBluetoothClass().getMajorDeviceClass()+") "+device.getBondState()+"\n"+device.getAddress());
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Bluetooth Device Not Found", Toast.LENGTH_SHORT).show();
                }

                adapterBluetooth.notifyDataSetChanged();
            }else {
                Toast.makeText(MainActivity.this, "Bluetooth Off", Toast.LENGTH_SHORT).show();
            }
        });

        btnscan.setOnClickListener(v -> {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                2);
                    }else {
                        if (bluetoothAdapter.isEnabled()){
                            listBluetoothDevice.clear();
                            listBluetooth.clear();
                            adapterBluetooth.notifyDataSetChanged();
                            bluetoothAdapter.startDiscovery();
                        }else {
                            Toast.makeText(MainActivity.this, "Bluetooth Off", Toast.LENGTH_SHORT).show();
                        }
                    }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiverScan, intentFilter);

        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiverPair, intent);

    }

    BroadcastReceiver receiverScan = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (!listBluetoothDevice.contains(device) && !String.valueOf(device.getName()).equals("null")){
                    listBluetoothDevice.add(device);
                    listBluetooth.add(device.getName()+" ("+device.getBluetoothClass().getMajorDeviceClass()+") "+device.getBondState()+"\n"+device.getAddress());
                    adapterBluetooth.notifyDataSetChanged();
                }
            }
        }
    };

    BroadcastReceiver receiverPair = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    Toast.makeText(MainActivity.this, "Paired", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
                    Toast.makeText(MainActivity.this, "Unpaired", Toast.LENGTH_SHORT).show();
                }


            }
        }
    };

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
