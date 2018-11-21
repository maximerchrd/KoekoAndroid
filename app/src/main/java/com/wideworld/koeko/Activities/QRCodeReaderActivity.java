package com.wideworld.koeko.Activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.wideworld.koeko.Activities.ActivityTools.OnProgressChanged;
import com.wideworld.koeko.Koeko;
import com.wideworld.koeko.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.error.CameraErrorListener;
import io.fotoapparat.exception.camera.CameraException;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;
import io.fotoapparat.result.BitmapPhoto;
import io.fotoapparat.result.PhotoResult;
import io.fotoapparat.result.WhenDoneListener;
import io.fotoapparat.view.CameraView;
import io.fotoapparat.view.FocusView;

import static io.fotoapparat.log.LoggersKt.fileLogger;
import static io.fotoapparat.log.LoggersKt.logcat;
import static io.fotoapparat.log.LoggersKt.loggers;
import static io.fotoapparat.result.transformer.ResolutionTransformersKt.scaled;
import static io.fotoapparat.selector.LensPositionSelectorsKt.back;

public class QRCodeReaderActivity extends AppCompatActivity {

    private static final String LOGGING_TAG = "Fotoapparat";
    //private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private CameraView cameraView;
    private FocusView focusView;
    private View capture;
    private static final String TAG = "QRCodeReader";

    private Fotoapparat fotoapparat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qrcodereader);

        cameraView = findViewById(R.id.cameraView);
        focusView = findViewById(R.id.focusView);
        capture = findViewById(R.id.capture);
        cameraView.setVisibility(View.VISIBLE);

        fotoapparat = createFotoapparat();
        takePictureOnClick();
        zoomSeekBar();
    }

    private Fotoapparat createFotoapparat() {
        return Fotoapparat
                .with(this)
                .into(cameraView)
                .focusView(focusView)
                .previewScaleType(ScaleType.CenterCrop)
                .lensPosition(back())
                .frameProcessor(new SampleFrameProcessor())
                .logger(loggers(
                        logcat(),
                        fileLogger(this)
                ))
                .cameraErrorCallback(new CameraErrorListener() {
                    @Override
                    public void onError(@NotNull CameraException e) {
                        Toast.makeText(QRCodeReaderActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .build();
    }

    private void zoomSeekBar() {
        SeekBar seekBar = findViewById(R.id.zoomSeekBar);

        seekBar.setOnSeekBarChangeListener(new OnProgressChanged() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fotoapparat.setZoom(progress / (float) seekBar.getMax());
            }
        });
    }

    private void takePictureOnClick() {
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void takePicture() {
        PhotoResult photoResult = fotoapparat.takePicture();
        photoResult.saveToFile(new File(
                getExternalFilesDir("photos"),
                "photo.jpg"
        ));

        photoResult
                .toBitmap(scaled(0.25f))
                .whenDone(new WhenDoneListener<BitmapPhoto>() {
                    @Override
                    public void whenDone(@Nullable BitmapPhoto bitmapPhoto) {
                        if (bitmapPhoto == null) {
                            Log.e(LOGGING_TAG, "Couldn't capture photo.");
                            return;
                        }

                        Bitmap bitmap = bitmapPhoto.bitmap;
                        FirebaseVisionBarcodeDetectorOptions options =
                                new FirebaseVisionBarcodeDetectorOptions.Builder()
                                        .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
                                        .build();
                        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                                .getVisionBarcodeDetector(options);

                        Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(FirebaseVisionImage.fromBitmap(bitmap))
                                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                                        String code = getQuestionCode(barcodes);
                                        if (code.length() == 0) {
                                            Toast.makeText(QRCodeReaderActivity.this,
                                                    "No question detected. Please try again.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.d(TAG, "onSuccess: " + code);
                                            Koeko.qrCode = code;
                                            finish();
                                            invalidateOptionsMenu();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });

                    }
                });
    }

    static private String getQuestionCode(List<FirebaseVisionBarcode> barcodes) {
        for (FirebaseVisionBarcode barcode: barcodes) {
            String rawValue = barcode.getRawValue();
            return rawValue;
        }
        return "";
    }

    @Override
    protected void onStart() {
        super.onStart();
        fotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        fotoapparat.stop();
    }

    private class SampleFrameProcessor implements FrameProcessor {
        @Override
        public void process(@NotNull Frame frame) {
            // Perform frame processing, if needed
        }
    }

    /**
     * method used to know if we send a disconnection signal to the server
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            Log.v("QRCodeReader: ", "focus lost");
            ((Koeko) this.getApplication()).startActivityTransitionTimer();
            ((Koeko) this.getApplication()).MAX_ACTIVITY_TRANSITION_TIME_MS = 1400;
        } else {
            ((Koeko) this.getApplication()).stopActivityTransitionTimer();
            ((Koeko) this.getApplication()).MAX_ACTIVITY_TRANSITION_TIME_MS = 700;
            Log.v("QRCodeReader: ", "has focus");
        }
    }
}
