package hhp.pdfreader;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileListAdapter extends ArrayAdapter<PdfFileProperties>{
    private final ArrayAdapter THIS = this;
    private final ArrayList<PdfFileProperties> listFile;
    private int layoutId;
    private Context context;
    private TextView txt;
    private ImageView img;
    private ImageButton imgBtn ;
    private boolean isShowDirective = false;
    private ListOfFileFragment listOfFileFragment;
    private boolean isSearch = false;

    public FileListAdapter(Context context, int layoutId, ArrayList<PdfFileProperties> listFile){
        super(context, layoutId, listFile);
        this.context = context;
        this.listFile = listFile;
        setLayoutId(layoutId);
    }
    private ListOfPdfProperties list = null;
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (list!=null) {
            list.clear();
            list.addAll(listFile);
        }
    }

    public void setIsShowDirective(boolean isShowDirective) {
        this.isShowDirective = isShowDirective;
    }

    public void setFragment(ListOfFileFragment listOfFileFragment) {
        this.listOfFileFragment = listOfFileFragment;
    }

    public ListOfPdfProperties getListFile() {
        return new ListOfPdfProperties(listFile);
    }

    public void notifyChangeOn(ListOfPdfProperties list) {
        this.list = list;
    }

    public void setIsSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    private class ViewHolder{
        public TextView txt;
        public ImageView img;
        public int rowPosition;

        public ViewHolder(TextView txt, ImageView img, int rowPosition) {
            this.txt = txt;
            this.img = img;
            this.rowPosition = rowPosition;
        }
    };
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theinflater = LayoutInflater.from(getContext());
        final FileListAdapter THIS = this;
        final View row = theinflater.inflate(layoutId, parent, false);
        //set Properties for View inside row
        setViewProperties(row, position);

        ViewHolder myView = new ViewHolder(txt,img,position);
        row.setTag(myView);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowOnClick(row);
            }
        });
        return row;
    }
    public void setTextViewProperties(View row, PdfFileProperties pdfFile){
        txt = (TextView) row.findViewById(R.id.textView_titleView);
        if (pdfFile.getSearchResultName()==null|| !isSearch)
            txt.setText(resizeText(pdfFile.getFileName()));
        else
            txt.setText(Html.fromHtml(pdfFile.getSearchResultName()));
    }
    private void setViewProperties(View row, int position) {
        PdfFileProperties pdfFile = getItem(position);
        //set title
        setTextViewProperties(row,pdfFile);

        //set icon
        img = (ImageView) row.findViewById(R.id.imageView_iconOfPdf);
        if (pdfFile.isFolder())
            img.setImageResource(pdfFile.getIconId());
        else
            img.setImageBitmap(pdfFile.getBitmap());
        //set favourite start if not folder
        imgBtn = (ImageButton) row.findViewById(R.id.imageButton_favouriteImage);
        imgBtn.setVisibility(View.GONE);
        if (!getItem(position).isFolder()) {
            imgBtn.setVisibility(View.VISIBLE);
            imgBtn.setTag(position);
            if (pdfFile.isFavourite()) {
                imgBtn.setImageResource(R.drawable.favourite_star);
            } else
                imgBtn.setImageResource(R.drawable.un_favourite_star);

            imgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeCurrentPdfFileFavouriteProperties(v);
                }
            });
        }
    }

    private String resizeText(String title) {

        if (title.length() >30)
            return title.substring(0, 30) + "...";
        else
            return title;
    }

    public void setLastPage(int position, int lastPage){
        getItem(position).setLastPageViewed(lastPage);
    }

    private void changeCurrentPdfFileFavouriteProperties(View v) {
        PdfFileProperties pdfFile = getItem((int)v.getTag());

        if (pdfFile.isFavourite()){
            pdfFile.setFavourite(false);
        }
        else {
            pdfFile.setFavourite(true);
        }
        pdfFile.saveFile(getContext());
        notifyDataSetChanged();
    }
    private void rowOnClick(View row){
        ViewHolder vh = (ViewHolder) row.getTag();
        if (isShowDirective && (!getItem(vh.rowPosition).isFolder())) return;

        row.setBackgroundColor(Color.BLACK);
        vh.txt.setBackgroundColor(Color.BLACK);
        vh.img.setBackgroundColor(Color.BLACK);
        if (!getItem(vh.rowPosition).isFolder()) {
            getItem(vh.rowPosition).setLastViewed();
            getItem(vh.rowPosition).saveFile(getContext());
            listOfFileFragment.openPdf(getItem(vh.rowPosition).getDirective(), vh.rowPosition, getItem(vh.rowPosition).getLastPageViewed());
        }
        else{
            PdfFileProperties pdf = getItem(vh.rowPosition);
            if (vh.rowPosition == 0 && pdf.getFolderTreeDeep()>1){
                PdfFileProperties tmp = new PdfFileProperties("...", R.drawable.folder);
                tmp.setIsFolder(true);
                tmp.setFolderTreeDeep(pdf.getFolderTreeDeep() - 1);
                listFile.clear();
                add(tmp);
                scanDirectories(PdfFileProperties.getDirectiveAtDeep(pdf.getFolderTreeDeep()-2),
                        pdf.getFolderTreeDeep()-2);
                notifyDataSetChanged();
            }
            else if (vh.rowPosition == 0 && pdf.getFolderTreeDeep()==1) {
                listFile.clear();
                PdfFileProperties tmp = new PdfFileProperties("Internal Storage",R.drawable.folder);
                tmp.setIsFolder(true);
                tmp.setDirective(getContext().getFilesDir().toString());
                add(tmp);
                tmp = new PdfFileProperties("Download",R.drawable.folder);
                tmp.setIsFolder(true);
                tmp.setDirective(Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS).toString());
                add(tmp);
                tmp = new PdfFileProperties("SdCard",R.drawable.folder);
                tmp.setIsFolder(true);
                tmp.setDirective((Environment.getExternalStorageDirectory().getAbsolutePath()));
                add(tmp);
                notifyDataSetChanged();
            }
            else if (vh.rowPosition > 0 || pdf.getFolderTreeDeep() == 0){
                PdfFileProperties tmp = new PdfFileProperties("...", R.drawable.folder);
                tmp.setIsFolder(true);
                String location = pdf.getDirective();
                int deep = pdf.getFolderTreeDeep();
                tmp.setFolderTreeDeep(deep+1);
                PdfFileProperties.setDirectiveAtDeep(deep,location);
                listFile.clear();
                add(tmp);
                scanDirectories(location,deep);
                notifyDataSetChanged();
            }
        }
    }
    public void scanDirectories(String directive, int deep) {
        List listOfFileInfo = new ArrayList<HashMap>();
        listOfFileInfo = getData(listOfFileInfo , directive);

        for (int i = 0; i<listOfFileInfo.size();++i){
            HashMap temp= (HashMap) listOfFileInfo.get(i);
            PdfFileProperties a;
            if ((boolean) temp.get("isFolder")) {
                a = new PdfFileProperties((String) temp.get("title"), R.drawable.folder);
                a.setIsFolder(true);
            }
            else {
                a = new PdfFileProperties((String) temp.get("title"), R.drawable
                        .favourite_star);
            }
            if (a.loadFile(getContext())!= null)
                a = a.loadFile(getContext());
            a.setFolderTreeDeep(deep+1);
            a.setDirective((String) temp.get("path"));
            add(a);
        }
        ((ListOfPdfProperties)listFile).sortFileAs(ListOfPdfProperties.PdfSortCase.SORTFOLDERSYSTEM);
    }

    protected void addItem(List<Map> data, String name, String path,boolean isFolder)
    {
        HashMap temp = new HashMap();
        temp.put("title", name);
        temp.put("path", path);
        temp.put("isFolder", isFolder);
        data.add(temp);
    }

    protected List getData(List myData,String mPath)
    {
        File f = new File(mPath);

        String[] files = f.list();
        if (files ==null) return myData;
        for (int i = 0; i < files.length; i++) {
            StringBuilder sb = new StringBuilder(mPath);
            sb.append(File.separatorChar);
            sb.append(files[i]);
            String path = sb.toString();
            File c = new File(path);
            if (c.isDirectory()) {
                addItem(myData, files[i], path, true);
                //getData(myData,path);
            }
            else if (isPdf(files[i]))
                addItem(myData, files[i], path, false);
        }
        return myData;
    }

    private boolean isPdf(String fileName) {
        return fileName.contains("pdf");
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }
}
