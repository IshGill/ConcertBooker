package proj.concert.service.domain;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

// Concert Class
@Entity
@Table(name = "CONCERTS")
public class Concert {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "CONCERT_DATES",
			joinColumns = @JoinColumn(name = "CONCERT_ID"))
	@Column(name = "DATE")
	private Set<LocalDateTime> concertDates = new HashSet<>();

	@ManyToMany(cascade = CascadeType.PERSIST)
	@Fetch(FetchMode.SELECT)
	@JoinTable(name = "CONCERT_PERFORMER",
			joinColumns = @JoinColumn(name = "CONCERT_ID"),
			inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID"))
	private Set<Performer> performers = new HashSet<>();

	@Column(name = "IMAGE_NAME")
	private String imageNameData;
	@Column(columnDefinition = "TEXT")
	private String blurb;
	private String title;

	public Concert() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	public Set<LocalDateTime> getConcertDates() {
		return concertDates;
	}

	public void setConcertDates(Set<LocalDateTime> dates) {
		this.concertDates = dates;
	}

	public Set<Performer> getPerformers() {
		return performers;
	}

	public void setPerformers(Set<Performer> performers) {
		this.performers = performers;
	}

	public String getImageNameData() {
		return imageNameData;
	}

	public void setImageNameData(String imageNameData) {
		this.imageNameData = imageNameData;
	}

	public String getBlurb() {
		return blurb;
	}

	public void setBlurb(String blurb) {
		this.blurb = blurb;
	}


	public String getConcertTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public int hashCode() { return Objects.hash(id, title, concertDates); }

	@Override
	public boolean equals(Object testConcert) {
        if (!(testConcert instanceof Concert)) {
            return false;
        } else if (this == testConcert) {
            return true;
        }
        if (Objects.equals(id, ((Concert) testConcert).id) && title.equals(((Concert) testConcert).title) && Objects.equals(concertDates, ((Concert) testConcert).concertDates)) {
        	return true;
		} else {
        	return false;
		}
	}
}