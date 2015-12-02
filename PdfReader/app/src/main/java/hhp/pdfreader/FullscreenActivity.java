package hhp.pdfreader;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class FullscreenActivity extends Activity {
    private static final int REQ_CODE_OPEN_PDF = 125;
    private static final int REQ_CODE_TO_MOVE_FOLDER = 123;
    private static final int REQ_CODE_TO_CREATE_FOLDER = 124;

    private SelectedListOfFileFragment selectedFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isControlPanelOpen= false;
        getScreenSize();
        findNecessaryView();
        setUpViewPosition();
        setEventForControl();
    }

    private void getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth=dm.widthPixels;
        screenHeight=dm.heightPixels;
    }

    private void setSubControlPosition(){
        controlPanelView.setTranslationX(-max(screenWidth, screenHeight));
        if (editControlView.getHeight()>0){
            controlPanelView.setTranslationY(editControlView.getHeight());
            editControlView.setTranslationY(-editControlView.getHeight());
        }
        if (sortControlView.getHeight()>0)
            sortControlView.setTranslationY(-sortControlView.getHeight());

    }

    private int max(int screenWidth, int screenHeight) {
        return (screenHeight>screenWidth? screenHeight: screenWidth);
    }

    private void setUpViewPosition() {
        int mControlsHeight = controlsView.getHeight();
        if (mControlsHeight>0)
            controlsView.setTranslationY(-mControlsHeight);
        contentView.setTranslationY(0);
        setSubControlPosition();
    }

    private void setHeightOfListOfFiles(int height) {

        if (selectedFragment == null ) {
            ViewGroup.LayoutParams params = listFrag.getListView().getLayoutParams();
            params.height = height;
            listFrag.getListView().setLayoutParams(params);
        }
        else{
            ViewGroup.LayoutParams params = selectedFragment.getListView().getLayoutParams();
            params.height = height;
            selectedFragment.getListView().setLayoutParams(params);
        }

        if (editControlView.getHeight()>0){
            editControlView.setTranslationY(-editControlView.getHeight());
        }
        if (sortControlView.getHeight()>0)
            sortControlView.setTranslationY(-sortControlView.getHeight());
        mainView.invalidate();
    }

    private void setControlsVisibility(boolean visible) {
        int mControlsHeight = controlsView.getHeight();
        controlsView.bringToFront();
        int mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        if (!visible){
            controlsView.animate().translationY(-mControlsHeight);
            sortControlView.setTranslationY(-mControlsHeight);
            editControlView.setTranslationY(-mControlsHeight);
            contentView.animate().translationY(0);
            setHeightOfListOfFiles(screenHeight);
        }
        else {
            setSubControlPosition();
            controlsView.animate().translationY(0).setDuration(mShortAnimTime);
            contentView.animate().translationY(mControlsHeight).setDuration(mShortAnimTime);
            setHeightOfListOfFiles(screenHeight-mControlsHeight);
        }
        mainView.invalidate();
    }
    private void setSubControlVisibility(boolean visible, View subControlView) {
        int mControlsHeight = controlsView.getHeight();
        int mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        subControlView.animate().translationY(visible ? mControlsHeight : -mControlsHeight);
        if (visible) setControlPanelVisibility(false);
        contentView.animate().translationY(visible ? mControlsHeight + subControlView.getHeight() :
                mControlsHeight).setDuration(mShortAnimTime);
        setHeightOfListOfFiles(visible ? screenHeight - subControlView.getHeight() * 2 :
                screenHeight - subControlView.getHeight());

    }

    public void setControlPanelVisibility(boolean visible){
        isControlPanelOpen = visible;
        int mShortAnimTime = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        controlPanelView.animate()
                .translationX(visible ? 0: -screenWidth )
                .setDuration(mShortAnimTime);
    }

    private void setEventForControl() {
        edit.setOnClickListener(EdiButtontOnClick);

        controlPanel.setOnClickListener(ControlPanelButtontOnClick);

        sort.setOnClickListener(SortButtontOnClick);

        //button
        sortByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFrag.getListOfFileProperties().sortFileAs(ListOfPdfProperties.PdfSortCase.SORTBYDATE);
                listFrag.applyChange();
            }
        });

       //button
        sortByName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listFrag.getListOfFileProperties().sortFileAs(ListOfPdfProperties.PdfSortCase.SORTBYNAME);
                listFrag.applyChange();
            }
        });
        editSelect.setOnClickListener(EditSelectOnClick);
        editDelete.setOnClickListener(EditDeleteOnClick);
        findViewById(R.id.button_ok_to_rename_file).setOnClickListener(OkToRenameOnClick);
        editRename.setOnClickListener(EditRenameOnClick);
        editMove.setOnClickListener(EditMoveOnClick);
        editNewFolder.setOnClickListener(EditNewFolderOnClick);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    private void findNecessaryView() {
        //remove action bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);
        //find views
        edit = (ImageButton) findViewById(R.id.button_edit);
        controlPanel = (ImageButton) findViewById(R.id.button_control_panel);
        sort = (ImageButton) findViewById(R.id.button_sort);
        controlsView = findViewById(R.id.fullscreen_content_controls);
        contentView =  findViewById(R.id.content);
        editControlView = findViewById(R.id.fullscreen_edit_control);
        sortControlView = findViewById(R.id.fullscreen_sort_control);
        mainView = findViewById(R.id.MainView);
        sortByDate = (Button) findViewById(R.id.button_sort_by_last_viewed);
        sortByName = (Button) findViewById(R.id.button_sort_by_name);
        editDelete = (ImageButton) findViewById(R.id.imageButton_delete);
        editMove = (ImageButton) findViewById(R.id.imageButton_move);
        editRename = (ImageButton) findViewById(R.id.imageButton_rename);
        editSelect = (ImageButton) findViewById(R.id.imageButton_select);
        editNewFolder = (ImageButton) findViewById(R.id.imageButton_addNewFolder);
        controlPanelView = findViewById(R.id.fullscreen_control_panel);
        renameView = findViewById(R.id.rename_view);
        searchControlView = findViewById(R.id.search_control);
        panelFrag = (ControlPanelListFragment)
                getFragmentManager().findFragmentById(R.id.fragment_control_panel_list_fragment);
        panelFrag.addMainView(this);

        EditText tmp = (EditText) findViewById(R.id.editText_Search);
        tmp.addTextChangedListener(new TextWatcher() {
            ListOfPdfProperties list = null;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (selectedFragment==null) {
                    if (list == null) list = listOfPdfProperties;
                    listFrag.showSearch(list,s.toString());
                }
                else {
                    if (list == null) list = selectedFragment.getListOfFileProperties();
                    selectedFragment.showSearch(list,s.toString());
                }

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        listFrag = new ListOfFileFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fullscreen_content, listFrag);
        transaction.addToBackStack(null);
        transaction.commit();

        listOfPdfProperties = listFrag.getListOfFileProperties();
        listFrag.setOnTouchListener(ScrollScreen);
    }
    View.OnTouchListener ScrollScreen = new View.OnTouchListener() {
        int move = 0;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    direction.addCoordinates(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    direction.addCoordinates(event.getX(), event.getY());
                    if (move++ < 2) return true;

                    switch (direction.getDirection()) {
                        case OnTouchTracking.GO_DOWN:
                            setControlsVisibility(true);
                            break;
                        case OnTouchTracking.GO_UP:
                            setControlsVisibility(false);
                            break;
                    }
                    return false;
                case MotionEvent.ACTION_UP:
                    move = 0;
                    direction.addCoordinates(event.getX(), event.getY());
                    switch (direction.getDirection()) {
                        case OnTouchTracking.SAME_POSITION:
                            return false;
                    }

            }
            return true;
        }
    };
    public void setAccountLoginVisibility() {
        if (findViewById(R.id.loginView).getVisibility() == View.INVISIBLE)
            findViewById(R.id.loginView).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.loginView).setVisibility(View.INVISIBLE);
    }
    public SelectedListOfFileFragment getSelelectedFragment(){
        return selectedFragment;
    }
    public ListOfFileFragment getListFrag() {
        return listFrag;
    }

    public void disableSelectedFragment() {
        selectedFragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.popBackStack();
    }
    private View.OnClickListener EdiButtontOnClick = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            editControlView.bringToFront();
            edit.setSoundEffectsEnabled(true);
            controlsView.bringToFront();
            mainView.invalidate();
            if (editViewClose) {
                sortViewClose = false;
                sort.setSoundEffectsEnabled(false);
                sort.performClick();
            }
            setSubControlVisibility(editViewClose, editControlView);
            editViewClose = !editViewClose;
        }
    };
    private View.OnClickListener ControlPanelButtontOnClick = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            controlPanelView.bringToFront();
            if (!isControlPanelOpen) {
                setControlPanelVisibility(true);
                editViewClose = false;
                editSelect.setSoundEffectsEnabled(false);
                if (selectedFragment != null)
                    editSelect.performClick();
            }
            else
                setControlPanelVisibility(false);
            mainView.invalidate();
        }
    };

    private View.OnClickListener SortButtontOnClick = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            sort.setSoundEffectsEnabled(true);
            sortControlView.bringToFront();
            controlsView.bringToFront();
            mainView.invalidate();
            if (sortViewClose) {
                editViewClose = false;
                edit.setSoundEffectsEnabled(false);
                edit.performClick();
            }
            setSubControlVisibility(sortViewClose, sortControlView);
            sortViewClose = (!sortViewClose);
        }
    };
    private boolean editSelectClose = true;
    private View.OnClickListener EditSelectOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            editSelect.setSoundEffectsEnabled(true);
            if (editSelectClose) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                selectedFragment = new SelectedListOfFileFragment();
                listOfPdfProperties = listFrag.getListOfFileProperties();
                selectedFragment.setList(listOfPdfProperties);
                fragmentTransaction.replace(R.id.fullscreen_content, selectedFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            } else {
                selectedFragment = null;
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.popBackStack();
                //listFrag.Refresh();
                listFrag.applyChange();
            }
            editSelectClose = (!editSelectClose);
        }
    };
    private View.OnClickListener EditDeleteOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedFragment != null) {
                selectedFragment.DeleteSelectedItems();
                selectedFragment.applyChange();
            } else
                Toast.makeText(getApplicationContext(), "You have to select something!",
                        Toast.LENGTH_LONG).show();
        }
    };
    private View.OnClickListener OkToRenameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            renameView.setVisibility(View.GONE);
            EditText tmp = (EditText) findViewById(R.id.edit_text_enter_new_pdf_name);
            selectedFragment.RenameSelectedItems(tmp.getText().toString());
            tmp.setText("");
            selectedFragment.erasedCheckedItems();
            selectedFragment.applyChange();
        }
    };



    private View.OnClickListener EditRenameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedFragment != null) {
                if (selectedFragment.countItemSelected() > 1)
                    Toast.makeText(getApplicationContext(), "Please choose 1 item", Toast.
                            LENGTH_SHORT).show();
                else {
                    renameView.setVisibility(View.VISIBLE);
                    renameView.bringToFront();
                    mainView.invalidate();
                }
            } else
                Toast.makeText(getApplicationContext(), "You have to select something!",
                        Toast.LENGTH_LONG).show();
        }
    };
    private View.OnClickListener EditNewFolderOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            renameView.setVisibility(View.VISIBLE);
            renameView.bringToFront();
            mainView.invalidate();
            findViewById(R.id.button_ok_to_rename_file).setOnClickListener
                    (OkToNameNewFolderOnClick);

        }
    };
    private View.OnClickListener OkToNameNewFolderOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.button_ok_to_rename_file).setOnClickListener(OkToRenameOnClick);
            renameView.setVisibility(View.GONE);

            EditText tmp = (EditText) findViewById(R.id.edit_text_enter_new_pdf_name);
            editNewFolder.setTag(tmp.getText().toString());
            tmp.setText("");

            Intent intent = new Intent(getApplicationContext(), ChooseFolder.class);
            startActivityForResult(intent, REQ_CODE_TO_CREATE_FOLDER);
        }
    };
    private View.OnClickListener EditMoveOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (selectedFragment != null) {
                Intent intent = new Intent(getApplicationContext(), ChooseFolder.class);
                startActivityForResult(intent, REQ_CODE_TO_MOVE_FOLDER);

            } else
                Toast.makeText(getApplicationContext(), "You have to select something!",
                        Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_TO_MOVE_FOLDER) {
            // came back from SecondActivity
            String tmp = data.getStringExtra("PdfLink");
            selectedFragment.moveFile(tmp);
            editSelect.setSoundEffectsEnabled(false);
            editSelect.performClick();
        }
        else if (requestCode == REQ_CODE_TO_CREATE_FOLDER){
            String tmp = data.getStringExtra("PdfLink");
            File file = new File(tmp,(String)editNewFolder.getTag());
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Toast.makeText(getApplicationContext(),"Error in creating new folder",Toast
                            .LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(),"Succesfully create folder in "+tmp +'/'+
                            (String)editNewFolder.getTag(),Toast.LENGTH_SHORT).show();
            }
            editNewFolder.setTag("");
        }else if(requestCode == REQ_CODE_OPEN_PDF){
            int pos = data.getIntExtra("position", -1);
            int lastPage = data.getIntExtra("last_page", 0);
            if(pos != -1)
                listOfPdfProperties.get(pos).setLastPageViewed(lastPage);
        }

    }

    boolean sortViewClose = true;
    boolean editViewClose = true;

    private ImageButton sort;
    private ImageButton controlPanel;
    private ImageButton edit;
    private Button sortByName;
    private Button sortByDate;
    private View mainView;
    private View controlsView;
    private View contentView;
    private View editControlView;
    private View sortControlView;
    private ImageButton editSelect;
    private ImageButton editRename;
    private ImageButton editMove;
    private ImageButton editDelete;
    private ImageButton editNewFolder;
    private View controlPanelView;
    private View renameView;
    private int screenWidth;
    private int screenHeight;
    private OnTouchTracking direction = new OnTouchTracking();
    private boolean isControlPanelOpen;
    private ListOfFileFragment listFrag;
    private ListOfPdfProperties listOfPdfProperties;
    private ControlPanelListFragment panelFrag;
    private View searchControlView;
    public View getSearchControlView(){
        return searchControlView;
    }


}


