package hhp.pdfreader;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by hhphat on 8/21/2015.
 */
public class AccountLoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View row = inflater.inflate(R.layout.layout_of_account_login_fragment,container,false);
        TextView textView = (TextView) row.findViewById(R.id.textView_signUp);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        String text = "<a href='http://www.google.com'> No account ? Please Sign Up</a>";
        textView.setText(Html.fromHtml(text));
        return row;
    }
}
