package fr.lescavistes.lescavistes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;


public class DisplayShopListActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String where = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        String what = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        String lng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        String lat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(where + '\n' + what + '\n' +lat+'\n'+lng);

        setContentView(textView);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openSearch(){

    }

    private void openSettings(){

    }
}
