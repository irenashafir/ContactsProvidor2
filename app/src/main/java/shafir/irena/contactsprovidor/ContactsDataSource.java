package shafir.irena.contactsprovidor;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import shafir.irena.contactsprovidor.models.Contact;

/**
 * Created by irena on 04/08/2017.
 */

public class ContactsDataSource {
    //uri
    // column names
    // permission- dangerous (READS-CONTACTS)
    // objects to query the uri contentResolver


    public interface onContactsArrivedListener{
        void onContactArrived(List<Contact>data);
    }

    public static void getContactsAsync(final Context context, final onContactsArrivedListener listener){
        (new AsyncTask<Void, Void, List<Contact>>() {
            // do in background == work in background thread
            @Override
            protected List<Contact> doInBackground(Void... v) {
                return getContacts(context);
            }

            // reports the result from the UI Thread
            @Override
            protected void onPostExecute(List<Contact> contacts) {
                listener.onContactArrived(contacts);
            }
        }).execute();
    }



    private static ArrayList<Contact>  getContacts(Context context){
        ArrayList<Contact> contacts = new ArrayList<>();

        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;

        Cursor cursor = context.getContentResolver().query(contactUri,null,null,null,null);
        if (cursor == null || !cursor.moveToFirst()){
            return new ArrayList<>();
           // TODO: notify the listener- No result
        }
        do {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

            ArrayList<String> phones = getPhones(context, id);
            ArrayList<String> emails = getEmails(context, id);

            Contact contact = new Contact(name, phones, emails);
            contacts.add(contact);
        }
        while (cursor.moveToNext());

        cursor.close();
        return contacts;
    }


    private static ArrayList<String> getPhones(Context context, String id){
        // go to phones table -> acquire the phone
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        HashSet<String> phones = new HashSet<>();

        String colNumber = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String colContactID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;

        // String selection:
        String where = colContactID + "=?";

        //String[] selectionArgs
        String[] whereArgs = {id};

        Cursor phoneCursor = context.getContentResolver().query(phoneUri,null,where,whereArgs,null);
        if (phoneCursor == null || !phoneCursor.moveToFirst()){
            return new ArrayList<>();
        }
        do {
            String phone = phoneCursor.getString(phoneCursor.getColumnIndex(colNumber));
            System.out.println(phone);
            phones.add(phone);
        }
        while (phoneCursor.moveToNext());

        phoneCursor.close();
        ArrayList<String> phoneList = new ArrayList<>(phones);
        return phoneList;
    }


    private static ArrayList<String> getEmails(Context context, String id){

        Uri mailUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;

        String colEmail = ContactsContract.CommonDataKinds.Email.DATA;
        String colContactID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;

        HashSet<String> mailSet = new HashSet<>();

        String where = colContactID + "=?";
        String[] whereArgs = {id};
        Cursor mailCursor = context.getContentResolver().query(mailUri, null, where, whereArgs, null);
            if (mailCursor == null || !mailCursor.moveToFirst()){
                return new ArrayList<>();
            }
        do {
            // check the DB column names/ counts
//            int columnCount = mailCursor.getColumnCount();
//            for (int i = 0; i < columnCount; i++) {
//                String colI = mailCursor.getString(i);
//                String colName = mailCursor.getColumnName(i);
//            }

            String mail = mailCursor.getString(mailCursor.getColumnIndex(colEmail));
            mailSet.add(mail);

        }while (mailCursor.moveToNext());

        mailCursor.close();
        return new ArrayList<>(mailSet);
    }


}
