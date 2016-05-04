package com.dlclient.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpResponseException;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	final int ACTIVITY_CHOOSE_FILE = 1;
	final int ACTIVITY_CAMERA = 2;
	final Context context = this;
	private WebView webview;
	private String fileName;
	private SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = getSharedPreferences("config", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("NAMESPACE", "http://services.deeplearningserver.com/");
		editor.putString("METHOD", "batikDetection");
		editor.putString("URL", "http://192.168.43.248:8080/DeepLearningServer/services/batik_detection?wsdl");
		editor.commit();

		setContentView(R.layout.activity_main);
		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void performClick() throws Exception {
				// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.layout_config, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);

				final EditText namespaceEditText = (EditText) promptsView.findViewById(R.id.namespaceEditText);
				final EditText methodNameEditText = (EditText) promptsView.findViewById(R.id.methodNameEditText);
				final EditText urlEditText = (EditText) promptsView.findViewById(R.id.urlEditText);

				namespaceEditText.setText(sharedPreferences.getString("NAMESPACE", ""));
				methodNameEditText.setText(sharedPreferences.getString("METHOD", ""));
				urlEditText.setText(sharedPreferences.getString("URL", ""));
				
				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
										SharedPreferences.Editor editor = sharedPreferences.edit();
										editor.putString("NAMESPACE", namespaceEditText.getText().toString());
										editor.putString("METHOD", methodNameEditText.getText().toString());
										editor.putString("URL", urlEditText.getText().toString());
										editor.commit();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,	int id) {
										dialog.cancel();
									}
								});
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
			}
		}, "config");

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void performClick() throws Exception {
				// Toast.makeText(MainActivity.this, "Login clicked",
				// Toast.LENGTH_LONG).show();

				Intent chooseFile;
				Intent intent;
				chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
				chooseFile.setType("file/*");
				intent = Intent.createChooser(chooseFile, "Choose a file");
				startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
			}
		}, "login");

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void openCamera() throws Exception {
				Intent intent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				if (intent.resolveActivity(getPackageManager()) != null) {
					startActivityForResult(intent, ACTIVITY_CAMERA);
				}
			}
		}, "camera");

		webview.addJavascriptInterface(new Object() {
			@JavascriptInterface
			public void batikDetection() throws Exception {
				File imagefile = new File(fileName);
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(imagefile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Bitmap bm = BitmapFactory.decodeStream(fis);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				byte[] b = baos.toByteArray();
				getService(Base64.encode(b));
			}
		}, "detect");

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
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ACTIVITY_CHOOSE_FILE: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String filePath = uri.getPath();
				fileName = filePath;
				webview.loadUrl("javascript:setPathValue('" + filePath + "');");
			}
			break;
		}
		case ACTIVITY_CAMERA: {
			if (resultCode == RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				Uri tempUri = getImageUri(getApplicationContext(), photo);

				// Intent intentNew = data;
				// Uri uri = data.getData();
				String filePath = getRealPathFromURI(tempUri);
				fileName = filePath;
				webview.loadUrl("javascript:setPathValue('" + filePath + "');");
			}
			break;
		}
		}
	}

	public void getService(String imageData) {
		// String NAMESPACE = "http://services.deeplearningserver.com/";
		// String METHOD_NAME = "batikDetection";
		// String URL =
		// "http://192.168.43.248:8080/DeepLearningServer/services/batik_detection?wsdl";
		String NAMESPACE = sharedPreferences.getString("NAMESPACE", "");
		String METHOD_NAME = sharedPreferences.getString("METHOD", "");
		String URL = sharedPreferences.getString("URL", "");
		// String SOAP_ACTION =
		// "http://services.deeplearningserver.com/batik_detection";

		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		request.addProperty("arg0", imageData);
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.setOutputSoapObject(request);
		HttpTransportSE httpTransport = new HttpTransportSE(URL);
		// httpTransport.debug = true;

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

		Toast.makeText(MainActivity.this, result.toString(), Toast.LENGTH_LONG).show();
		Log.d("App", "" + result.toString());
	}

	public Uri getImageUri(Context inContext, Bitmap inImage) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		String path = Images.Media.insertImage(inContext.getContentResolver(),
				inImage, "Title", null);
		return Uri.parse(path);
	}

	public String getRealPathFromURI(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToFirst();
		int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
		return cursor.getString(idx);
	}
}
