// If you are ever feeling angry or down, just remember fricatta. - Albert

package jcdr23.com.dev3d;

import org.opencv.features2d.BFMatcher;
import org.opencv.features2d.Params;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import java.lang.String;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Timer;
import org.opencv.calib3d.*;
import java.util.TimerTask;
import java.util.stream.*;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import java.lang.Exception;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import 	java.util.Arrays;
import android.util.Pair;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture.AsynchronousCompletionTask;

import android.util.Log;
import android.hardware.Camera;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.MenuItem;
import android.app.Dialog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FlannBasedMatcher;
import org.opencv.xfeatures2d.SURF;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.features2d.ORB;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.util.List;
import java.util.ArrayList;
import org.opencv.core.DMatch;
import org.opencv.core.MatOfDMatch;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*System.loadLibrary("main");
        System.loadLibrary("SFM");*/

        // OpenCV check
        if (!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
            Toast.makeText(MainActivity.this, "Unable to load OpenCV", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("SUCCESS", "OpenCV loaded");
            Toast.makeText(MainActivity.this,"OpenCV loaded",Toast.LENGTH_SHORT).show();
        }

        // SELECT IMAGES
        Button selectImages = findViewById(R.id.btn_selectImages);
        selectImages.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                DialogProperties properties = new DialogProperties();

                properties.selection_mode = DialogConfigs.MULTI_MODE;
                properties.selection_type = DialogConfigs.FILE_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;

                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this,properties);
                dialog.setTitle("Select your images");

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        //files is the array of the paths of files selected by the Application User.
                        if(files.length >= 2){
                            double time = 0;
                            Timer timer = new Timer();
                            TextView log = findViewById(R.id.txt_log);
                            TimerTask t = new TimerTask() {
                                double time = 0;
                                public void run() {
                                    time++;
                                    Log.i("fricatta", Double.toString(time / 1000));
                                }
                            };
                            timer.scheduleAtFixedRate(t, 1, 1);

                            //MatOfDMatch[] FLANNMATCHResult =
                            List<MatOfDMatch> kpResults = kpDetect(files);
                            log.setText(kpResults.get(0).size().toString() + " | " + kpResults.get(1).size().toString());

                            Mat retVal = new Mat(findEssentialMat_1(kpResults.get(0).nativeObj, kpResults.get(1).nativeObj, 2563.7013, pp.x, pp.y, method, prob, threshold));
                            // Log.i("gonzaga", (FLANNMATCHResult).toString());

                            /*CompletableFuture CFkpDetect = CompletableFuture.supplyAsync(() -> kpDetect(files));
                            try {
                                MatOfDMatch FLANNMATCHResult = FLANNMATCH(CFkpDetect.get());
                            } catch (InterruptedException e){
                                // TODO: Error Code: 001
                                Toast.makeText(MainActivity.this,"Error Code: 001",Toast.LENGTH_SHORT).show();
                                Log.e("fricatta", "Error Code: 001");
                                while(true){ log.setText("Errpr Code 001"); }
                            } catch (ExecutionException e){
                                // TODO: Error Code: 002
                                Toast.makeText(MainActivity.this,"Error Code: 002",Toast.LENGTH_SHORT).show();
                                Log.e("fricatta", "Error Code: 002");
                                while(true){ log.setText("Errpr Code 002"); }
                            }*/
                        } else {
                            Toast.makeText(MainActivity.this,"Please select at least two images.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.show();
                Log.i("fricatta", "btn_selectImages clicked!");
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode,@NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted.
                    Toast.makeText(MainActivity.this,"Thank you. Please continue.",Toast.LENGTH_SHORT).show();
                }
                else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this,"We need permission to access your dataset.",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public MatOfDMatch[] newPC(String[] files){
        MatOfDMatch[] results = new MatOfDMatch[500];
        for (int i = 0; i < files.length; i++) {
            Mat img1 = Imgcodecs.imread(files[i], Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Mat img2 = Imgcodecs.imread(files[i+1], Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Size size1 = new Size(img1.width()*0.5, img1.height()*0.5);
            Imgproc.resize(img1, img1, size1);
            Size size2 = new Size(img2.width()*0.5, img2.height()*0.5);
            Imgproc.resize(img2, img2, size2);
            FastFeatureDetector detector = FastFeatureDetector.create();
            DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

// DETECTION
// first image
            Mat descriptors1 = new Mat();
            MatOfKeyPoint keypoints1 = new MatOfKeyPoint();

            detector.detect(img1, keypoints1);
            descriptor.compute(img1, keypoints1, descriptors1);

// second image
            Mat descriptors2 = new Mat();
            MatOfKeyPoint keypoints2 = new MatOfKeyPoint();

            detector.detect(img2, keypoints2);
            descriptor.compute(img2, keypoints2, descriptors2);

// MATCHING
// match these two keypoints sets
            List<MatOfDMatch> matches = new ArrayList<MatOfDMatch>();
            /*//-- Filter matches using the Lowe's ratio test
            float ratioThresh = 0.7f;
            List<DMatch> listOfGoodMatches = new ArrayList<>();
            for (int j = 0; j < knnMatches.size(); i++) {
                if (knnMatches.get(j).rows() > 1) {
                    DMatch[] matches = knnMatches.get(j).toArray();
                    if (matches[0].distance < ratioThresh * matches[1].distance) {
                        listOfGoodMatches.add(matches[0]);
                    }
                }
            }
            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(listOfGoodMatches);

            results[i] = goodMatches;*/
        }
        return results;

    }

    public List<MatOfDMatch> kpDetect(String[] files) {
        Mat[] desResult = new Mat[999];
        MatOfKeyPoint[] kpResult = new MatOfKeyPoint[999];
        for (int i = 0; i < files.length; i++) {
            Log.i("fricatta", "Alfred");
            TextView log = findViewById(R.id.txt_log);
            //try {
            log.setText("Setup is successful. Continuing.");
            String userLog = "";
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            Mat des = new Mat();
            ORB orb = ORB.create(10000, 1.4f, 11, 29, 0, 2, 0, 29);
            //for(int i = 0; i < files.length; i++) {
            Mat img = Imgcodecs.imread(files[i]);
            Size size = new Size(img.width()*0.5, img.height()*0.5);
            Imgproc.resize(img, img, size);
            orb.detect(img, keypoints);
            orb.compute(img, keypoints, des);
            Log.i("fricatta", Double.toString((des.size().width) * (des.size().height)));
            // userLog += Double.toString((des.size().width)) + " | " + Double.toString(des.size().height);
            //}
            // log.setText(userLog);
            Log.i("gonzaga", des.toString());
            desResult[i] = des;
            kpResult[i] = keypoints;
        }

        /*for (int k = 0; k < desResult.length; k++) {
            if(desResult[0].type() != CvType.CV_32F) {
                desResult[0].convertTo(desResult[0], CvType.CV_32F);
            }
        }*/

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
        Log.i("gonzaga", desResult[0].toString());
        Log.i("gonzaga", desResult[1].toString());
        MatOfDMatch[] array = new MatOfDMatch[999];
        for(int a = 0; a < array.length; a++){
            array[a] = new MatOfDMatch();
        }
        List<MatOfDMatch> matches = Arrays.asList(array);
        MatOfDMatch filteredMatches = new MatOfDMatch();
        Log.i("gonzaga", Integer.toString(desResult.length));
        for (int q = 0; desResult[q+1] != null; q++) {
            Log.i("gonzaga", Integer.toString(desResult.length));
            /*if(desResult[q+1] == null){
                break;
            }*/
            matcher.match(desResult[q], desResult[q+1], matches.get(q));
        }

        List<MatOfDMatch> allGoodMatches = Arrays.asList(array);

        float ratioThresh = 0.7f;
        List<DMatch> listOfGoodMatches = new ArrayList<>();
        MatOfDMatch goodMatches = null;
        for (int w = 0; w < files.length; w++) {
            for (int i = 0; i < files.length; i++) {
                Log.i("gonzaga", matches.get(i).toString());
                Log.i("gonzaga", Integer.toString(i));
                if (matches.get(i).rows() > 1) {
                    DMatch[] fMatches = matches.get(i).toArray();
                    if (fMatches[i].distance < ratioThresh * fMatches[i+1].distance) {
                        listOfGoodMatches.add(fMatches[i]);
                    }
                }
            }
            goodMatches = new MatOfDMatch();
            goodMatches.fromList(listOfGoodMatches);
            allGoodMatches.set(w, goodMatches);
        }
        return allGoodMatches;
        //} catch (Exception e){
        //    log.setText("An error has occurred");
        //    return null;
        //}
    }
        public List<MatOfDMatch> FLANNMATCH(Object[] h) {
            // @Nullable
            TextView log = findViewById(R.id.txt_log);
            MatOfDMatch[] allGoodMatches = new MatOfDMatch[999];
            Mat[] des = Mat[].class.cast(h[0]);
            MatOfKeyPoint[] kp = MatOfKeyPoint[].class.cast(h[1]);
            for (int k = 0; k < des.length; k++) {
                if(des[0].type() != CvType.CV_32F) {
                    des[0].convertTo(des[0], CvType.CV_32F);
                }
            }
            Mat descriptors1 = new Mat();
            Mat descriptors2 = new Mat();
            /*//try {
                for (int j = 0; j < des.length; j++) {
                    //-- Step 2: Matching descriptor vectors with a FLANN based matcher
                    // Since SURF is a floating-point descriptor NORM_L2 is used
                    */
                    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
                    List<MatOfDMatch> knnMatches = new ArrayList<>();
                    matcher.knnMatch(des[0], des[1], knnMatches, 5);
                    /*
                    //-- Filter matches using the Lowe's ratio test
                    float ratioThresh = 0.7f;
                    List<DMatch> listOfGoodMatches = new ArrayList<>();
                    for (int i = 0; i < knnMatches.size(); i++) {
                        if (knnMatches.get(i).rows() > 1) {
                            DMatch[] matches = knnMatches.get(i).toArray();
                            if (matches[0].distance < ratioThresh * matches[1].distance) {
                                listOfGoodMatches.add(matches[0]);
                            }
                        }
                    }
                    MatOfDMatch goodMatches = new MatOfDMatch();
                    goodMatches.fromList(listOfGoodMatches);
                    Log.i("gonzaga", goodMatches.toString());
                    allGoodMatches[j] = goodMatches;
                }
            //return allGoodMatches;*/
            return null;
            /*} catch (NullPointerException e){
                // TODO: Error Code 003
                Toast.makeText(MainActivity.this,"Error Code: 003",Toast.LENGTH_SHORT).show();
                Log.e("fricatta", "Error Code: 003");
                Log.e("gonzaga", "args 0: " + h[0].toString());
                Log.e("gonzaga", "args 1: " + h[1].toString());
                Log.e("gonzaga", "kp length: " + Integer.toString(kp.length));
                Log.e("gonzaga", "des length: " + Integer.toString(des.length));
                Log.e("gonzaga", "kp 0: " + kp[0].toString());
                Log.e("gonzaga", "des 0: " + des[0].toString());
                return null;
            } catch (Exception e) {
                // TODO: Error Code 004
                Toast.makeText(MainActivity.this,"Error Code: 004",Toast.LENGTH_SHORT).show();
                Log.e("fricatta", "Error Code: 004");
                e.printStackTrace();
                Log.e("gonzaga", "args 0: " + h[0].toString());
                Log.e("gonzaga", "args 1: " + h[1].toString());
                Log.e("gonzaga", "kp length: " + Integer.toString(kp.length));
                Log.e("gonzaga", "des length: " + Integer.toString(des.length));
                Log.e("gonzaga", "kp 0: " + kp[0].toString());
                Log.e("gonzaga", "des 0: " + des[0].toString());
                Log.e("gonzaga", e.getClass().getCanonicalName());
                return null;
            }*/
        }

    public native String stringFromJNI();

    static {
        System.loadLibrary("hello-jni");
    }

}
