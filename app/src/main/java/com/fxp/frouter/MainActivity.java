package com.fxp.frouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.fxp.frouter.annotation.FRouter;

@FRouter(group = "app", path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
