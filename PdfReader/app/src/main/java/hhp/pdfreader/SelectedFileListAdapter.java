package hhp.pdfreader;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hhphat on 7/27/2015.
 */
public class SelectedFileListAdapter extends ArrayAdapter<PdfFileProperties>{
    private final ArrayAdapter THIS = this;
    private int layoutId;
    private Context context;
    private ArrayList<Boolean> listOfCheckedBox;
    private TextView txt;
    private ImageView img;
    private boolean isSearch = false;
    private SelectedListOfFileFragment selectedFrag;

    public SelectedFileListAdapter(Context context, int layoutId, ArrayList<PdfFileProperties>
            listFile, ArrayList<Boolean> listOfCheckedBox) {
        super(context, layoutId, listFile);
        this.context = context;
        this.listOfCheckedBox = listOfCheckedBox;
        setLayoutId(layoutId);
    }

    public void setIsSearch(boolean isSearch) {
        this.isSearch = isSearch;
    }

    private class ViewHolder{
        public TextView txt;
        public ImageView img;
        public int rowPosition;
        public CheckBox checkBox;
        public ViewHolder(TextView txt, ImageView img, int rowPosition,CheckBox checkBox) {
            this.txt = txt;
            this.img = img;
            this.rowPosition = rowPosition;
            this.checkBox = checkBox;
        }
    };
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theinflater = LayoutInflater.from(getContext());
        final SelectedFileListAdapter THIS = this;
        final View row = theinflater.inflate(layoutId, parent, false);
        //set Properties for View inside row
        //if the folder is ... then not show
        setViewProperties(row, position);
        CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkBox);
        img = (ImageView) row.findViewById(R.id.imageView_iconOfPdf);
        if (getItem(position).isFolder())
            img.setImageResource(R.drawable.folder);
        else
            img.setImageBitmap(getItem(position).getBitmap());

        ViewHolder myView = new ViewHolder(txt,img,position,checkBox);

        row.setTag(myView);

        //if user click select button, show select mode
        if (listOfCheckedBox.get(position) && listOfCheckedBox.size()>0){
            checkBox.setChecked(true);
            rowOnClick(row);
        }
        if (!getItem(position).getFileName().equals("...")) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setOnCheckedChangeListener
                    (new CompoundButton.OnCheckedChangeListener() {
                         @Override
                         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                             int position = ((ViewHolder) row.getTag()).rowPosition;
                             listOfCheckedBox.set(position, isChecked);
                             if (isChecked) rowOnClick(row);
                             else rowOnUndoClick(row);
                         }
                     }
                    );
            row.setOnClickListener(new View.OnClickListener() {
                boolean isChecked = false;

                @Override
                public void onClick(View v) {
                    isChecked = !isChecked;
                    ViewHolder vh = (ViewHolder) row.getTag();
                    vh.checkBox.setChecked(isChecked);
                    if (isChecked) rowOnClick(row);
                    else rowOnUndoClick(row);
                }
            });
        }
        return row;
    }

    private void setViewProperties(View row, int position) {
        PdfFileProperties pdfFile = getItem(position);
        //set title
        txt = (TextView) row.findViewById(R.id.textView_titleView);

        if (pdfFile.getSearchResultName()==null|| !isSearch)
            txt.setText(resizeText(pdfFile.getFileName()));
        else
            txt.setText(Html.fromHtml(pdfFile.getSearchResultName()));
        //set icon
        img = (ImageView) row.findViewById(R.id.imageView_iconOfPdf);
        img.setImageBitmap(pdfFile.getBitmap());

    }

    private String resizeText(String title) {

        if (title.length() >30)
            return title.substring(0, 30) + "...";
        else
            return title;
    }
    private void rowOnUndoClick(View row){
        ViewHolder vh = (ViewHolder) row.getTag();
        row.setBackgroundResource(R.drawable.shelfselect);
        vh.txt.setTextColor(Color.BLACK);
    }
    private void rowOnClick(View row){
        ViewHolder vh = (ViewHolder) row.getTag();
        row.setBackgroundResource(R.drawable.shelfselected);
        vh.txt.setTextColor(Color.parseColor("#FFA9FFFC"));
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }
}
