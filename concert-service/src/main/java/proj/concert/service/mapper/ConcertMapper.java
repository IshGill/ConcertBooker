package proj.concert.service.mapper;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//Concert Mapper
public class ConcertMapper {
	private ConcertMapper() {}

	public static ConcertDTO concertMapperDTO(Concert concertObject) {
		ConcertDTO conDto = new ConcertDTO(concertObject.getId(), concertObject.getConcertTitle(), concertObject.getImageNameData(), concertObject.getBlurb());
		for (Performer p: concertObject.getPerformers()) {
		    conDto.getPerformers().add(PerformerMapper.PerformerMapperDTO(p));
        }
		conDto.getDates().addAll(concertObject.getConcertDates());
		return conDto;
	}
	//concert relative to summary
	public  static ConcertSummaryDTO summaryDTO(Concert c){
		return new ConcertSummaryDTO(c.getId(), c.getConcertTitle(), c.getImageNameData());
	}
}
