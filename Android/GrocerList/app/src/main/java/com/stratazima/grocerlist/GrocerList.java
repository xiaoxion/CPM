package com.stratazima.grocerlist;

import android.app.Application;
import com.parse.Parse;

public class GrocerList extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "3fE0cRIrdo0RW4Y3DBiNueuJzpWijqPjufuTDllW", "gME0wQ3IB5ZAbyPYBBgShjUOfQ7e92w9U3ayF7m3");
    }
}
