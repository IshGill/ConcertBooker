package proj.concert.service.mapper;
import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.User;

/** CS331 Assignment
 * Ish Gill igil215
 *  Leo Wood lwoo131
 *  Roshan Patel rpat371
 */

//User Mapper
public class UserMapper {

    private UserMapper() {}

    public static UserDTO userMapperDTO(User userObject) {
        return new UserDTO(userObject.getUsername(), userObject.getPassword());
    }

}
