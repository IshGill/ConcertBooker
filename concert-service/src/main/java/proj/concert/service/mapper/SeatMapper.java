package proj.concert.service.mapper;
import proj.concert.common.dto.SeatDTO;
import proj.concert.service.domain.Seat;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//Seat Mapper
public class SeatMapper {
	private SeatMapper() {}

	public static SeatDTO seatMapperDTO(Seat seatObject) {
		return new SeatDTO(seatObject.getLabel(), seatObject.getSeatPrice());
	}
}
