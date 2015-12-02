package hhp.pdfreader;
import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by hhphat on 7/7/2015.
 */
public class EssentialFunction {
    //context: may be use getContext to call
    //txtView, the textView need to by set
    //fontSize: the font size
    //custom font: Ex: "fonts/FabfeltScript-Bold.otf")
    // gravity: Gravity.CENTER or ...
    static  void setFont(Context context,TextView txtView, int fontSize, String
            customFont, int gravity) {
        Typeface type = Typeface.createFromAsset(context.getAssets(), customFont);
        if (gravity>0) txtView.setGravity(gravity);
        (txtView).setTypeface(type);
        txtView.setTextSize(fontSize);
    }
    //something like loadSerializedObject(new File("/sdcard/save_object.bin"));
    //To load an Object which extends Serialization class
    public Object loadSerializedObject(File f)
    {
        try
        {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            Object o = ois.readObject();
            return o;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }
    public void saveObject(Object object,File f) {
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(object);
            oos.flush();
            oos.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
