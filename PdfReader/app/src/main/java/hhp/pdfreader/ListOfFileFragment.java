package hhp.pdfreader;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hakashi.ruyka.pdfviewer.PDFView;


/**
 * Created by hhphat on 7/27/2015.
 */
public class ListOfFileFragment extends ListFragment {
    private static final int REQ_CODE_OPEN_PDF = 125;
    public static boolean isChooseDirecive = false;
    protected ListOfPdfProperties listOfFileProperties;
    private ListOfPdfProperties listOfFavoriteItems;
    private ListOfPdfProperties listOfHistoryItems;
    private FileListAdapter adapter;
    private View.OnTouchListener onTouch;
    private boolean isShowFavourite = false;
    private boolean isShowHistory = false;
    private boolean isShowAllFiles = false;
    private boolean isShowSearch = false;
    private ListOfPdfProperties listOfAllItems ;
    private ListOfPdfProperties listOfSearchItems;

    public ListOfFileFragment() {
        super();
        if(listOfFileProperties == null)
            listOfFileProperties = new ListOfPdfProperties();
    }
    public FileListAdapter getFileListAdapter(){
        return adapter;
    }

    public ListOfPdfProperties getListOfFileProperties(){
        if (isShowFavourite)
            return listOfFavoriteItems;
        else if (isShowHistory)
            return listOfHistoryItems;
        else if (isShowAllFiles)
            return listOfAllItems;
        else if (isShowSearch)
            return listOfSearchItems;
        else
            return listOfFileProperties;
    }

