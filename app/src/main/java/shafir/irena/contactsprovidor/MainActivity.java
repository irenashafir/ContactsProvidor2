package shafir.irena.contactsprovidor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import shafir.irena.contactsprovidor.models.Contact;

public class MainActivity extends AppCompatActivity implements ContactsDataSource.onContactsArrivedListener {

    private static final int RC_CONTACTS = 0;
    RecyclerView rvContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContacts();
            }
        });
    }

    private void getContacts() {
        // check permission -> if no- RQ it.
        if (!checkContactsPermission()){
            requestContactPermission();
            return;
        }
        ContactsDataSource.getContactsAsync(this, this);
    }

    @Override
    public void onContactArrived(List<Contact> data) {
        //view holder + adapter
        rvContacts = (RecyclerView) findViewById(R.id.rvContacts);
        rvContacts.setAdapter(new ContactsAdapter(this, data));
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }

    static class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
        // adapter properties:
        private Context context;
        private List <Contact> data;

        public ContactsAdapter(Context context, List<Contact> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.contact_item, parent, false);
            return new ContactViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ContactViewHolder holder, int position) {
            Contact contact = data.get(position);
            holder.model = contact;
            holder.context = context;

            holder.tvName.setText(contact.getName());
            holder.tvPhone.setText(contact.getPhones().toString());
            holder.tvPhone.setText(contact.getEmails().toString());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ContactViewHolder extends RecyclerView.ViewHolder {
            TextView tvName;
            TextView tvPhone;
            TextView tvEmail;

            Context context;
            Contact model;

            public ContactViewHolder(View itemView) {
                super(itemView);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvPhone = (TextView) itemView.findViewById(R.id.tvPhone);
                tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            }
        }
    }


    private boolean checkContactsPermission() {
        int permissionResult = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return  permissionResult == PackageManager.PERMISSION_GRANTED;
    }


    private void requestContactPermission() {
        String[] requestPermissions = {Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this,requestPermissions,RC_CONTACTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_CONTACTS &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getContacts();
        }
        else {
            Toast.makeText(this, "No Permission..", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri packageUri = Uri.parse("package:" + getPackageName());
            intent.setData(packageUri);
            startActivity(intent);
        }
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


}
