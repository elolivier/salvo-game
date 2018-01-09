package salvo.salvo;

//import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import java.util.List;

@RepositoryRestResource
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Player findByUserNameOrEmail(String userName, String email);
    Player findByUserName(String username);
}
