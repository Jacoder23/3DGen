// If you are ever feeling angry or down, just remember fricatta. - Albert

package jcdr23.com.dev3d;

import android.support.v7.app.AppCompatActivity;
import java.lang.String;
import java.util.Timer;
import java.util.TimerTask;
import java.lang.Exception;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
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
                            CompletableFuture CFkpDetect = CompletableFuture.supplyAsync(() -> kpDetect(files));
                            try {
                                Log.i("gonzaga", CFkpDetect.get().toString());
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
                            }
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

    public Object[] kpDetect(String[] files) {
        Mat[] desResult = new Mat[0];
        MatOfKeyPoint[] kpResult = new MatOfKeyPoint[0];
        for (int i = 0; i < files.length; i++) {
            desResult = new Mat[500];
            Log.i("fricatta", "Alfred");
            TextView log = findViewById(R.id.txt_log);
            //try {
            log.setText("Setup is successful. Continuing.");
            String userLog = "";
            MatOfKeyPoint keypoints = new MatOfKeyPoint();
            Mat des = new Mat();
            ORB orb = ORB.create(150000, 1.3f, 11, 31, 0, 3, 0, 31);
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

        Object[] results = {desResult, kpResult};

        return results;
        //} catch (Exception e){
        //    log.setText("An error has occurred");
        //    return null;
        //}
    }
        public MatOfDMatch FLANNMATCH(Object[] args) {
            Mat[] des = Mat[].class.cast(args[0]);
            MatOfKeyPoint[] kp = MatOfKeyPoint[].class.cast(args[1]);
            //-- Step 2: Matching descriptor vectors with a FLANN based matcher
            // Since SURF is a floating-point descriptor NORM_L2 is used
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
            List<MatOfDMatch> knnMatches = new ArrayList<>();
            matcher.knnMatch(des[0], kp[0], knnMatches, 2);
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

            return goodMatches;
        }

    public native String stringFromJNI();

    static {
        System.loadLibrary("hello-jni");
    }

}
