package com.fxp.frouter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.fxp.frouter.annotation.FRouter;
import com.fxp.frouter.annotation.Param;

@FRouter(group = "app", path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Param
    String userId;

    @Param(name = "age")
    int userAge;

    @Param(name = "isHappy")
    boolean isHappy = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
