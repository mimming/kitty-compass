package com.google.android.glass.sample.kittycompass;

import android.app.Activity;
import android.content.Intent;

public class StartCompassActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, CompassService.class));
        finish();
    }
}
