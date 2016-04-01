package com.dlclient.main;

import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class MainActivity extends Activity {

	final int ACTIVITY_CHOOSE_FILE = 1;
	private WebView webview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void performClick() throws Exception {
				// Toast.makeText(MainActivity.this, "Login clicked",
				// Toast.LENGTH_LONG).show();
				
//				Intent chooseFile;
//				Intent intent;
//				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//				chooseFile.setType("file/*");
//				intent = Intent.createChooser(chooseFile, "Choose a file");
//				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
				
				getService();
			}
		}, "login");
		webview.loadUrl("file:///android_asset/index.html");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_CHOOSE_FILE: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String filePath = uri.getPath();
				webview.loadUrl("javascript:setPathValue('" + filePath + "');");
			}
		}
		}
	}

	public void getService() {
		String NAMESPACE = "http://services.deeplearningserver.com/";
		String METHOD_NAME = "batikDetection";
		String URL = "http://192.168.43.248:8080/DeepLearningServer/services/batik_detection?wsdl";
		String SOAP_ACTION = "http://services.deeplearningserver.com/batik_detection";

		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE httpTransport = new HttpTransportSE(URL);
		//httpTransport.debug = true;

		try {
			httpTransport.call("", envelope);
		} catch (HttpResponseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // send request
		
		SoapPrimitive result = null;
		try {
			result = (SoapPrimitive) envelope.getResponse();
		} catch (SoapFault e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Log.d("App",""+result.toString());
	}
}
