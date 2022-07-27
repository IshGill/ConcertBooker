package proj.concert.service.mapper;
import proj.concert.common.dto.BookingDTO;
import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Booking;
import proj.concert.service.domain.Seat;
import java.util.ArrayList;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//Booking mapper for model
public class BookingMapper {
	private BookingMapper() {}

	public static BookingDTO bookingMapperDTO(Booking bookingObject) {
		ArrayList<SeatDTO> seats = new ArrayList<>();
		for (Seat s: bookingObject.getSeat()) {
			seats.add(SeatMapper.seatMapperDTO(s));
		}
		return new BookingDTO(bookingObject.getBookingId(), bookingObject.getDate(), seats);
	}
}
