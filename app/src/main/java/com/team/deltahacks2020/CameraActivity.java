package com.team.deltahacks2020;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.*;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import java.io.*;
import java.util.*;

public class CameraActivity extends AppCompatActivity {
    private boolean cameraIsOpen = false;
    private CameraManager cameraManager;
    private int cameraFacing;
    private TextureView.SurfaceTextureListener surfaceTextureListener;
    private static final int CAMERA_REQUEST_CODE = 1;
    private Size previewSize;
    private String cameraId;
    private CameraDevice.StateCallback stateCallback;
    private Handler backgroundHandler;
    private CameraDevice cameraDevice;
    private HandlerThread backgroundThread;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private CameraCaptureSession cameraCaptureSession;
    private TextureView textureView;
    private CountDownTimer pictureTimer;

    private FirebaseVisionImageLabeler labeler;
    private FirebaseVisionFaceDetector detector;

    private String cameraKey;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private Bitmap lastImage;

    private boolean motionStatus;
    private boolean humanStatus;
    private int motionPictureCount;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    private static final List<String> humanIdentifiers = new ArrayList<String>() {
        {
            add("Arm");
            add("Shoulder");
            add("Sitting");
            add("Hand");
            add("Wrist");
            add("Muscle");
            add("Finger");
            add("Ear");
            add("Standing");
            add("Skin");
            add("Nose");
            add("Cheek");
            add("Neck");
            add("Eyelash");
        }
    };

    //for the log out method
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    /*static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        auth = FirebaseAuth.getInstance();
        cameraKey = readFromFile();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        db = FirebaseFirestore.getInstance();
        cameraIsOpen = false;
        motionStatus = false;
        humanStatus = false;
        motionPictureCount = 0;


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        /*FirebaseVisionFaceDetectorOptions highAccuracyOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();*/

        FirebaseVisionFaceDetectorOptions realTimeOpts =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(realTimeOpts);

        labeler = FirebaseVision.getInstance().getCloudImageLabeler();

        textureView = findViewById(R.id.texture_view);

