package com.chiragsavsani.contactmanager.adapter;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chiragsavsani.contactmanager.R;
import com.chiragsavsani.contactmanager.entities.ContactListDataEntities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chirag.savsani on 9/25/2015.
 */
public class ContactListViewAdapter extends RecyclerView.Adapter<ContactListViewAdapter.MyViewHolder> {

    List<ContactListDataEntities> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;
    int layoutResID;

    public ContactListViewAdapter(Context context,int layoutResID, List<ContactListDataEntities> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.layoutResID = layoutResID;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.contact_list_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        ContactListDataEntities current = data.get(position);
        holder.contactName.setText(current.getContactName());
        holder.contactNumber.setText(current.getContactNumber());
        holder.contactEmail.setText(current.getContactEmail());
        holder.contactImage.setImageBitmap(current.getContactImage());
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView contactName,contactNumber,contactEmail;
        ImageView contactImage;

        public MyViewHolder(View itemView) {
            super(itemView);
            contactName = (TextView) itemView.findViewById(R.id.txtContactName);
            contactNumber = (TextView) itemView.findViewById(R.id.txtContactNumber);
            contactImage = (ImageView)itemView.findViewById(R.id.imgDefaultContact);
            contactEmail = (TextView) itemView.findViewById(R.id.txtContactEmail);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View v) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setTitle("Delete Contact").setMessage("Do you really want to delete this contact?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String contactNo =  contactNumber.getText().toString();
                    int pos = getPosition();
                    removeAt(pos);
                    deleteContact(contactNo);
                }
            }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();
            return false;
        }
        void removeAt(int position) {
            data.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, data.size());
        }

        public void deleteContact(String number) {
            ContentResolver contactHelper = context.getContentResolver();
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            String[] args = new String[] { String.valueOf(getContactID(contactHelper, number)) };
            ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI).withSelection(RawContacts.CONTACT_ID + "=?", args).build());
            try {
                contactHelper.applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
        private long getContactID(ContentResolver contactHelper,String number) {
            Uri contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            String[] projection = {PhoneLookup._ID };
            Cursor cursor = null;
            try {
                cursor = contactHelper.query(contactUri, projection, null, null, null);
                if (cursor.moveToFirst()) {
                    int personID = cursor.getColumnIndex(PhoneLookup._ID);
                    return cursor.getLong(personID);
                }
                return -1;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }
            return -1;
        }
    }
}
