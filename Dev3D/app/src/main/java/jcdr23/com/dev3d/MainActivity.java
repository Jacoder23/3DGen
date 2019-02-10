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
                            SFM(files);
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

    public void SFM(String[] files){
        Log.i("fricatta", "Alfred");
        TextView log = findViewById(R.id.txt_log);
        try {
            log.setText("Setup is successful. Continuing.");
            Timer timer = new Timer();
            TimerTask t = new TimerTask() {
                double i = 0;
                TextView log = findViewById(R.id.txt_log);
                @Override
                public void run() {
                    i++;
                }
            };
            timer.scheduleAtFixedRate(t,1,1);
            for(int i = 0; i < files.length; i++) {
                Mat img = Imgcodecs.imread(files[0]);
                MatOfKeyPoint keypoints = new MatOfKeyPoint();
                ORB orb = ORB.create();
                orb.detect(img, keypoints);
                Mat des = new Mat();
                orb.compute(img, keypoints, des);
                Log.i("fricatta", Double.toString((des.size().width)*(des.size().height)) + " | Time expended (seconds): " + Double.toString(i/1000));
                log.setText(Double.toString((des.size().width)*(des.size().height)) + " | Time expended (seconds): " + Double.toString(i/1000));
            }
        } catch (Exception e){
            log.setText("An error has occurred");
        }
    }

    public native String stringFromJNI();

    static {
        System.loadLibrary("hello-jni");
    }

}
