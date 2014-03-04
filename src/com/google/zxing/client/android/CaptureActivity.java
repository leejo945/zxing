package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

	public static final int HISTORY_REQUEST_CODE = 0x0000bacc;

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	// private TextView statusView;
	private View resultView;
	private Result lastResult;
	private boolean hasSurface;

	private IntentSource source;

	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.capture);

	}

	@Override
	protected void onResume() {
		super.onResume();

		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		resultView = findViewById(R.id.result_view);

		handler = null;
		lastResult = null;

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {

			initCamera(surfaceHolder);
		} else {

			surfaceHolder.addCallback(this);
		}
		source = IntentSource.NONE;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		// inactivityTimer.onPause();
		// ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// inactivityTimer.shutdown();
		super.onDestroy();
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {

		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		// inactivityTimer.onActivity();
		lastResult = rawResult;
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
				this, rawResult);
		Toast.makeText(this, "999999999", 0).show();
		boolean fromLiveScan = barcode != null;
		switch (source) {
		case NONE:
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			if (fromLiveScan
					&& prefs.getBoolean(PreferencesActivity.KEY_BULK_MODE,
							false)) {
				// Toast.makeText(getApplicationContext(),
				// getResources().getString(R.string.msg_bulk_mode_scanned) +
				// " (" + rawResult.getText() + ')',
				// Toast.LENGTH_SHORT).show();
				// // Wait a moment or else it will scan the same barcode
				// continuously about 3 times
				restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
			} else {
				handleDecodeInternally(rawResult, resultHandler, barcode);
			}
			break;

		default:
			Log.e("qr", "ÆäËûµÄ¡£¡£¡£¡£");
			break;
		}

	}

	private void handleDecodeInternally(Result rawResult,
			ResultHandler resultHandler, Bitmap barcode) {
		// // statusView.setVisibility(View.GONE);
		viewfinderView.setVisibility(View.GONE);
		resultView.setVisibility(View.VISIBLE);
		//
		// ImageView barcodeImageView = (ImageView)
		// findViewById(R.id.barcode_image_view);
		// if (barcode == null) {
		// barcodeImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
		// R.drawable.launcher_icon));
		// } else {
		// barcodeImageView.setImageBitmap(barcode);
		// }
		//
		// TextView formatTextView = (TextView)
		// findViewById(R.id.format_text_view);
		// formatTextView.setText(rawResult.getBarcodeFormat().toString());
		//
		// TextView typeTextView = (TextView) findViewById(R.id.type_text_view);
		// typeTextView.setText(resultHandler.getType().toString());
		//
		// DateFormat formatter =
		// DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
		// TextView timeTextView = (TextView) findViewById(R.id.time_text_view);
		// timeTextView.setText(formatter.format(new
		// Date(rawResult.getTimestamp())));
		//
		//
		// TextView metaTextView = (TextView) findViewById(R.id.meta_text_view);
		// View metaTextViewLabel = findViewById(R.id.meta_text_view_label);
		// metaTextView.setVisibility(View.GONE);
		// metaTextViewLabel.setVisibility(View.GONE);
		// Map<ResultMetadataType,Object> metadata =
		// rawResult.getResultMetadata();
		// if (metadata != null) {
		// StringBuilder metadataText = new StringBuilder(20);
		// for (Map.Entry<ResultMetadataType,Object> entry :
		// metadata.entrySet()) {
		// if (DISPLAYABLE_METADATA_TYPES.contains(entry.getKey())) {
		// metadataText.append(entry.getValue()).append('\n');
		// }
		// }
		// if (metadataText.length() > 0) {
		// metadataText.setLength(metadataText.length() - 1);
		// metaTextView.setText(metadataText);
		// metaTextView.setVisibility(View.VISIBLE);
		// metaTextViewLabel.setVisibility(View.VISIBLE);
		// }
		// }
		//
		// TextView contentsTextView = (TextView)
		// findViewById(R.id.contents_text_view);
		// CharSequence displayContents = resultHandler.getDisplayContents();
		// contentsTextView.setText(displayContents);
		// // Crudely scale betweeen 22 and 32 -- bigger font for shorter text
		// int scaledSize = Math.max(22, 32 - displayContents.length() / 4);
		// contentsTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSize);
		//
		// TextView supplementTextView = (TextView)
		// findViewById(R.id.contents_supplement_text_view);
		// supplementTextView.setText("");
		// supplementTextView.setOnClickListener(null);
		// if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
		// PreferencesActivity.KEY_SUPPLEMENTAL, true)) {
		// SupplementalInfoRetriever.maybeInvokeRetrieval(supplementTextView,
		// resultHandler.getResult(),
		// historyManager,
		// this);
		// }
		//
		// int buttonCount = resultHandler.getButtonCount();
		// // ViewGroup buttonView = (ViewGroup)
		// findViewById(R.id.result_button_view);
		// // buttonView.requestFocus();
		// for (int x = 0; x < ResultHandler.MAX_BUTTON_COUNT; x++) {
		// // TextView button = (TextView) buttonView.getChildAt(x);
		// if (x < buttonCount) {
		// /// button.setVisibility(View.VISIBLE);
		// // button.setText(resultHandler.getButtonText(x));
		// // button.setOnClickListener(new ResultButtonListener(resultHandler,
		// x));
		// } else {
		// // button.setVisibility(View.GONE);
		// }
		// }
		//
		// if (copyToClipboard && !resultHandler.areContentsSecure()) {
		// ClipboardInterface.setText(displayContents, this);
		// }
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);

			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			// displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			// displayFrameworkBugMessageAndExit();
		}
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		resultView.setVisibility(View.GONE);
		// statusView.setText(R.string.msg_default_status);
		// statusView.setVisibility(View.VISIBLE);
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
}
