package hhp.pdfreader;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

/**
 * Created by hhphat on 7/29/2015.
 */
public class ControlPanelListFragment extends ListFragment {
    private static final String FILES ="Files";
    private static final String DIRECTIVE ="Directive";
    private static final String HISTORY ="History";
    private static final String FAVOURITE ="Favorites";
    private static final String SEARCH ="Search";
    private static final String CREDIT ="Credit";
    private FullscreenActivity fullscreenActivity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String[] listItems = new String[]{FILES,DIRECTIVE,HISTORY,FAVOURITE,CREDIT};
        ControlPanelListAdapter adapter = new ControlPanelListAdapter(getActivity(),listItems);

        this.setListAdapter(adapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String tag =(String) v.getTag();

        if (tag.equals(FILES)|| tag.equals(HISTORY)||tag.equals(FAVOURITE)|| tag.equals(DIRECTIVE)){
            ListOfFileFragment listFrag = fullscreenActivity.getListFrag();
            ListFragmentShowType(listFrag, tag);
            fullscreenActivity.setControlPanelVisibility(false);

        }
        else if (tag.equals("Account")) {
            fullscreenActivity.setAccountLoginVisibility();
        }
        else  if (tag.equals(CREDIT)){
            Intent intent = new Intent(getActivity(),ShowSettings.class);
            startActivity(intent);
        }
    }

    private void ListFragmentShowType(ListOfFileFragment listFrag, String tag) {
        if (tag.equals(FILES))
            listFrag.showAllFiles();
        else if (tag.equals(DIRECTIVE)){
            listFrag.showDirective();
        }else if (tag.equals(FAVOURITE)){
            listFrag.showFavorites();
        }
        else if (tag.equals(HISTORY)){
            listFrag.showHistory();
        }

    }

    public void addMainView(FullscreenActivity fullscreenActivity) {
        this.fullscreenActivity = fullscreenActivity;
    }

}
