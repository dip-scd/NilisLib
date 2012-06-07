package org.nilis.utils.contacts;

import java.util.LinkedList;
import java.util.List;

public class Contact {
	public Contact(final String contactName) {
		name = contactName;
		phoneNumbers = new LinkedList<String>();
		emails = new LinkedList<String>();
		companies = new LinkedList<String>();
	}

	public String name() {
		return name;
	}

	public void addPhoneNumber(final String number) {
		phoneNumbers.add(number);
	}

	public void addEmail(final String email) {
		emails.add(email);
	}

	public void addCompany(final String company) {
		companies.add(company);
	}

	public List<String> phoneNumbers() {
		return phoneNumbers;
	}

	public List<String> emails() {
		return emails;
	}

	public List<String> companies() {
		return companies;
	}

	public void setPhoneNumbersList(final List<String> numbers) {
		phoneNumbers = numbers;
	}

	public void setEmailsList(final List<String> emailsList) {
		emails = emailsList;
	}

	public void setCompaniesList(final List<String> companiesList) {
		companies = companiesList;
	}

	protected String name;
	protected List<String> phoneNumbers;
	protected List<String> emails;
	protected List<String> companies;
}
