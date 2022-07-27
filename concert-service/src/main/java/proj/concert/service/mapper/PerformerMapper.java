package proj.concert.service.mapper;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Performer;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//Performer Mapper
public class PerformerMapper {
    private PerformerMapper(){}

    public static PerformerDTO PerformerMapperDTO(Performer performerObject){
        return new PerformerDTO(performerObject.getId(), performerObject.getName(),
                performerObject.getImageName(), performerObject.getGenre(), performerObject.getBlurb());
    }
}
