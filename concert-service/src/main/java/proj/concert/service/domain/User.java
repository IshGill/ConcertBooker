package proj.concert.service.domain;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//User Class
//Many Users
@Entity
@Table(name = "USERS")
public class User {

	@Id
	private Long id;
	@Column(unique = true)
	private UUID sessionId;
	@Column(unique = true)
	private String username;
	private String password;
	@Version
	private Long version;

    //Users can have many bookings
	@OneToMany(cascade = CascadeType.PERSIST, mappedBy = "user")
	private Set<Booking> userBookings = new HashSet<>();

	public User() {
	}

	public User(String username, String password, Set<Booking> userBookings) {
		this.username = username;
		this.password = password;
		this.userBookings = userBookings;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<Booking> getUserBookings() {
		return userBookings;
	}

	public void setUserBookings(Set<Booking> bookings) {
		this.userBookings = bookings;
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, password);
	}

	@Override
	public boolean equals(Object testUser) {
		if (!(testUser instanceof User)) {
			return false;
		} else if (this == testUser) {
			return true;
		}
		if (username.equals(((User) testUser).username) && (password.equals(((User) testUser).password))) {
			return true;
		} else {
			return false;
		}
	}
}