package jcdr23.com.Dev3DNight;

import android.content.pm.PackageManager;
import android.media.audiofx.AudioEffect;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.*;
import java.io.File;
import android.os.Environment;
import java.io.FileWriter;

import org.opencv.core.Size;
import org.opencv.features2d.*;
import org.opencv.core.*;
import org.opencv.imgproc.*;
import org.opencv.calib3d.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.android.OpenCVLoader;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public Mat alfred(String[] files) {
        Mat retVal = new Mat();
        Mat[] desResult = new Mat[999];
        MatOfKeyPoint[] kpResult = new MatOfKeyPoint[999];

        for (int i = 0; i < files.length; i++) {
            ORB orb = ORB.create(3000, 1.6f, 5, 15, 0, 2, 0, 14);
            Mat img = Imgcodecs.imread(files[i]);
            MatOfKeyPoint keypoints = new MatOfKeyPoint(new KeyPoint(), new KeyPoint());
            Mat des = new Mat(2, 2, 0);

            FastFeatureDetector feature = FastFeatureDetector.create();
            feature.setNonmaxSuppression(false);

            Mat mask = new Mat(img.rows(), img.cols(), CvType.CV_8U, Scalar.all(0));

            Log.i("Siggy", "Below is detect and compute!");

            feature.detectAndCompute(img, mask ,keypoints, des);
        }

        return retVal;
    }

    private Mat createDisparityMap(Mat rectLeft, Mat rectRight){

        // Converts the images to a proper type for stereoMatching
        Mat left = new Mat();
        Mat right = new Mat();

        /*Size leftSize = new Size();
        leftSize.width = (rectLeft.width() * 20f) / 100f;
        leftSize.height = (rectLeft.height() * 20f) / 100f;

        Size rightSize = new Size();
        rightSize.width = (rectRight.width() * 20f) / 100f;
        rightSize.height = (rectRight.height() * 20f) / 100f;*/

        Imgproc.cvtColor(rectLeft, left, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(rectRight, right, Imgproc.COLOR_BGR2GRAY);

        /*Imgproc.resize(left, left, leftSize, 0, 0, 0);
        Imgproc.resize(right, right, rightSize, 0, 0, 0);*/

        // Create a new image using the size and type of the left image
        Mat disparity = new Mat(left.size(), left.type());

        int numDisparity = (int)(left.size().width/8);

        StereoSGBM stereoAlgo = StereoSGBM.create(
                0,    // min DIsparities
                numDisparity, // numDisparities
                7,   // SADWindowSize
                2*11*11,   // 8*number_of_image_channels*SADWindowSize*SADWindowSize   // p1
                5*11*11,  // 8*number_of_image_channels*SADWindowSize*SADWindowSize  // p2

                -1,   // disp12MaxDiff
                31,   // prefilterCap
                0,   // uniqueness ratio
                0, // sreckleWindowSize
                25, // spreckle Range
                0); // full DP
        // create the DisparityMap - SLOW: O(Width*height*numDisparity)
        stereoAlgo.compute(left, right, disparity);

        Core.normalize(disparity, disparity, 0, 256, Core.NORM_MINMAX);

        return disparity;
    }

    public void onRequestPermissionsResult ( int requestCode, @NonNull String permissions[],
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case FilePickerDialog.EXTERNAL_READ_PERMISSION_GRANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission has been granted.
                    Toast.makeText(MainActivity.this, "Thank you. Please continue.", Toast.LENGTH_SHORT).show();
                } else {
                    //Permission has not been granted. Notify the user.
                    Toast.makeText(MainActivity.this, "We need permission to access your dataset.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    static {
        System.loadLibrary("opencv_java4");
    }

    public String stereo(String[] files){
        Mat img1 = Imgcodecs.imread(files[0]);
        Mat img2 = Imgcodecs.imread(files[1]);

        /*Size leftSize = new Size();
        leftSize.width = (img1.width() * 5f) / 100f;
        leftSize.height = (img1.height() * 5f) / 100f;

        Size rightSize = new Size();
        rightSize.width = (img2.width() * 5f) / 100f;
        rightSize.height = (img2.height() * 5f) / 100f;

        Imgproc.resize(img1, img1, leftSize, 0, 0, 0);
        Imgproc.resize(img2, img2, rightSize, 0, 0, 0);*/

        Mat disparityMap = createDisparityMap(img1, img2);

        /*Mat really = new Mat();
        Mat q = new Mat();


        for (int x = 0; x <= 4; x++) {
            for (int y = 0; y <= 4; y++) {
                q.put(x, y, 1.0);
            }
        }

        double[] def = {0.0, 0.0};

        Calib3d.stereoRectify();

        Calib3d.reprojectImageTo3D(disparityMap, really, q);*/

        String strin = "ply\n" +
                "format ascii 1.0\n" +
                "comment author: Jacob Meimban\n" +
                "comment object: pointcloud" +
                "element vertex 36" +
                "property float x" +
                "property float y" +
                "property float z" +
                "end_header";
        for(int q = 0; q < disparityMap.height(); q++){
            for (int w = 0; w < disparityMap.width(); w++) {
                double[] j = disparityMap.get(w, q);
                strin += Double.toString(j[0]) + " ";
                strin += Double.toString(j[1]) + " ";
                strin += Double.toString(j[2]) + "\n";
            }
        }

        return strin;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
            Toast.makeText(MainActivity.this, "Unable to load OpenCV", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("SUCCESS", "OpenCV loaded");
            Toast.makeText(MainActivity.this, "OpenCV loaded", Toast.LENGTH_SHORT).show();
        }

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

                FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
                dialog.setTitle("Select your images");

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        if (files.length >= 2) {
                            String king = stereo(files);
                            Log.i("Sev", king);

                            File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
                            boolean isPresent = true;
                            if (!docsFolder.exists()) {
                                isPresent = docsFolder.mkdir();
                            }
                            if (isPresent) {
                                if(Environment.getExternalStorageState() != null) {
                                    try {
                                        File file = new File(docsFolder.getAbsolutePath(), "finale.ply");
                                        FileWriter writer = new FileWriter(file);
                                        writer.append(king);
                                        writer.flush();
                                        writer.close();
                                    } catch (IOException e) {
                                        throw new RuntimeException("Checkmate.");
                                    }
                                }
                            } else {
                                // Failure
                                throw new RuntimeException("Checkmate.");
                            }

                        }
                    }
                });

                dialog.show();
            }
        });
    }
}
