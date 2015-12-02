package hhp.pdfreader;

/**
 * Created by hhphat on 7/28/2015.
 */
public class FloatPoint{
    private float x,y;
    public FloatPoint(){
        setX(0);
        setY(0);
    }

    public FloatPoint(float x, float y){
        setX(x);
        setY(y);
    }
    public void setXY(float x, float y){
        setX(x);
        setY(y);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
