package com.example.dhoomilbsheta.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.dhoomilbsheta.myapplication.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.splitinstall.SplitInstallHelper;
import com.google.android.play.core.splitinstall.SplitInstallManager;
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.splitinstall.SplitInstallSessionState;
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener;
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    private SplitInstallManager manager;
    private final String TAG = "DYNAMIC_TEST";
    private final String moduleName = "kotlin";
    private final String packageName = "com.example.dhoomilbsheta.myapplication";
    private final String dynamicFeatureActivity = "com.example.dhoomilbsheta.myapplication.ondemand.MainActivity";
    private ActivityMainBinding binding;

    FloatingActionButton showImageButton;
    Listener listener = new Listener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        showImageButton = binding.floatingActionButton;
        manager = SplitInstallManagerFactory.create(this);
        initializeViews();
    }

    @Override
    protected void onResume() {
        manager.registerListener(listener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        manager.unregisterListener(listener);
        super.onPause();
    }

    private void initializeViews() {
        if (manager.getInstalledModules().contains(moduleName)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName(packageName, dynamicFeatureActivity);
            if (getPackageManager().resolveActivity(intent, 0) == null) {
                Toast.makeText(this, "Waiting for installing module", Toast.LENGTH_SHORT).show();
            }
        }
        setupClickListener();
    }

    private void setupClickListener() {
        showImageButton.setOnClickListener(view -> loadAndLaunchModule(moduleName));
    }

    private void loadAndLaunchModule(String name) {
        Log.d(TAG, "Loading module " + name);
        if (manager.getInstalledModules().contains(name)) {
            Log.d(TAG, "Already installed");
            onSuccessfulLoad(true);
            return;
        }

        SplitInstallRequest request = SplitInstallRequest.newBuilder()
                .addModule(name)
                .build();

        manager.startInstall(request);
        Log.d(TAG, "Starting install for " + name);
    }

    private void onSuccessfulLoad(boolean launch) {
        if (launch) {
            try {
                launchActivity(dynamicFeatureActivity);
            } catch (Exception e) {
                StringWriter erros = new StringWriter();
                e.printStackTrace(new PrintWriter(erros));
                Log.d(TAG, erros.toString());
                Log.d(TAG, getBaseContext().getPackageCodePath());
            }
        }
    }

    private void launchActivity(String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName(getPackageName(), name);
        startActivity(intent);
    }

    class Listener implements SplitInstallStateUpdatedListener {

        @Override
        public void onStateUpdate(SplitInstallSessionState state) {
            for (String module : state.moduleNames()) {
                switch (state.status()) {
                    case SplitInstallSessionStatus.DOWNLOADING:
                        Log.d(TAG, "Downloading " + module);
                        break;
                    case SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION:
                        try {
                            startIntentSender(state.resolutionIntent().getIntentSender(), null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SplitInstallSessionStatus.INSTALLED:
                        if (26 <= Build.VERSION.SDK_INT) {
                            Log.d("DHOOMIL_TEST", String.valueOf(Build.VERSION.SDK_INT));
                            SplitInstallHelper.updateAppInfo(getApplicationContext());
                        }
                        break;
                    case SplitInstallSessionStatus.INSTALLING:
                        Log.d(TAG, "Downloading " + module);
                        break;
                    case SplitInstallSessionStatus.FAILED:
                        Log.e(TAG, "Error " + state.errorCode() + " for module " + module);
                        break;
                }
            }
        }
    }
}