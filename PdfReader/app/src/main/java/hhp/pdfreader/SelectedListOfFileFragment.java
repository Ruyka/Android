package hhp.pdfreader;

/**
 * Created by hhphat on 8/21/2015.
 */

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SelectedListOfFileFragment extends ListFragment {
    private ListOfPdfProperties listOfFileProperties;
    private SelectedFileListAdapter adapter;
    private ArrayList<Boolean> listOfCheckedBox;
    private Object selectedPathOfItem;

    public ListOfPdfProperties getListOfFileProperties(){
        return listOfFileProperties;
    }
    public void applyChange(){
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listOfCheckedBox = new ArrayList<Boolean>();
        int i=0;
        while (i<listOfFileProperties.size()){
            listOfCheckedBox.add(false);
            ++i;
        }
        adapter = new SelectedFileListAdapter(getActivity(),R.layout
                .row_layout_of_selected_list_file,
                listOfFileProperties,listOfCheckedBox);

        this.setListAdapter(adapter);

    }

    public void erasedCheckedItems() {
        int i=0;
        while (i<listOfCheckedBox.size()){
            listOfCheckedBox.set(i, false);
            ++i;
        }
    }

    public void setList(ListOfPdfProperties listOfPdfProperties) {
        this.listOfFileProperties = listOfPdfProperties;
    }

    public void DeleteSelectedItems() {
        int i =0;
        while (i<listOfCheckedBox.size())
            if (listOfCheckedBox.get(i)){
                PdfFileProperties tmp = listOfFileProperties.get(i);
                File temp = new File(tmp.getDirective());

                if (tmp.isFolder())
                    DeleteRecursive(temp);
                else
                    temp.delete();
                tmp.deletePdfInfo();
                listOfFileProperties.remove(i);
                listOfCheckedBox.remove(i);
            }
        else ++i;
        //Refresh();
    }
    private void DeleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);
        fileOrDirectory.delete();

    }
    public int countItemSelected(){
        int i =0, num =0;
        while (i<listOfCheckedBox.size()){
            if (listOfCheckedBox.get(i)){
                num++;
            }
        ++i;
        }
        return num;
    }
    public void RenameSelectedItems(String s) {
        int i =0, num =0;
        while (i<listOfCheckedBox.size()){
            if (listOfCheckedBox.get(i)){
                if (listOfFileProperties.get(i).getFileName().contains(".pdf"))
                    if (!s.contains(".pdf")) s = s + ".pdf";
                String filePath = listOfFileProperties.get(i).getDirective();
                File from = new File(filePath);
                filePath = filePath.replace(listOfFileProperties.get(i).getFileName(),"");
                File to = new File(filePath,s);
                if (to.exists()){
                    Toast.makeText(getActivity().getApplicationContext(),"File Name Exist",Toast
                            .LENGTH_SHORT).show();
                }
                boolean tmp = from.renameTo(to);
                listOfFileProperties.get(i).setFileName(s);
                listOfFileProperties.get(i).setDirective(to.getAbsolutePath());
                listOfFileProperties.get(i).saveFile(getActivity());
            }
            ++i;
        }
        //Refresh();
    }

    public ArrayList<PdfFileProperties> getSelectedItems() {
        int i=0;
        ArrayList<PdfFileProperties> strtmp = new ArrayList<PdfFileProperties>();
        while (i<listOfCheckedBox.size()) {
            if (listOfCheckedBox.get(i)) {
                PdfFileProperties tmp = listOfFileProperties.get(i);
                strtmp.add(tmp);
            }
            ++i;
        }
        return strtmp;
    }
    private static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File
            targetLocation)  {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }
            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {
                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            InputStream in = null;
            try {
                in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void moveFile(String tmp) {
        ArrayList<PdfFileProperties> listTmp = getSelectedItems();
        for (int i=0; i <listTmp.size();++i) {
            copyDirectoryOneLocationToAnotherLocation(new File(listTmp.get(i).getDirective()),
                    new File(tmp+'/'+listTmp.get(i).getFileName()));
            DeleteRecursive(new File(listTmp.get(i).getDirective()));
            if (!listTmp.get(i).isFolder()) {
                listTmp.get(i).deletePdfInfo();
            }
            listTmp.get(i).setDirective(tmp + '/' + listTmp.get(i).getFileName());
            listTmp.get(i).saveFile(getActivity());
            listOfFileProperties.remove(listTmp.get(i));
            listTmp.remove(i);
            i--;
        }
        //Refresh();
    }
    // Refresh the folder/files in runtime
//    public void //Refresh(){
//        for (int i=0; i< listOfFileProperties.size();++i){
//            PdfFileProperties tmp =listOfFileProperties.get(i).loadFile(getActivity());
//            if (tmp== null) {
//                listOfFileProperties.remove(i);
//                listOfCheckedBox.remove(i);
//                i--;
//            }
//            else
//                listOfFileProperties.set(i,tmp);
//        }
//    }
    public void showSearch(ListOfPdfProperties list, String s) {
        this.listOfFileProperties = list;
        listOfFileProperties = (ListOfPdfProperties) listOfFileProperties.getSearchResultAs(getActivity()
                , ListOfPdfProperties.PdfSortCase.SORTBYKEYWORD,s);
        erasedCheckedItems();
        adapter = new SelectedFileListAdapter(getActivity(),R.layout.row_layout_of_selected_list_file,
                listOfFileProperties, listOfCheckedBox);
        adapter.setIsSearch(true);
        this.setListAdapter(adapter);
    }
}
