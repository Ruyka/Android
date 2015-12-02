package hhp.pdfreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.media.ThumbnailUtils;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Date;

public class PdfFileProperties implements Serializable {
    private String fileName;
    private int iconId;
    private Date lastViewed;
    private int lastPageViewed;
    private boolean favourite;
    private String directive;
    private boolean isFolder = false;
    private int folderTreeDeep = 0;
    public static String[] DirectiveDeep = new String[1000];
    private String searchResultName = null;
    public PdfFileProperties(String fileName, int iconId){
        this.fileName = fileName;
        this.iconId = iconId;
        setLastViewed();
        favourite = false;
    }

    public int CompareToAsName(PdfFileProperties pdf){
        return fileName.compareTo(pdf.getFileName());
    }

    public int CompareToAsDate(PdfFileProperties pdf){
        return lastViewed.compareTo(pdf.getLastViewed());
    }

    public String getFileName(){
        return fileName;
    }

    public void setFavourite(boolean isFavourite) {
        this.favourite = isFavourite;
    }

    public void setLastViewed() {
        this.lastViewed = new Date();
    }

    public PdfFileProperties loadFile(Context context){
        FileInputStream fis = null;
        try {
            String str;
            if (isFolder())
                str = fileName;
            else
                str = fileName.substring(0, fileName.length() - 4);
            fis = context.openFileInput(str);
            ObjectInputStream is = new ObjectInputStream(fis);
            PdfFileProperties simpleClass = (PdfFileProperties) is.readObject();
            is.close();
            fis.close();
            return simpleClass;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveFile(Context context){
        FileOutputStream fos = null;
        try {
            String str;
            if (isFolder())
                str = fileName;
            else
                str = fileName.substring(0, fileName.length() - 4);
            fos = context.openFileOutput(str, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFavourite() {
        return favourite;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public int getIconId() {
        return iconId;
    }

    public String getDirective() {
        return directive;
    }

    public void setDirective(String directive) {
        this.directive = directive;
    }

    public void setFileName(String title) {
        this.fileName = title;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public int getFolderTreeDeep() {
        return folderTreeDeep;
    }

    public void setFolderTreeDeep(int folderTreeDeep) {
        this.folderTreeDeep = folderTreeDeep;
    }

    public static String getDirectiveAtDeep(int folderTreeDeep) {
        return DirectiveDeep[folderTreeDeep];
    }

    public static void setDirectiveAtDeep(int folderTreeDeep,String directive) {
        DirectiveDeep[folderTreeDeep] = directive;
    }

    public int getLastPageViewed() {
        return lastPageViewed;
    }

    public void setLastPageViewed(int lastPageViewed) {
        this.lastPageViewed = lastPageViewed;
    }

    public Bitmap getBitmap() {
        if(!isFolder()) {
            File file = new File(directive);
            String thumbnailPath = file.getName();
            thumbnailPath = file.getParent() + "/" + thumbnailPath.substring(0, thumbnailPath.lastIndexOf(".")) + ".png";
            File fileThumbnail = new File(thumbnailPath);

            try {
            if (fileThumbnail.exists()) {
                return BitmapFactory.decodeFile(thumbnailPath);
            } else {
                    PdfRenderer renderer =new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));
                    // create bitmap for thumbnails
                    PdfRenderer.Page page = renderer.openPage(0);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_4444);
                    // fill bitmap with white
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawColor(Color.WHITE);
                    // get pdf image
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    page.close();
                    renderer.close();

                    // create thumbnais
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 100, 100);

                    // out thumbnails
                    FileOutputStream out = new FileOutputStream(fileThumbnail);
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    return thumbnail;
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deletePdfInfo() {
        String tmp;
        File file ;
        if (!isFolder()) {
            tmp = directive.substring(0, directive.length() - 4);
            file = new File(tmp+".png");
            file.delete();
        }
        else tmp = directive;
        file = new File(tmp);
        file.delete();
    }

    public String getSearchResultName() {
        return searchResultName;
    }


    public void setSearchResultName(String str) {
        searchResultName = str;
    }
}
