package com.knightboost.lancetx;

import android.util.Log;

public class ConstructorTest {
    public ConstructorTest(String msg){
        Log.i("ConstructorTestI",msg);
        Log.w("ConstructorTestW",msg);
        Log.e("ConstructorTestE",msg);
    }

}
