package com.chiragsavsani.contactmanager;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by chirag.savsani on 9/25/2015.
 */
public class AddContactActivity extends AppCompatActivity {

    EditText edtTxtContactName, edtTxtContactNumber, edtTxtContactEmail;
    ImageView imgContactPhoto;
    Button btnAddContact;
    View view;
    String imagePath = null;
    Uri uri;
    ExifInterface exif;
    boolean isButtonClicked = false;
    View snackView;
    Bitmap rotateBitmap;
    InputMethodManager inm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);

        view = findViewById(R.id.rootView);

        edtTxtContactName = (EditText) findViewById(R.id.edtTxtContactName);
        edtTxtContactNumber = (EditText) findViewById(R.id.edtTxtContactNumber);
        edtTxtContactEmail = (EditText) findViewById(R.id.edtTxtContactEmail);

        imgContactPhoto = (ImageView) findViewById(R.id.imgContactPhoto);

        btnAddContact = (Button) findViewById(R.id.btnAddContact);
        inm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        btnAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTxtContactName.getText().toString().trim().isEmpty()) {
                    Snackbar.make(v, "You must provide name.", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else if (edtTxtContactNumber.getText().toString().trim().isEmpty()) {
                    Snackbar.make(v, "You must provide number.", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
                } else {
                    isButtonClicked = true;
                    snackView = v;
                    inm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    addContact();
                }

            }
        });

        imgContactPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    private void selectImage() {
        final CharSequence[] option = {"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(
                AddContactActivity.this);
        builder.setTitle("Add Photo!");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.setItems(option, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (option[item].equals("Take Photo")) {
                    clickPhotoFromCamera();
                } else if (option[item].equals("Choose from Gallery")) {
                    uploadPhotoFromCamera();
                }
            }

        });
        builder.show();
    }

    private void clickPhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), imageFileName);
        uri = Uri.fromFile(imageStorageDir);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);

    }

    private void uploadPhotoFromCamera() {

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                imagePath = uri.getPath();
                displayImageBitmap(imagePath);
            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath,
                        null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                displayImageBitmap(picturePath);
            }
        }
    }

    public void displayImageBitmap(String image_path) {
        File mediaFile = new File(image_path);
        Bitmap myBitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath());
        int height = (myBitmap.getHeight() * 512 / myBitmap.getWidth());
        Bitmap scale = Bitmap.createScaledBitmap(myBitmap, 512, height, true);
        int rotate = 0;
        try {
            exif = new ExifInterface(mediaFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                rotate = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        rotateBitmap = Bitmap.createBitmap(scale, 0, 0, scale.getWidth(), scale.getHeight(), matrix, true);
        imgContactPhoto.setImageBitmap(rotateBitmap);
    }

    public void addContact() {
        ArrayList<ContentProviderOperation> insertOperation = new ArrayList<ContentProviderOperation>();
        int rawContactID = insertOperation.size();

        // Adding insert operation to operations list
        // For insert a new raw contact in the ContactsContract.RawContacts
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(rotateBitmap!=null) {    // If an image is selected successfully
            rotateBitmap.compress(Bitmap.CompressFormat.PNG, 75, stream);

            // For insert Photo in the ContactsContract.Data
            insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                    .withValue(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                    .withValue(ContactsContract.Data.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, stream.toByteArray())
                    .build());

            try {
                stream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // For insert display name in the ContactsContract.Data
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, edtTxtContactName.getText().toString())
                .build());
        // For insert Mobile Number in the ContactsContract.Data
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, edtTxtContactNumber.getText().toString())
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build());
        // For insert Work Email in the ContactsContract.Data
        insertOperation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactID)
                .withValue(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                .withValue(Email.ADDRESS, edtTxtContactEmail.getText().toString())
                .withValue(Email.TYPE, Email.TYPE_WORK)
                .build());
        try {
            // Executing all the insert operations as a single database transaction
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, insertOperation);
            if(isButtonClicked == true){
                edtTxtContactName.setText("");
                edtTxtContactNumber.setText("");
                edtTxtContactEmail.setText("");
                imgContactPhoto.setImageResource(R.drawable.default_contact);
                Snackbar.make(snackView, "Contact added successfully", Snackbar.LENGTH_INDEFINITE).setAction("Hide", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show();
            }else{
                Toast.makeText(getBaseContext(), "Contact is successfully added", Toast.LENGTH_SHORT).show();
                finish();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_contact_menu, menu);
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
        if (id == R.id.action_save) {
            if(edtTxtContactName.getText().toString().trim().isEmpty()){
                Toast.makeText(getApplicationContext(), "You must provide name.", Toast.LENGTH_LONG).show();

            }else if(edtTxtContactNumber.getText().toString().trim().isEmpty()){
                Toast.makeText(getApplicationContext(), "You must provide number.", Toast.LENGTH_LONG).show();

            }else{
                inm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                isButtonClicked = false;
                addContact();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
