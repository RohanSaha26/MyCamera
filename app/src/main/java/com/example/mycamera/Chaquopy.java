package com.example.mycamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class Chaquopy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chaquopy);

        TextView textView = (TextView) findViewById(R.id.textView);
        Button button = (Button) findViewById(R.id.button);

        if(!Python.isStarted()){
            Python.start(new AndroidPlatform((this)));
        }

        Python py = Python.getInstance();

        PyObject pyScript = py.getModule("pyScript");

        PyObject hello = pyScript.callAttr("hello");
        textView.setText(hello.toString());


//        PyObject returnArr = pyScript.callAttr("returnArr",10);

//        textView.setText(returnArr.toString());

    }
}