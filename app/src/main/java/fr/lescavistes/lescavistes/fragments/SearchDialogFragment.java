package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Model;

/**
 * Created by Sylvain on 01/06/2015.
 */
public class SearchDialogFragment extends DialogFragment {

    public interface SearchDialogListener {
        public void onSearchClick(SearchDialogFragment dialog);
    }

    SearchDialogListener mListener;

    private Model model;
    private String query;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SearchDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        model = MainApplication.getModel();
        query = model.what;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.search_dialog_fragment, null);
        final EditText searchET = (EditText) v.findViewById(R.id.query_what);

        Button clear = (Button) v.findViewById(R.id.clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchET.setText("");
            }
        });

        if(model.what.length() != 0)
            searchET.setText(model.what);
        builder.setView(v)
                // Add action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if(!searchET.getText().toString().equals(query)) {
                            query= searchET.getText().toString();
                            mListener.onSearchClick(SearchDialogFragment.this);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SearchDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    public String getQuery() {
        return query;
    }
}
