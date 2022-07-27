package proj.concert.service.domain;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

// Seats Class
// create seatCompostie for its Id class
@Entity
@Table(name = "SEATS")
@IdClass(SeatComposite.class)
public class Seat {

	@Id
	private String label;
	private boolean seatReserved;
	private BigDecimal seatPrice;
	@Id
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime date;

	public Seat() {
	}

	public Seat(String label, boolean isBooked, LocalDateTime date, BigDecimal newPrice) {
		this.label = label;
		this.seatReserved = isBooked;
		this.date = date;
		this.seatPrice = newPrice;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getSeatReserved() {
		return seatReserved;
	}

	public void setSeatReserved(Boolean isRes) {
		this.seatReserved = isRes;
	}

	public BigDecimal getSeatPrice() {
		return seatPrice;
	}

	public void setSeatPrice(BigDecimal price) {
		this.seatPrice = price;
	}

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public LocalDateTime getDate() {
		return date;
	}

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	@Override
	public int hashCode() {
		return Objects.hash(label, date, seatPrice);
	}

	@Override
	public boolean equals(Object testSeat) {
		if (!(testSeat instanceof Seat)) {
			return false;
		} else if (this == testSeat) {
			return true;
		}
		if (Objects.equals(label, ((Seat) testSeat).label) && seatPrice.equals(((Seat) testSeat).seatPrice) && Objects.equals(date, ((Seat) testSeat).date)) {
			return true;
		} else {
			return false;
		}
	}
}

