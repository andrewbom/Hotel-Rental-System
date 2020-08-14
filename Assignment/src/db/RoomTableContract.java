package db;

public class RoomTableContract {

    public static final String TABLE_NAME = "rooms";
    public static final String COL_ID = "room_id";
    public static final String COL_NUM_OF_BED = "num_of_bed";
    public static final String COL_SUMMARY = "summary";
    public static final String COL_TYPE = "type";
    public static final String COL_STATUS = "status";
    public static final String COL_LATEST_MAINTENANCE_DATE = "latest_maintenance_date";
    public static final String COL_IMAGE = "image";

    static final String CREATE_ROOM_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME + " (" +
                    COL_ID + " VARCHAR(50) NOT NULL," +
                    COL_NUM_OF_BED + " INT NOT NULL," +
                    COL_SUMMARY + " VARCHAR(300) NOT NULL," +
                    COL_TYPE + " INT NOT NULL," +
                    COL_STATUS + " INT NOT NULL," +
                    COL_LATEST_MAINTENANCE_DATE + " VARCHAR(50)," +
                    COL_IMAGE + " VARCHAR(50)," +
                    "PRIMARY KEY (" + COL_ID + ")" +
                    ");";
}
