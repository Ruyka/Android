package hhp.pdfreader;

import android.graphics.Point;
import android.view.MotionEvent;

import java.util.ArrayList;

import static java.lang.Math.sqrt;

public class OnTouchTracking {
    final static int GO_DOWN = 0;
    final static int GO_UP = 1;
    final static int SAME_POSITION = 2;
    private FloatPoint prevPoint;
    private FloatPoint curPoint;
    private int numPoint = 0;

    public OnTouchTracking(){
        prevPoint = new FloatPoint();
        curPoint = new FloatPoint();
    }

    public void addCoordinates(float x, float y){
        numPoint++;
        prevPoint.setXY(curPoint.getX(),curPoint.getY());
        curPoint.setXY(x,y);
    }

    public int getDirection() {
        if (numPoint <2) {
            return -1;
        }
        if (isDown()) return GO_DOWN;
        if (isUp()) return GO_UP;
        return SAME_POSITION;
    }

    private boolean isUp() {
        return (prevPoint.getY()-curPoint.getY()>0);
    }

    private boolean isDown() {
        return (prevPoint.getY()-curPoint.getY()<0);
    }
}