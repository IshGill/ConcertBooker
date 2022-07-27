package proj.concert.service.domain;
import javax.persistence.*;
import proj.concert.common.types.Genre;
import java.util.Objects;
import java.util.Set;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

// Performer Class
@Entity
@Table(name = "PERFORMERS")
public class Performer {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

//    will implement performers in concert.java
    @ManyToMany(mappedBy = "performers", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Concert> concerts;

   @Column(name = "IMAGE_NAME")
   private String imageName;

   @Enumerated(EnumType.STRING)
   private Genre genre;

   @Column(name = "NAME")
   private String name;

   @Column(columnDefinition = "TEXT")
   private String blurb;

   public Performer() { }

   public Performer(Long id, String name, String imageName, Genre genre, String blurb) {
       this.id = id;
       this.name = name;
       this.imageName = imageName;
       this.genre = genre;
       this.blurb = blurb;
   }

   public Performer(String name, String imageName, Genre genre, String blurb) {
       this(null, name, imageName, genre, blurb);
   }

   public Long getId() {
       return id;
   }

   public void setId(Long id) {
       this.id = id;
   }

   public Set<Concert> getConcerts() {
        return concerts;
    }

    public String getImageName() {
       return imageName;
   }

   public void setImageName(String imageUri) {
       this.imageName = imageUri;
   }

   public String getName() {
       return name;
   }

   public void setName(String name) {
       this.name = name;
   }

   public Genre getGenre() {
       return this.genre;
   }

   public void setGenre(Genre genre) {
       this.genre = genre;
   }

   public String getBlurb() {
       return blurb;
   }

   public void setBlurb(String blurb){
       this.blurb = blurb;
   }

   @Override
   public String toString() {
       StringBuffer buffer = new StringBuffer();
       buffer.append("Performer, id: ");
       buffer.append(id);
       buffer.append(", name: ");
       buffer.append(name);
       buffer.append(", s3 image: ");
       buffer.append(imageName);
       buffer.append(", genre: ");
       buffer.append(genre.toString());
       return buffer.toString();
   }

   	@Override
	public int hashCode() { return Objects.hash(id, genre, name, imageName); }

	@Override
	public boolean equals(Object testPerformer) {
        if (!(testPerformer instanceof Performer)) {
            return false;
        } else if (this == testPerformer) {
            return true;
        }
        if (Objects.equals(id, ((Performer) testPerformer).id) && name.equals(((Performer) testPerformer).name) && Objects.equals(imageName, ((Performer) testPerformer).imageName) && Objects.equals(genre, ((Performer) testPerformer).genre)) {
        	return true;
		} else {
        	return false;
		}
	}
}
