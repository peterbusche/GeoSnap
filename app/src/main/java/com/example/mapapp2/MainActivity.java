/*
Role of this file:
    Activity Lifecycle: Manage the lifecycle methods such as onCreate(), onStart(), and onResume().
    Logic: Respond to user interactions, such as button clicks.
    UI Control: Link XML layout files to Java code using setContentView() and manipulate
                UI elements via IDs.


Other Files in This Folder:
    You may have additional Java files for:
        Other Activities: For other screens in your app.
        Custom Classes: For specific logic, such as a helper class for calculations
                        or handling APIs
 */



package com.example.mapapp2;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Link to your new layout
    }
}

