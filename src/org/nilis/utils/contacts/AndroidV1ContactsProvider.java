package org.nilis.utils.contacts;

import java.util.LinkedList;
import java.util.List;

import org.nilis.utils.data.DataConsumer;
import org.nilis.utils.data.DataSensor;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.ContactMethodsColumns;
import android.provider.Contacts.OrganizationColumns;
import android.provider.Contacts.Organizations;
import android.provider.Contacts.People;
import android.provider.Contacts.PeopleColumns;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;

public class AndroidV1ContactsProvider implements DataSensor<List<Contact>> {
	private Context context = null;

	public AndroidV1ContactsProvider(final Context contextToUse) {
		context = contextToUse;
	}

	private List<String> getPhoneNumberByPeopleId(final long id) {
		final List<String> ret = new LinkedList<String>();
		final Cursor managedCursor = context.getContentResolver().query(Phones.CONTENT_URI,
				new String[] { PhonesColumns.NUMBER }, Phones.PERSON_ID + " = " + id, null, null);
		if (managedCursor != null && managedCursor.moveToFirst()) {
			final int numberIndex = managedCursor.getColumnIndex(PhonesColumns.NUMBER);
			do {
				ret.add(managedCursor.getString(numberIndex));
			} while (managedCursor.moveToNext());
		}
		if (managedCursor != null) {
			managedCursor.close();
		}
		return ret;
	}

	private List<String> getEmailByPeopleId(final long id) {
		final List<String> ret = new LinkedList<String>();
		final Cursor managedCursor = context.getContentResolver().query(ContactMethods.CONTENT_URI,
				new String[] { ContactMethodsColumns.DATA },
				"(" + ContactMethods.PERSON_ID + " = " + id + ") AND (" + ContactMethodsColumns.KIND + " = 1)", null,
				null);
		try {
			if (managedCursor != null && managedCursor.moveToFirst()) {
				final int dataIndex = managedCursor.getColumnIndex(ContactMethodsColumns.DATA);
				do {
					ret.add(managedCursor.getString(dataIndex));
				} while (managedCursor.moveToNext());
			}
		} finally {
			if (managedCursor != null) {
				managedCursor.close();
			}
		}
		return ret;
	}

	private List<String> getCompanyByPeopleId(final long id) {
		final List<String> ret = new LinkedList<String>();
		final Cursor cursor = context.getContentResolver().query(Organizations.CONTENT_URI,
				new String[] { OrganizationColumns.COMPANY }, OrganizationColumns.PERSON_ID + " = " + id, null, null);
		try {
			if (cursor != null && cursor.moveToFirst()) {
				final int companyIndex = cursor.getColumnIndex(OrganizationColumns.COMPANY);
				do {
					ret.add(cursor.getString(companyIndex));
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return ret;
	}

	@Override
	public void get(final DataConsumer<List<Contact>> consumer) {
		final List<Contact> ret = new LinkedList<Contact>();

		final Cursor cursor = context.getContentResolver().query(People.CONTENT_URI,
				new String[] { BaseColumns._ID, PeopleColumns.NAME }, null, null, null);
		Contact contact;
		try {
			if (cursor != null && cursor.moveToFirst()) {
				final int _id = cursor.getColumnIndex(BaseColumns._ID);
				final int _name = cursor.getColumnIndex(PeopleColumns.NAME);

				do {
					final long id = cursor.getLong(_id);
					contact = new Contact(cursor.getString(_name));
					contact.setPhoneNumbersList(getPhoneNumberByPeopleId(id));
					contact.setEmailsList(getEmailByPeopleId(id));
					contact.setCompaniesList(getCompanyByPeopleId(id));
					ret.add(contact);
				} while (cursor.moveToNext());
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		consumer.set(ret);
	}
}