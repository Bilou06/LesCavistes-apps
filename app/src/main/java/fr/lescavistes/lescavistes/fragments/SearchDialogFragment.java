package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Toast;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.persistent.RequestsContract;
import fr.lescavistes.lescavistes.persistent.RequestsContractDbHepler;
import fr.lescavistes.lescavistes.utils.FilterCursorWrapper;

/**
 * Created by Sylvain on 01/06/2015.
 */
public class SearchDialogFragment extends DialogFragment {

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
        query = model.getWhat();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.search_dialog_fragment, null);

        final AutoCompleteTextView searchET = (AutoCompleteTextView) v.findViewById(R.id.query_what);
        // do db queries in another thread
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void... voids) {
                RequestsContractDbHepler dbHepler = new RequestsContractDbHepler(MainApplication.getInstance());
                return dbHepler.getMostRecentQueries();
            }

            @Override
            protected void onPostExecute(final Cursor c) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(MainApplication.getInstance(),
                        android.R.layout.simple_spinner_dropdown_item,
                        c,
                        new String[]{RequestsContract.RequestWhat.COLUMN_NAME_QUERY},
                        new int[] { android.R.id.text1});

                adapter.setCursorToStringConverter(null);
                adapter.setStringConversionColumn(1);

                adapter.setFilterQueryProvider(new FilterQueryProvider() {
                    public Cursor runQuery(CharSequence str) {
                        RequestsContractDbHepler dbHepler = new RequestsContractDbHepler(MainApplication.getInstance());
                        return new FilterCursorWrapper(dbHepler.getMostRecentQueries(), str.toString(), 1);
                    }
                });

                searchET.setAdapter(adapter);
            }
        }.execute();




        Button clear = (Button) v.findViewById(R.id.clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchET.setText("");
            }
        });

        if(model.getWhat().length() != 0)
            searchET.setText(model.getWhat());
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

    public interface SearchDialogListener {
        public void onSearchClick(SearchDialogFragment dialog);
    }
}