        surfaceTextureListener = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                setUpCamera();
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                CameraActivity.this.cameraDevice = cameraDevice;
                if (!cameraIsOpen) {
                    cameraIsOpen = true;
                    createPreviewSession();
                }
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
                cameraIsOpen = false;
                CameraActivity.this.cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice cameraDevice, int error) {
                cameraDevice.close();
                cameraIsOpen = false;
                CameraActivity.this.cameraDevice = null;
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        openBackgroundThread();
        if (textureView != null && textureView.isAvailable()) {
            setUpCamera();
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
        if (pictureTimer != null) {
            pictureTimer.cancel();
        }
    }

    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void closeBackgroundThread() {
        if (backgroundHandler != null) {
            backgroundThread.quitSafely();
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    private void setUpCamera() {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics =
                        cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        cameraFacing) {
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraId = cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void createPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            ImageReader reader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            //captureRequestBuilder.addTarget(reader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            ImageReader.OnImageAvailableListener readerListener = reader1 -> {
                try (Image image = reader1.acquireLatestImage()) {
                    System.out.println(image);//TODO

                }
            };
            reader.setOnImageAvailableListener(readerListener, backgroundHandler);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if (cameraDevice == null) {
                                return;
                            }
                            try {
                                captureRequest = captureRequestBuilder.build();
                                CameraActivity.this.cameraCaptureSession = cameraCaptureSession;
                                CameraActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest,
                                        null, backgroundHandler);
                                startTakingPictures();
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startTakingPictures() {
        if (pictureTimer != null) {
            return;
        }
        pictureTimer = new CountDownTimer(9000000000000000000l, 1200) {
            @Override
            public void onTick(long millisUntilFinished) {
                Bitmap bitmap = textureView.getBitmap().copy(textureView.getBitmap().getConfig(), false);
                motionPictureCount++;
                //ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                //byte[] byteArray = stream.toByteArray();

                FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                        .setWidth(bitmap.getWidth())   // 480x360 is typically sufficient for
                        .setHeight(bitmap.getHeight())  // image recognition
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_YV12)
                        .setRotation(getRotationCompensation(cameraId, CameraActivity.this, CameraActivity.this))
                        .build();

                //FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromByteArray(byteArray, metadata);
                FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromBitmap(bitmap);

                analyzeImage(firebaseImage);
            }
            @Override
            public void onFinish() {
            }
        }.start();
    }

    private int getRotationCompensation(String cameraId, Activity activity, Context context) {
        try {
            // Get the device's current rotation relative to its "native" orientation.
            // Then, from the ORIENTATIONS table, look up the angle the image must be
            // rotated to compensate for the device's rotation.
            int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int rotationCompensation = ORIENTATIONS.get(deviceRotation);

            // On most devices, the sensor orientation is 90 degrees, but for some
            // devices it is 270 degrees. For devices with a sensor orientation of
            // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
            CameraManager cameraManager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
            int sensorOrientation = cameraManager
                    .getCameraCharacteristics(cameraId)
                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;

            // Return the corresponding FirebaseVisionImageMetadata rotation value.
            int result;
            switch (rotationCompensation) {
                case 0:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    break;
                case 90:
                    result = FirebaseVisionImageMetadata.ROTATION_90;
                    break;
                case 180:
                    result = FirebaseVisionImageMetadata.ROTATION_180;
                    break;
                case 270:
                    result = FirebaseVisionImageMetadata.ROTATION_270;
                    break;
                default:
                    result = FirebaseVisionImageMetadata.ROTATION_0;
                    Log.e("ERROR", "Bad rotation value: " + rotationCompensation);
            }
            return result;
        } catch (CameraAccessException e) {
            Log.e("ERROR", e.getMessage());
            return FirebaseVisionImageMetadata.ROTATION_0;
        }
    }

    private void analyzeImage(FirebaseVisionImage image) {
        /*labeler.processImage(image).addOnSuccessListener(detectedImages -> {
            detector.detectInImage(image).addOnSuccessListener(detectedFaces -> {
                System.out.println("AMOUNT: " + detectedImages.size());
                for (FirebaseVisionImageLabel vImageLabel : detectedImages) {
                    System.out.println("AMOUNT TYPE:" + vImageLabel.getText());
                }
                System.out.println("AMOUNT EYE: "+detectedFaces.size());
                for (FirebaseVisionFace vImageLabel : detectedFaces) {
                    System.out.println("AMOUNT TRACK:" + vImageLabel.getTrackingId());
                }*/
        if (lastImage == null) {
            lastImage = image.getBitmap();
        }
        if (lastImage.isRecycled()) {
            lastImage = image.getBitmap();
            return;

        }
        int rSum = 0, gSum = 0, bSum = 0;

        for (int i = 0; i < lastImage.getWidth(); i += 10) {
            for (int j = 0; j < lastImage.getHeight(); j += 10) {
                int lastPixel = lastImage.getPixel(i, j);
                int thisPixel = image.getBitmap().getPixel(i, j);
                rSum += Math.abs(Color.red(thisPixel) - Color.red(lastPixel));
                gSum += Math.abs(Color.green(thisPixel) - Color.green(lastPixel));
                bSum += Math.abs(Color.blue(thisPixel) - Color.blue(lastPixel));
            }
        }
        int sum = rSum + gSum + bSum;
        //Motion detected!
        if (sum > 800000) {
            System.out.println("AMOUNT MOTION TRUE");
            //if (motionStatus == false) {
                motionStatus = true;
                sendMotionAlert(true);
            //}
            if (/*motionPictureCount % 5 == 0*/ true) {
                labeler.processImage(image).addOnSuccessListener(detectedImages -> {
                    int allConfidenceHumanCount = 0;
                    int highConfidenceHumanCount = 0;
                    for (FirebaseVisionImageLabel vImageLabel : detectedImages) {
                        if (humanIdentifiers.contains(vImageLabel.getText())) {
                            allConfidenceHumanCount++;
                            if (vImageLabel.getConfidence() > 0.80f) {
                                highConfidenceHumanCount++;
                            }
                        }
                    }
                    if (allConfidenceHumanCount >= 1 /*|| highConfidenceHumanCount >= 2*/) {
                        //if (humanStatus == false) {
                            humanStatus = true;
                            sendHumanAlert(true);
                        //}
                    }
                    else if (allConfidenceHumanCount == 0) {
                        //if (humanStatus == true) {
                            humanStatus = false;
                            sendHumanAlert(false);
                        //}
                    }
                });
            }
        } else {
            System.out.println("AMOUNT MOTION FALSE");
            //if (motionStatus == true) {
                motionStatus = false;
                sendMotionAlert(false);
            //}
        }

        if (!lastImage.isRecycled()) {
            lastImage.recycle();
        }
        lastImage = image.getBitmap();
    }

    private void sendMotionAlert(boolean alertStatus) {
        db.collection("controller").document(auth.getCurrentUser().getEmail()).update("motionAlert-" + cameraKey, alertStatus).addOnCompleteListener((@NonNull Task<Void> task)->{
            if (task.isSuccessful()) {

            } else {
                if (task.getException() instanceof FirebaseFirestoreException) {
                    Map<String, Object> placeholder = new HashMap<>();
                    placeholder.put("place", "holder");
                    db.collection("controller").document(auth.getCurrentUser().getEmail()).set(placeholder);
                }
                System.out.println("ERROR: " + "Failed to update motionAlert");
                task.getException().printStackTrace();
            }
        });
    }
    private void sendHumanAlert(boolean alertStatus) {
        db.collection("controller").document(auth.getCurrentUser().getEmail()).update("humanAlert-" + cameraKey, alertStatus).addOnCompleteListener((@NonNull Task<Void> task)->{
            if (task.isSuccessful()) {

            } else {
                System.out.println("ERROR: " + "Failed to update motionAlert");
                task.getException().printStackTrace();
            }
        });
    }

    public void logOutClick(View view){
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent switchIntent = new Intent(this, MainActivity.class);
            closeCamera();
            startActivity(switchIntent);
            finish();
        });
    }
    private String readFromFile() {
        String ret = "";
        try {
            String fileName = auth.getCurrentUser().getEmail() + "_settings.txt";
            InputStream inputStream = openFileInput(fileName);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            System.out.println("login activity File not found: " + e.toString());
            try {
                String fileName = auth.getCurrentUser().getEmail() + "_settings.txt";
                FileOutputStream fileout = openFileOutput(fileName, MODE_PRIVATE);
                OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
                long time = System.currentTimeMillis();
                cameraKey = Long.toString(time);
                ret = cameraKey;
                outputWriter.write(Long.toString(time));
                outputWriter.close();

            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("login activity Can not read file: " + e.toString());
        }

        return ret;
    }
}
