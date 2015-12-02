package hhp.pdfreader;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by hhphat on 7/27/2015.
 */
public class ListOfPdfProperties extends ArrayList<PdfFileProperties> implements Serializable {
    public ListOfPdfProperties(ArrayList<PdfFileProperties> favouriteList) {
        super();
        for (int i=0;i<favouriteList.size();++i){
            this.add(favouriteList.get(i));
        }
    }
    public enum PdfSortCase{
        SORTBYNAME(0),
        SORTBYDATE(1),
        SORTFOLDERSYSTEM(2), SORTBYKEYWORD(3);
        private int value;
        private PdfSortCase(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    public ListOfPdfProperties loadFile(Context context, String fileName){
        FileInputStream fis = null;
        try {
            fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            ListOfPdfProperties simpleClass = (ListOfPdfProperties) is.readObject();
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
    public void saveFile(Context context, String fileName){
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
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
    public  ListOfPdfProperties getFavouriteList(Context context){
        ArrayList<PdfFileProperties> favouriteList = new ArrayList<PdfFileProperties>();
        for (int i=0; i < this.size(); i++) {
            PdfFileProperties pdfProperties = this.get(i);
            if (pdfProperties.loadFile(context) == null){
                pdfProperties.saveFile(context);
            }
            else
                pdfProperties = pdfProperties.loadFile(context);
            if (pdfProperties.isFavourite())
                favouriteList.add(pdfProperties);
        }
        return new ListOfPdfProperties(favouriteList);
    }
    public  ListOfPdfProperties getFilesList(Context context){
        ArrayList<PdfFileProperties> list = new ArrayList<PdfFileProperties>();
        for (int i=0; i < this.size(); i++) {
            PdfFileProperties pdfProperties = this.get(i);
            if (pdfProperties.loadFile(context) == null){
                pdfProperties.saveFile(context);
            }
            else
                pdfProperties = pdfProperties.loadFile(context);
            list.add(pdfProperties);
        }
        return new ListOfPdfProperties(list);
    }
    private int compareTwoPdfFileAs(PdfFileProperties pdf1,PdfFileProperties pdf2, int index){
        if (pdf1.getFileName().equals("..."))
            return -1;
        if (pdf1.isFolder() || pdf2.isFolder())
            return compareFolder(pdf1,pdf2);
        if (index != PdfSortCase.SORTFOLDERSYSTEM.getValue()) {
            if (index == PdfSortCase.SORTBYNAME.getValue()) {
                return pdf1.CompareToAsName(pdf2);
            } else
                return pdf1.CompareToAsDate(pdf2);
        }
        return 0;
    }
    private int compareFolder(PdfFileProperties pdf1,PdfFileProperties pdf2){
        if (pdf1.isFolder() == pdf2.isFolder())
            return 0;
        else
        if (pdf1.isFolder()&& (!pdf2.isFolder())){
            return -1;
        }
        else
            return 1;
    }
    public void sortFileAs(final PdfSortCase pdfSortCase){
        Collections.sort(this, new Comparator<PdfFileProperties>() {
            public int compare(PdfFileProperties pdf1, PdfFileProperties pdf2) {
                return compareTwoPdfFileAs(pdf1, pdf2, pdfSortCase.getValue());
            }
        });
    }

    public ListOfPdfProperties() {
        super();
    }
    public ArrayList<PdfFileProperties> getSearchResultAs(Context context, PdfSortCase
            sortbykeyword, String s) {

        ArrayList<PdfFileProperties> searchList = new ArrayList<PdfFileProperties>();
        for (int i=0; i < this.size(); i++) {
            PdfFileProperties pdfProperties = this.get(i);
            if (pdfProperties.getFileName().toLowerCase().contains(s.toLowerCase())) {
                String tmp = pdfProperties.getFileName();
                if(!s.equals("")) {
                    tmp = tmp.replaceAll(s.toLowerCase(), addColor
                            (addUnderline(s.toLowerCase())));
                    tmp = tmp.replaceAll(s.toUpperCase(), addColor
                            (addUnderline(s.toUpperCase())));
                }
                pdfProperties.setSearchResultName(tmp);
                searchList.add(pdfProperties);
            }
            pdfProperties.saveFile(context);
        }
        Collections.sort(searchList, new Comparator<PdfFileProperties>() {
            public int compare(PdfFileProperties pdf1, PdfFileProperties pdf2) {
                return compareTwoPdfFileAs(pdf1, pdf2, PdfSortCase.SORTBYNAME.getValue());
            }
        });
        return new ListOfPdfProperties(searchList);
    }
    private String addBold(String str){
        return "<b>"+str+"</b>";
    }
    private String addColor(String str){
        return "<font color='#ffff580b'>"+str+"</font>";
    }
    private String addUnderline(String str){
        return "<u>"+str+"</u>";
    }
}
