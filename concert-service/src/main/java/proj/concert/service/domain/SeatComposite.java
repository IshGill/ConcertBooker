package proj.concert.service.domain;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//SeatComposite for Seat class
public class SeatComposite implements Serializable {
	public String label;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime date;
	public static final int serialVersionUID = 777;

	public SeatComposite() {
	}

	public SeatComposite(String newLabel) {
		this.label = newLabel;
		this.date = null;
	}

	public SeatComposite(LocalDateTime newDate) {
		this.label = null;
		this.date = newDate;
	}

	public SeatComposite(String newLabel, LocalDateTime newDate) {
		this.label = newLabel;
		this.date = newDate;
	}

	public long getRandomUID() {
		long LOWER_RANGE = 0;
		long UPPER_RANGE = 1000000;
		Random random = new Random();
		long randomValue = LOWER_RANGE +
				(long) (random.nextDouble() * (UPPER_RANGE - LOWER_RANGE));
		return randomValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(label, date);
	}

	@Override
	public boolean equals(Object testSeatComp) {
		if (!(testSeatComp instanceof SeatComposite)) {
			return false;
		} else if (this == testSeatComp) {
			return true;
		}
		if (Objects.equals(label, ((SeatComposite) testSeatComp).label) && Objects.equals(date, ((SeatComposite) testSeatComp).date)) {
			return true;
		} else {
			return false;
		}
	}
}

