package com.benmohammad.vidz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.benmohammad.vidz.trimmer.VidzHgLVideoTrimmer;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnHgLVideoListener;
import com.benmohammad.vidz.trimmer.interfaces.VidzOnTrimVideoListener;

public class VidzTrimmerActivity extends AppCompatActivity implements VidzOnTrimVideoListener, VidzOnHgLVideoListener {

    private VidzHgLVideoTrimmer vidzTrimmer;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vidz_activity_trimmer);

        Intent extraIntent = getIntent();
        String path = "";
        int maxDuration = 10;
        if(extraIntent != null) {
            path = extraIntent.getStringExtra("VideoPath");
            maxDuration = extraIntent.getIntExtra("VideoDuration" , 10);
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(getString(R.string.trimming_progress));

        vidzTrimmer = findViewById(R.id.timeLine);
        if(vidzTrimmer != null) {
            Log.e("tg", "maxDuration= " + maxDuration);
            vidzTrimmer.setMaxDuration(maxDuration);
            vidzTrimmer.setOnTrimVideoListener(this);
            vidzTrimmer.setOnHgLVideoListener(this);
            vidzTrimmer.setVideoUri(Uri.parse(path));
            vidzTrimmer.setVideoInformationVisibility(true);
        }
    }

    @Override
    public void onVideoPrepared() {
        runOnUiThread(() -> {
            Toast.makeText(this, "OnVideoPrepared", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onTrimStarted(int startPosition, int endPosition) {
        Intent intent = new Intent();
        intent.putExtra("startPosition", startPosition);
        intent.putExtra("endPosition", endPosition);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void getResult(Uri uri) {
        mProgressDialog.cancel();
    }

    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        vidzTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(String message) {
        mProgressDialog.cancel();

        runOnUiThread(() -> {
            Toast.makeText(this, "OnError", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("tg", "resultCode = " + resultCode + " data = " + data);
    }
}
