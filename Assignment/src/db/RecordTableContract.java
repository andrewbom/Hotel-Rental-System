package db;


public class RecordTableContract {

    public static final String TABLE_NAME = "records";
    public static final String COL_ID = "record_id";
    public static final String COL_ROOM_ID = "room_id";
    public static final String COL_CUSTOMER_ID = "customer_id";
    public static final String COL_RENT_DATE = "rent_date";
    public static final String COL_ESTIMATE_RETURN_DATE = "estimate_return_date";
    public static final String COL_ACTUAL_RETURN_DATE = "actual_return_date";
    public static final String COL_RENTAL_FEE = "rental_fee";
    public static final String COL_LATE_FEE = "late_fee";
    public static final String COL_RETURNED = "returned";

    static final String CREATE_RECORD_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + " (" +
                    COL_ID + " VARCHAR(50) NOT NULL," +
                    COL_ROOM_ID + " VARCHAR(50) NOT NULL," +
                    COL_CUSTOMER_ID + " VARCHAR(50) NOT NULL," +
                    COL_RENT_DATE + " VARCHAR(50) NOT NULL," +
                    COL_ESTIMATE_RETURN_DATE + " VARCHAR(50) NOT NULL," +
                    COL_ACTUAL_RETURN_DATE + " VARCHAR(50)," +
                    COL_RENTAL_FEE + " DOUBLE," +
                    COL_LATE_FEE + " DOUBLE," +
                    COL_RETURNED + " BOOLEAN," +
                    "PRIMARY KEY (" + COL_ID + ")" +
                    ");";
}
