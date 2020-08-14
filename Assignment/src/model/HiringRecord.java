package model;

import db.RecordTableContract;
import util.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HiringRecord {

	private String recordId;
	private DateTime rentDate;
	private DateTime estimatedReturnDate;
	private DateTime actualReturnDate;
	private double rentalFee;
	private double lateFee;
	private String customerId;
	private boolean returned;

	// Constructor
	public HiringRecord(String roomId, String customerId, DateTime rentDate, int numOfRentDay) {

		this.recordId = roomId + "_" + customerId + "_" + rentDate.getEightDigitDate();
		this.rentDate = rentDate;
		this.estimatedReturnDate = new DateTime(rentDate, numOfRentDay);
		this.customerId = customerId;
		this.returned = false;
	}

	// Constructor for create record from Database
	private HiringRecord(String recordId, DateTime rentDate, DateTime estimatedReturnDate,
						DateTime actualReturnDate, double rentalFee, double lateFee, String customerId, boolean returned) {

		this.recordId = recordId;
		this.rentDate = rentDate;
		this.estimatedReturnDate = estimatedReturnDate;
		this.actualReturnDate = actualReturnDate;
		this.rentalFee = rentalFee;
		this.lateFee = lateFee;
		this.customerId = customerId;
		this.returned = returned;
	}

	public static HiringRecord map(ResultSet resultSet) throws SQLException {

		if (resultSet == null) {
			return null;
		}

		String recordId = resultSet.getString(RecordTableContract.COL_ID);
		String customerId = resultSet.getString(RecordTableContract.COL_CUSTOMER_ID);
		DateTime rentDate = Utility.stringToDate(
				resultSet.getString(RecordTableContract.COL_RENT_DATE));
		DateTime estimatedReturnDate = Utility.stringToDate(
				resultSet.getString(RecordTableContract.COL_ESTIMATE_RETURN_DATE));
		boolean returned = resultSet.getBoolean(RecordTableContract.COL_RETURNED);

		DateTime actualReturnDate = null;
		double rentalFee = 0;
		double lateFee = 0;

		if (returned) {
			actualReturnDate = Utility.stringToDate(
					resultSet.getString(RecordTableContract.COL_ACTUAL_RETURN_DATE));
			rentalFee = resultSet.getDouble(RecordTableContract.COL_RENTAL_FEE);
			lateFee = resultSet.getDouble(RecordTableContract.COL_LATE_FEE);
		}

		return new HiringRecord(recordId, rentDate, estimatedReturnDate,
				actualReturnDate, rentalFee, lateFee, customerId, returned);
	}

	public String getRecordId() {
		return recordId;
	}

	public DateTime getRentDate() {
		return rentDate;
	}

	public DateTime getEstimatedReturnDate() {
		return estimatedReturnDate;
	}

	public DateTime getActualReturnDate() {
		return actualReturnDate;
	}

	public double getRentalFee() {
		return rentalFee;
	}

	public double getLateFee() {
		return lateFee;
	}

	public String getCustomerId() {
		return customerId;
	}

	public boolean getReturned() {
		return returned;
	}

	public void setRentalFee(double rentalFee) {
		this.rentalFee = rentalFee;
	}

	public void setLateFee(double lateFee) {
		this.lateFee = lateFee;
	}

	// get the estimated date
	public int getEstDays() {
		return DateTime.diffDays(estimatedReturnDate, rentDate);
	}

	@Override
	public String toString() {

		String latestRentalRecord = recordId + ":" + rentDate.getFormattedDate() + ":"
				+ estimatedReturnDate.getFormattedDate() + ":";

		if (returned) {
			latestRentalRecord += actualReturnDate.getFormattedDate() + ":" + String.format("%.2f", rentalFee) + ":"
					+ String.format("%.2f", lateFee);
		} else {
			latestRentalRecord += "none:none:none";
		}

		return latestRentalRecord;
	}

	public String getDetails() {

		String latestRentalDetail = "Record ID :                " + recordId + "\n" + "Rent Date :                "
				+ rentDate.getFormattedDate() + "\n" + "Estimated Return Date :    "
				+ estimatedReturnDate.getFormattedDate() + "\n";

		if (returned) {
			latestRentalDetail += "Actual Return Date :       " + actualReturnDate.getFormattedDate() + "\n"
					+ "Rental Fee:                " + String.format("%.2f", rentalFee) + "\n"
					+ "Late Fee:                  " + String.format("%.2f", lateFee);
		}

		return latestRentalDetail;
	}

	// update return information
	public void setReturnInfo(DateTime returnDate, double rentalFee, double lateFee) {
		actualReturnDate = returnDate;
		setRentalFee(rentalFee);
		setLateFee(lateFee);
		returned = true;
	}

}

