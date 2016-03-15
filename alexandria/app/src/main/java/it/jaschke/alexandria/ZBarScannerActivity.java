package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;


import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ZBarScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    public static final String RESULT_STR = "RESULT_STR";
    private List<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.EAN13);
    private Toast mToast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZBarScannerView(this);
        mScannerView.setFormats(formats);
        setContentView(mScannerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result onResult) {
        final String result_barcodeType = onResult.getBarcodeFormat().getName();
            final String TAG = "Result";
            Log.v(TAG, onResult.getContents() + " " + result_barcodeType);

            //returning intent package and finish Activity here
            Intent resultIntent = new Intent();
            resultIntent.putExtra(RESULT_STR, onResult.getContents());
            setResult(RESULT_OK, resultIntent);
            finish();

        mScannerView.resumeCameraPreview(this);
    }
}
