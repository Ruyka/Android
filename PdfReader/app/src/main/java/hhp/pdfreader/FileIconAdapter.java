package hhp.pdfreader;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hhphat on 7/27/2015.
 */
public class FileIconAdapter extends ArrayAdapter<PdfFileProperties> {

    private int layoutId;
    private int selectedPosition;
    private PdfFileProperties pdfFile;

    public FileIconAdapter(Context context, int layoutId, ArrayList<PdfFileProperties> listFile) {
        super(context, layoutId, listFile);
        setLayoutId(layoutId);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater theinflater = LayoutInflater.from(getContext());

        final View row = theinflater.inflate(layoutId, parent, false);
        pdfFile = getItem(position);
        //set title
        TextView txt = (TextView) row.findViewById(R.id.textView_titleView);
        txt.setText(pdfFile.getFileName());


        EssentialFunction.setFont(getContext(), txt, 20, "fonts/EBGaramond08-Regular.ttf", Gravity
                .CENTER);
        //set icon
        ImageView img = (ImageView) row.findViewById(R.id.imageView_iconOfPdf);
        img.setImageBitmap(pdfFile.getBitmap());
        //set favourite start
        ImageButton imgBtn = (ImageButton) row. findViewById(R.id.imageButton_favouriteImage);
        if (pdfFile.isFavourite()) {
            imgBtn.setImageResource(R.drawable.favourite_star);
        }
        else
            imgBtn.setImageResource(R.drawable.un_favourite_star);

        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCurrentPdfFileFavouriteProperties(row);
            }
        });
        return row;
    }

    private void changeCurrentPdfFileFavouriteProperties(View row) {
        ImageButton imgBtn = (ImageButton) row.findViewById(R.id.imageButton_favouriteImage);
        if (pdfFile.isFavourite()){
            pdfFile.setFavourite(false);

        }
        else {
            pdfFile.setFavourite(true);
        }
        notifyDataSetChanged();
    }
    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }
    public void updateCurSelectedPosition(int position) {
        selectedPosition = position;
    }

}

