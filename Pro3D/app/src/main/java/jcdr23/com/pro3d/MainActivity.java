package jcdr23.com.pro3d;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean folderSelected = false;

        Button selectFolder = findViewById(R.id.btn_selectFolder);
        selectFolder.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Make this onClick listener invoke a call to our file selection library to select a folder
                // TODO Make it so when the activity this button invokes finishes and there is no folder selected, an alert pops up
            }
        });

        Button start = findViewById(R.id.btn_selectFolder);
        selectFolder.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Make this start another activity where the earlier files will be processed.
                // TODO Make it so when this button is clicked and there is no folder selected, an alert pops up
            }
        });
    }


}
