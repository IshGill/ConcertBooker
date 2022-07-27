package proj.concert.service.domain;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

// Completed Booking Class
// Booking for concert
@Entity
@Table(name = "BOOKINGS")
public class Booking {

	/*
	Old- isnt related to user
	ManyToOne on concert_IDs

	Collection table "Booking_Seat"

	proposed:
	user attribute
	One user has many bookings

	 */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    //unsure if this needs to be joined yet.
    @Column(name = "CONCERT_ID", nullable = false)
    private Long concertID;

    @Column(name = "DATE", nullable = false)
    private LocalDateTime concert_date;

    @OneToMany(fetch = FetchType.EAGER)
    private List<Seat> bookSeats = new ArrayList<>();

    @Column(nullable = false, name = "VALID")
    private boolean isValid;

    @ManyToOne
    private User user;

    public Booking() {
    }

    public Booking(User user, long concertId, LocalDateTime date) {
        this.user = user;
        this.concertID = concertId;
        this.concert_date = date;
    }

    public Booking(long concertID, LocalDateTime concert_date, List<Seat> seats) {
        this.concertID = concertID;
        this.concert_date = concert_date;
        this.bookSeats = seats;
        this.isValid = false;
    }

    public Long getBookingId() {
        return this.bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getConcertID() {
        return this.concertID;
    }

    public void setConcertID(Long concertID) {
        this.concertID = concertID;
    }
    //Implement Serializers
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getDate() {
        return this.concert_date;
    }

    public void setDate(LocalDateTime date) {
        this.concert_date = date;
    }

    public List<Seat> getSeat() {
        return this.bookSeats;
    }

    public void setBookSeats(List<Seat> seats) {
        this.bookSeats = seats;
    }

    public boolean checkValidity() {
        return this.isValid;
    }

    public void setValidity(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookingId, user, concertID, concert_date);
    }

    @Override
    public boolean equals(Object testBooking) {
        if (!(testBooking instanceof Booking)) {
            return false;
        } else if (this == testBooking) {
            return true;
        }
        if (concertID.equals(((Booking) testBooking).concertID) && Objects.equals(bookingId, ((Booking) testBooking).bookingId) && Objects.equals(((Booking) testBooking).user, user) && Objects.equals(concert_date, ((Booking) testBooking).concert_date))
            return true;
        else {
            return false;
        }
    }
}

