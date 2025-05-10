package com.techathon.qrc_scan;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler {
	ZXingScannerView scannerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);
		scannerView = new ZXingScannerView(this);
		setContentView(scannerView);
	}

	@Override
	public void handleResult(Result result) {
	}
}