    public void applyChange(){
        adapter.notifyDataSetChanged();
    }
    public void showAllFiles(){
        setShow(0);
        listOfAllItems = new ListOfPdfProperties();
        scanAllDirectoriesInto(listOfAllItems, getActivity().getFilesDir().toString());
        scanAllDirectoriesInto(listOfAllItems,Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).toString());
        listOfAllItems = listOfAllItems.getFilesList(getActivity());
        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                listOfAllItems);
        adapter.setFragment(this);

        this.setListAdapter(adapter);
    }

    private void setShow(int i) {
        switch (i){
            case 0:
                isShowAllFiles = true;
                isShowHistory = false;
                isShowFavourite = false;
                isShowSearch=false;
                break;
            case 1:
                isShowAllFiles = false;
                isShowHistory = true;
                isShowFavourite = false;
                isShowSearch = false;
                break;
            case 2:
                isShowAllFiles = false;
                isShowHistory = false;
                isShowFavourite = true;
                isShowSearch = false;
                break;
            case 3:
                isShowAllFiles = false;
                isShowHistory = false;
                isShowFavourite = false;
                isShowSearch = false;
                break;
            case 4:
                isShowSearch = true;
                isShowAllFiles = false;
                isShowHistory = false;
                isShowFavourite = false;
        }
    }

    public void showFavorites(){
        setShow(2);
        listOfFavoriteItems = new ListOfPdfProperties();
        scanAllDirectoriesInto(listOfFavoriteItems,getActivity().getFilesDir().toString());
        scanAllDirectoriesInto(listOfFavoriteItems,Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).toString());

        listOfFavoriteItems = listOfFavoriteItems.getFavouriteList(getActivity());

        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                listOfFavoriteItems);
        adapter.setFragment(this);
        this.setListAdapter(adapter);
    }
    public void showHistory(){
        setShow(1);
        listOfHistoryItems = new ListOfPdfProperties();

        scanAllDirectoriesInto(listOfHistoryItems,getActivity().getFilesDir().toString());
        scanAllDirectoriesInto(listOfHistoryItems,Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).toString());

        listOfHistoryItems =listOfHistoryItems.getFilesList(getActivity());
        listOfHistoryItems.sortFileAs(ListOfPdfProperties.PdfSortCase.SORTBYDATE);

        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                listOfHistoryItems);
        adapter.setFragment(this);
        this.setListAdapter(adapter);
    }
    public void setOnTouchListener(View.OnTouchListener onTouch){
        this.onTouch = onTouch;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (listOfFileProperties.size()==0) {
            PdfFileProperties tmp = new PdfFileProperties("Internal Storage",R.drawable.folder);
            tmp.setIsFolder(true);
            tmp.setDirective(getActivity().getFilesDir().toString());
            listOfFileProperties.add(tmp);
            tmp = new PdfFileProperties("Download",R.drawable.folder);
            tmp.setIsFolder(true);
            tmp.setDirective(Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS).toString());
            listOfFileProperties.add(tmp);
            tmp = new PdfFileProperties("SdCard",R.drawable.folder);
            tmp.setIsFolder(true);
            tmp.setDirective(Environment.getExternalStorageDirectory().getAbsolutePath());
            listOfFileProperties.add(tmp);
        }
        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                getListOfFileProperties());
        adapter.setIsShowDirective(isChooseDirecive);
        adapter.setFragment(this);
        this.setListAdapter(adapter);
        this.getListView().setOnTouchListener(onTouch);
    }

    public void openPdf(String filePath, int pos, int lastPage){
        final Intent intent = new Intent(getActivity(), PDFView.class);
        intent.putExtra("pdf_path", filePath);
        intent.putExtra("position", pos);
        intent.putExtra("last_page", lastPage);
        getActivity().startActivityForResult(intent, REQ_CODE_OPEN_PDF);
        applyChange();
    }

    public void scanAllDirectoriesInto(ListOfPdfProperties list, String directive) {
        List listOfFileInfo = new ArrayList<HashMap>();
        listOfFileInfo = getData(listOfFileInfo , directive);
        for (int i = 0; i<listOfFileInfo.size();++i){
            HashMap temp= (HashMap) listOfFileInfo.get(i);
            PdfFileProperties a;
            a = new PdfFileProperties((String) temp.get("title"), R.drawable.favourite_star);
            a.setDirective((String) temp.get("path"));
            if(a.loadFile(getActivity())==null)
                a.saveFile(getActivity());
            else a = a.loadFile(getActivity());
            list.add(a);
        }
    }

    protected void addItem(List<Map> data, String name, String path)
    {
        HashMap temp = new HashMap();
        temp.put("title", name);
        temp.put("path", path);
        data.add(temp);
    }

    protected List getData(List myData,String mPath)
    {
        File f = new File(mPath);
        String[] files = f.list();
        for (int i = 0; i < files.length; i++) {
            StringBuilder sb = new StringBuilder(mPath);
            sb.append(File.separatorChar);
            sb.append(files[i]);
            String path = sb.toString();
            File c = new File(path);
            if (c.isDirectory()) {
                getData(myData, path);
            }
            else if (isPdf(files[i]))
                addItem(myData, files[i], path);
        }
        return myData;
    }

    private boolean isPdf(String fileName) {
        return fileName.contains(".pdf");
    }

    public void showDirective() {
        setShow(3);
        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                this.getListOfFileProperties());
        adapter.setFragment(this);
        this.setListAdapter(adapter);
    }

    public void setIsChooseDirecive(boolean isChooseDirecive) {
        adapter.setIsShowDirective(true);
    }

    public void showSearch(ListOfPdfProperties listOfPdfProperties, String s) {
        listOfSearchItems = listOfPdfProperties;
        setShow(4);
        listOfSearchItems = (ListOfPdfProperties) listOfSearchItems.getSearchResultAs(getActivity()
                , ListOfPdfProperties.PdfSortCase.SORTBYKEYWORD,s);

        adapter = new FileListAdapter(getActivity(),R.layout.row_layout_of_list_file,
                listOfSearchItems);
        adapter.setFragment(this);
        adapter.setIsSearch(true);
        adapter.notifyChangeOn(listOfPdfProperties);
        this.setListAdapter(adapter);
    }
    // Refresh folders/files in runtime
//    public void Refresh(){
//        ListOfPdfProperties listOfFileProperties = getListOfFileProperties();
//        for (int i=0; i< listOfFileProperties.size();++i){
//            PdfFileProperties tmp =listOfFileProperties.get(i).loadFile(getActivity());
//            if (tmp== null) {
//                listOfFileProperties.remove(i);
//                i--;
//            }
//            else
//                listOfFileProperties.set(i,tmp);
//        }
//    }
}
