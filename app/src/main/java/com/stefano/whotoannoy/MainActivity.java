package com.stefano.whotoannoy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        populateAnnoyanceTable();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                annoy(view);
            }
        });
    }

    private void populateAnnoyanceTable(){
        // Create a RealmConfiguration which is to locate Realm file in package's "files" directory.
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(MainActivity.this).build();
        realm = Realm.getInstance(realmConfig);

        RealmResults<Annoyance> annoyances = realm.where(Annoyance.class).findAll().sort("callDate", Sort.ASCENDING);
        for (Annoyance annoyance : annoyances){
            addAnnoyanceRow(annoyance);
        }

    }

    private void addAnnoyanceRow(Annoyance annoyance){
        // Get the TableLayout
        TableLayout annoyanceTable = (TableLayout) findViewById(R.id.annoyance_table);

        // Create a TableRow
        TableRow row = new TableRow(MainActivity.this);
        row.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));

        // Create a TextView for contact name
        TextView labelName = new TextView(MainActivity.this);
        labelName.setText(annoyance.getContactName());
        labelName.setTextColor(Color.BLACK);
        labelName.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        row.addView(labelName);

        // Create a TextView for phone number
        TextView labelNumber = new TextView(MainActivity.this);
        labelNumber.setText(annoyance.getContactPhone());
        labelNumber.setTextColor(Color.BLACK);
        labelNumber.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        //row.addView(labelNumber);

        // Create a TextView for call date
        TextView labelDate = new TextView(MainActivity.this);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd HH:mm:ss");
        labelDate.setText(dateFormatter.format(annoyance.getCallDate()));
        labelDate.setTextColor(Color.BLACK);
        labelDate.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        row.addView(labelDate);

        // Add the TableRow to the TableLayout
        annoyanceTable.addView(row, 1, new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void annoy(View view) {
        // Kabloey
        System.out.println("Annoy a \"Friend\"");

        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},0);
            return;
        }

        Annoyance annoyance = whoToAnnoy();
        if (annoyance==null) return;

        // Persist annoyance data
        realm.beginTransaction();
        realm.copyToRealm(annoyance);
        realm.commitTransaction();

        addAnnoyanceRow(annoyance);

        call(annoyance);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Annoyance annoyance = whoToAnnoy();
                    if (annoyance==null) return;

                    // Persist annoyance data
                    realm.beginTransaction();
                    realm.copyToRealm(annoyance);
                    realm.commitTransaction();

                    addAnnoyanceRow(annoyance);

                    call(annoyance);

                }
                return;
            }
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
            }
        }
    }

    private void call(Annoyance annoyance){
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + annoyance.getContactPhone()));
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED ){
            startActivity(intent);
        }
    }

    private Annoyance whoToAnnoy(){
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},1);
            return null;
        }

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,"starred=?",new String[] {"1"}, null);
        int numContacts = phones.getCount();
        int lucky = (int) (Math.random() * numContacts);

        phones.moveToPosition(lucky);

        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

        phones.close();

        Annoyance annoyance = new Annoyance();
        annoyance.setCallDate(new Date());
        annoyance.setContactName(name);
        annoyance.setContactPhone(phoneNumber);

        return annoyance;
    }
}
