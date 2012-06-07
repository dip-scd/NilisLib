package org.nilis.utils.contacts;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nilis.utils.data.DataConsumer;
import org.nilis.utils.data.DataSensor;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactsProvider implements DataSensor<List<Contact>> {

	Context context;

	public ContactsProvider(final Context contextToUse) {
		context = contextToUse;
	}

	@Override
	public void get(final DataConsumer<List<Contact>> consumer) {
		final ArrayList<Contact> ret = new ArrayList<Contact>();
		final ContentResolver cr = context.getContentResolver();
		final String projection[] = new String[] { ContactsContract.Contacts.LOOKUP_KEY,
				ContactsContract.Contacts.DISPLAY_NAME };
		final Cursor contactsCursor = cr.query(ContactsContract.Contacts.CONTENT_URI, projection, null, null, null);
		try {
			if (contactsCursor.moveToFirst()) {
				Contact contact;
				do {
					final String name = contactsCursor.getString(1);
					contact = new Contact(name);
					contact.setPhoneNumbersList(lookupPhoneNumbers(contactsCursor.getString(0)));
					contact.setEmailsList(lookupEmails(contactsCursor.getString(0)));
					contact.setCompaniesList(lookupCompanies(contactsCursor.getString(0)));
					ret.add(contact);
				} while (contactsCursor.moveToNext());
			}
		} finally {
			contactsCursor.close();
		}

		consumer.set(ret);
	}

	protected List<String> lookupPhoneNumbers(final String lookupKey) {
		return lookupForContactRelatedStringData(lookupKey, Phone.CONTENT_URI, Phone.NUMBER);
	}

	protected List<String> lookupEmails(final String lookupKey) {
		return lookupForContactRelatedStringData(lookupKey, Email.CONTENT_URI, Email.DATA);
	}

	protected List<String> lookupCompanies(final String lookupKey) {
		final List<String> ret = new LinkedList<String>();

		final Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
				new String[] { ContactsContract.Contacts.LOOKUP_KEY, Organization.COMPANY },
				ContactsContract.Contacts.LOOKUP_KEY + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
				new String[] { lookupKey, Organization.CONTENT_ITEM_TYPE }, null);
		try {
			while (cursor.moveToNext()) {
				final String data = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
				if (!ret.contains(data)) {
					ret.add(data);
				}
			}
		} finally {
			cursor.close();
		}

		return ret;
	}

	protected List<String> lookupForContactRelatedStringData(final String lookupKey, final Uri contentUri,
			final String dataColumn) {
		final List<String> ret = new LinkedList<String>();

		final Cursor cursor = context.getContentResolver().query(contentUri, null,
				ContactsContract.Contacts.LOOKUP_KEY + " = ?", new String[] { lookupKey }, null);
		try {
			while (cursor.moveToNext()) {
				final String data = cursor.getString(cursor.getColumnIndex(dataColumn));
				if (!ret.contains(data)) {
					ret.add(data);
				}
			}
		} finally {
			cursor.close();
		}

		return ret;
	}
}