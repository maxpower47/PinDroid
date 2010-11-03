package com.android.droidlicious.platform;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.StatusUpdates;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.client.User;
import com.android.droidlicious.R;

import java.util.List;
import java.util.ArrayList;

/**
 * Class for managing contacts sync related mOperations
 */
public class ContactManager {
    /**
     * Custom IM protocol used when storing status messages.
     */
    public static final String CUSTOM_IM_PROTOCOL = "Droidlicious";
    private static final String TAG = "ContactManager";

    /**
     * Synchronize raw contacts
     * 
     * @param context The context of Authenticator Activity
     * @param account The username for the account
     * @param users The list of users
     */
    public static synchronized void syncContacts(Context context,
        String account, List<User> users) {
        String userName;
        long rawContactId = 0;
        final ContentResolver resolver = context.getContentResolver();
        
        List<Long> currentContacts = lookupAllContacts(resolver);
        
        final BatchOperation batchOperation =
            new BatchOperation(context, resolver);
        Log.d(TAG, "In SyncContacts");
        for (final User user : users) {
            userName = user.getUserName();
            // Check to see if the contact needs to be inserted or updated
            rawContactId = lookupRawContact(resolver, userName);
            if (rawContactId == 0) {
                // add new contact
                Log.d(TAG, "In addContact");
                addContact(context, account, user, batchOperation);
            } else{
            	currentContacts.remove(rawContactId);
            }
            // A sync adapter should batch operations on multiple contacts,
            // because it will make a dramatic performance difference.
            if (batchOperation.size() >= 50) {
                batchOperation.execute();
            }
        }
        
        for(final Long l : currentContacts){
        	Log.d(TAG, "Deleting contact");
        	deleteContact(context, l, batchOperation);
        }
        
        batchOperation.execute();
    }

    /**
     * Add a list of status messages to the contacts provider.
     * 
     * @param context the context to use
     * @param accountName the username of the logged in user
     * @param statuses the list of statuses to store
     */
    public static void insertStatuses(Context context, String username,
        List<User.Status> list) {
        final ContentValues values = new ContentValues();
        final ContentResolver resolver = context.getContentResolver();
        
        final ArrayList<String> processedUsers = new ArrayList<String>();
        
        final BatchOperation batchOperation =
            new BatchOperation(context, resolver);
        for (final User.Status status : list) {
            // Look up the user's sample SyncAdapter data row
            final String userName = status.getUserName();
            
            if(!processedUsers.contains(userName)){
	            final long profileId = lookupProfile(resolver, userName);
	
	            // Insert the activity into the stream
	            if (profileId > 0) {
	                values.put(StatusUpdates.DATA_ID, profileId);
	                values.put(StatusUpdates.STATUS, status.getStatus());
	                values.put(StatusUpdates.PROTOCOL, Im.PROTOCOL_CUSTOM);
	                values.put(StatusUpdates.CUSTOM_PROTOCOL, CUSTOM_IM_PROTOCOL);
	                values.put(StatusUpdates.IM_ACCOUNT, username);
	                values.put(StatusUpdates.IM_HANDLE, status.getUserName());
	                values.put(StatusUpdates.STATUS_TIMESTAMP, status.getTimeStamp().getTime());
	                values.put(StatusUpdates.STATUS_RES_PACKAGE, context
	                    .getPackageName());
	                values.put(StatusUpdates.STATUS_ICON, R.drawable.icon);
	                values.put(StatusUpdates.STATUS_LABEL, R.string.label);

	                batchOperation
	                    .add(ContactOperations.newInsertCpo(
	                        StatusUpdates.CONTENT_URI, true).withValues(values)
	                        .build());
	                // A sync adapter should batch operations on multiple contacts,
	                // because it will make a dramatic performance difference.
	                if (batchOperation.size() >= 50) {
	                    batchOperation.execute();
	                }
	            }
	            
	            processedUsers.add(userName);
            }
        }
        batchOperation.execute();
    }

    /**
     * Adds a single contact to the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param accountName the account the contact belongs to
     * @param user the sample SyncAdapter User object
     */
    private static void addContact(Context context, String accountName,
        User user, BatchOperation batchOperation) {
        // Put the data in the contacts provider
        final ContactOperations contactOp =
            ContactOperations.createNewContact(context, user.getUserName(),
                accountName, batchOperation);
        contactOp.addName(user.getUserName()).addProfileAction(user.getUserName());
    }

    /**
     * Deletes a contact from the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     */
    private static void deleteContact(Context context, long rawContactId,
        BatchOperation batchOperation) {
        batchOperation.add(ContactOperations.newDeleteCpo(
            ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
            true).build());
    }


    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     * 
     * @param context the Authenticator Activity context
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static long lookupRawContact(ContentResolver resolver, String userName) {
        long authorId = 0;
        final Cursor c =
            resolver.query(RawContacts.CONTENT_URI, UserIdQuery.PROJECTION,
                UserIdQuery.SELECTION, new String[] {userName},
                null);
        try {
            if (c.moveToFirst()) {
                authorId = c.getLong(UserIdQuery.COLUMN_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return authorId;
    }

    /**
     * Returns the Data id for a sample SyncAdapter contact's profile row, or 0
     * if the sample SyncAdapter user isn't found.
     * 
     * @param resolver a content resolver
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the profile Data row id, or 0 if not found
     */
    private static long lookupProfile(ContentResolver resolver, String userName) {
        long profileId = 0;
        final Cursor c =
            resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION,
                ProfileQuery.SELECTION, new String[] {userName},
                null);
        try {
            if (c != null && c.moveToFirst()) {
                profileId = c.getLong(ProfileQuery.COLUMN_ID);
                Log.d("ProfileLookup", Long.toString(c.getLong(ProfileQuery.COLUMN_ID)));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return profileId;
    }
    
    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     * 
     * @param context the Authenticator Activity context
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static List<Long> lookupAllContacts(ContentResolver resolver) {
        List<Long> result = new ArrayList<Long>();
        final Cursor c =
            resolver.query(RawContacts.CONTENT_URI, AllUsersQuery.PROJECTION,
            		AllUsersQuery.SELECTION, null, null);
        try {
            while (c.moveToNext()) {
                result.add(c.getLong(AllUsersQuery.COLUMN_ID));
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    private interface ProfileQuery {
        public final static String[] PROJECTION = new String[] {Data._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
            Data.MIMETYPE + "='" + DroidliciousSyncAdapterColumns.MIME_PROFILE
                + "' AND " + DroidliciousSyncAdapterColumns.DATA_PID + "=?";
    }
    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    private interface UserIdQuery {
        public final static String[] PROJECTION = new String[] {RawContacts._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
            RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                + RawContacts.SOURCE_ID + "=?";
    }
    
    /**
     * Constants for a query to find all Droidlicious contacts
     */
    private interface AllUsersQuery {
        public final static String[] PROJECTION = new String[] {RawContacts._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
            RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "'";
    }
}
