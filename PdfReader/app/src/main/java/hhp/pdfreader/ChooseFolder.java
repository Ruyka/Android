package hhp.pdfreader;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class ChooseFolder extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
               // .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_choose_folder);
        final ListOfFileFragment listFrag = new ListOfFileFragment();
        listFrag.setOnTouchListener(null);
        ListOfFileFragment.isChooseDirecive = true;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.listOfDirective, listFrag);
        transaction.addToBackStack(null);
        transaction.commit();


        Button tmp = (Button) findViewById(R.id.okButtonToGetFolder);
        tmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListOfFileFragment.isChooseDirecive = false;
                PdfFileProperties tmp = listFrag.getListOfFileProperties().get(0);
                if (tmp.getFolderTreeDeep()<1)
                    Toast.makeText(getApplicationContext(),"Please choose a directive!",Toast
                            .LENGTH_SHORT).show();
                else{
                    Intent intent = new Intent();
                    intent.putExtra("PdfLink", PdfFileProperties.DirectiveDeep[tmp.getFolderTreeDeep
                            ()-1]);
                    setResult(RESULT_OK, intent);
                    finish();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_folder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
