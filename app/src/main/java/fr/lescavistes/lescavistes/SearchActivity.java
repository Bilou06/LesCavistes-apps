package fr.lescavistes.lescavistes;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class SearchActivity extends ActionBarActivity {
    public final static String WHERE_MESSAGE = "fr.lescavistes.lescavistes.WHERE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

    /** Called when the user clicks the button */
    public void searchWhere(View view){
        Intent intent = new Intent(this, DisplayShopListActivity.class);
        EditText editText = (EditText) findViewById(R.id.query_where);
        String message = editText.getText().toString();
        intent.putExtra(WHERE_MESSAGE, message);
        startActivity(intent);
    }
}
