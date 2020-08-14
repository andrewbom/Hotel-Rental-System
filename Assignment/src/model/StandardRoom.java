package model;

import exception.RentRoomException;

import static exception.RentRoomException.RentRoomError.ExceedMaxRentalDays;
import static exception.RentRoomException.RentRoomError.ExceedMinRentalDays;

public class StandardRoom extends Room {

    private static final double RENTAL_FEE_1_BEDROOM = 59;
    private static final double RENTAL_FEE_2_BEDROOM = 99;
    private static final double RENTAL_FEE_4_BEDROOM = 199;
    private static final double MIN_RENT_DAY = 2;
    private static final double MAX_RENT_DAY = 10;
    private static final double LATE_RATE = 135.0 / 100;

    public StandardRoom(String roomId, int numOfBedrooms, String featureSummary, int roomType) {
        super(roomId, numOfBedrooms, featureSummary, roomType);
    }

    // Constructor for create standard room from Database
	StandardRoom(String roomId, int numOfBedrooms,
				 String featureSummary, int roomStatus, String image) {

        super(roomId, numOfBedrooms, featureSummary,
                1, roomStatus, null, image);
    }

    @Override
    public String getRoomType() {
        return new String("Standard Room");
    }

    @Override
    public double getPrice() {
        switch (getNumOfBedrooms()) {
            case 2:
                return RENTAL_FEE_2_BEDROOM;
            case 4:
                return RENTAL_FEE_4_BEDROOM;
            default:
                return RENTAL_FEE_1_BEDROOM;
        }
    }

    @Override
    public void checkRentDays(DateTime rentDate, int numOfRentDays) throws RentRoomException {

        String nameOfTheDay = rentDate.getNameOfDay(); //check the name of the rent date

        //Standard room can be rented for maximum 10 days
        if (numOfRentDays > MAX_RENT_DAY) {
            throw new RentRoomException(ExceedMaxRentalDays);
        }

//		//Standard room can be rented for a minimum of 2 days
//		if (numOfRentDays < 2) {
//			System.out.println("FAIl! The standard room can be rented for a minimum of 2 days.");
//			return false;
//		}

        //Standard room should be rented for a minimum of 3 days if the rental day is Sunday or Saturday
        if (numOfRentDays <= 2) {
            if (nameOfTheDay.equals("Saturday") || nameOfTheDay.equals("Sunday")) {
                System.out.println(
                        "Since the rental day is Saturday or Sunday, the Standard room should be rented from a minimum of 3 days.");
                throw new RentRoomException(ExceedMinRentalDays);
            }
        }
    }

    @Override
    public double calRentFee(double actualDays, double estDays) {

        double rentFee = 0;

        //if the customer returns the room within the same day, it will be charged for minimum 2 days fee
        if (actualDays <= 1) {
            if (super.getNumOfBedrooms() == 1) {
                //fee for 1 bedrooms
                rentFee = RENTAL_FEE_1_BEDROOM * MIN_RENT_DAY;

            } else if (super.getNumOfBedrooms() == 2) {
                //fee for 2 bedrooms
                rentFee = RENTAL_FEE_2_BEDROOM * MIN_RENT_DAY;
            } else if (super.getNumOfBedrooms() == 4) {
                //fee for 4 bedrooms
                rentFee = RENTAL_FEE_4_BEDROOM * MIN_RENT_DAY;
            }

            //if the customer returns the room earlier
        } else if (actualDays <= estDays) {
            if (super.getNumOfBedrooms() == 1) {
                //fee for 1 bedroom
                rentFee = RENTAL_FEE_1_BEDROOM * actualDays;

            } else if (super.getNumOfBedrooms() == 2) {
                //fee for 2 bedrooms
                rentFee = RENTAL_FEE_2_BEDROOM * actualDays;
            } else if (super.getNumOfBedrooms() == 4) {
                rentFee = RENTAL_FEE_4_BEDROOM * actualDays;
            }


        } else {

            if (super.getNumOfBedrooms() == 1) {
                //fee for 1 bedroom
                rentFee = RENTAL_FEE_1_BEDROOM * estDays;

            } else if (super.getNumOfBedrooms() == 2) {
                rentFee = RENTAL_FEE_1_BEDROOM * estDays;
            } else if (super.getNumOfBedrooms() == 4) {
                rentFee = RENTAL_FEE_4_BEDROOM * estDays;
            }
        }

        return rentFee;

    }

    @Override
    public double calLateFee(double actualDays, double estDays) {

        double lateFee = 0;

        //if the customer returns the room on time
        if (actualDays < estDays) {
            lateFee = 0;


        } else {
            //if the customer returns the room late

            if (super.getNumOfBedrooms() == 1) {

                //late fee for 1 bedroom
                lateFee = LATE_RATE * RENTAL_FEE_1_BEDROOM * (actualDays - estDays);


            } else if (super.getNumOfBedrooms() == 2) {

                //late fee for 2 bedrooms
                lateFee = LATE_RATE * RENTAL_FEE_2_BEDROOM * (actualDays - estDays);
            } else if (super.getNumOfBedrooms() == 4) {
                //late fee for 4 bedrooms
                lateFee = LATE_RATE * RENTAL_FEE_4_BEDROOM * (actualDays - estDays);
            }
        }
        return lateFee;
    }
}
