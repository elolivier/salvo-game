package salvo.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SalvoApplication {
	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
    }

	@Bean
	public CommandLineRunner initData(PlayerRepository repPlayer,
                                      GameRepository repGame,
                                      GamePlayerRepository repGamePlayer,
                                      ShipRepository repShip,
                                      SalvoRepository repSalvo,
                                      ScoreRepository repScore) {
		return (args) -> {
            Player player1 = new Player("j.bauer","j.bauer@ctu.gov", "24");
            Player player2 = new Player("c.obrian","c.obrian@ctu.gov", "42");
            Player player3 = new Player("kim_bauer","kim_bauer@gmail.com", "kb");
            Player player4 = new Player("t.almeida","t.almeida@ctu.gov", "Bauer");
            Game game1 = new Game();
            Game game2 = new Game();
            Game game3 = new Game();
            Game game4 = new Game();
            GamePlayer gamePlayer1 = new GamePlayer(game1, player1);
            GamePlayer gamePlayer2 = new GamePlayer(game1, player2);
            GamePlayer gamePlayer3 = new GamePlayer(game2, player3);
            GamePlayer gamePlayer4 = new GamePlayer(game2, player4);
            GamePlayer gamePlayer5 = new GamePlayer(game3, player2);
//            GamePlayer gamePlayer6 = new GamePlayer(game3, player3);
            GamePlayer gamePlayer7 = new GamePlayer(game4, player1);
            List<String> sh1_loc = new ArrayList<>(Arrays.asList("J1", "J2", "J3", "J4"));
            List<String> sh2_loc = new ArrayList<>(Arrays.asList("C2", "C3", "C4"));
            List<String> sh3_loc = new ArrayList<>(Arrays.asList("A1", "A2", "A3", "A4"));
            List<String> sh4_loc = new ArrayList<>(Arrays.asList("F2", "G2", "H2"));
            Ship ship1 = new Ship("Battleship", sh1_loc, gamePlayer2);
            Ship ship2 = new Ship("Destroyer", sh2_loc, gamePlayer4);
            Ship ship3 = new Ship("Battleship", sh3_loc, gamePlayer5);
            Ship ship4 = new Ship("Battleship", sh3_loc, gamePlayer1);
            Ship ship5 = new Ship("Destroyer", sh2_loc, gamePlayer1);
            Ship ship6 = new Ship("Destroyer", sh4_loc, gamePlayer2);
            List<String> sal1_loc1_1 = new ArrayList<>(Arrays.asList("F2", "H1", "A1"));
            List<String> sal1_loc2_1 = new ArrayList<>(Arrays.asList("F6", "C3", "C2"));
            List<String> sal1_loc1_2 = new ArrayList<>(Arrays.asList("J4", "G2", "H2"));
            List<String> sal1_loc2_2 = new ArrayList<>(Arrays.asList("C7", "A3", "D4"));
            Salvo salvo1_1 = new Salvo(1, sal1_loc1_1, gamePlayer1);
            Salvo salvo1_2 = new Salvo(1, sal1_loc2_1, gamePlayer2);
            Salvo salvo1_3 = new Salvo(2, sal1_loc1_2, gamePlayer1);
            Salvo salvo1_4 = new Salvo(2, sal1_loc2_2, gamePlayer2);
            Score score1 = new Score(game1, player1, 1.0);
            Score score2 = new Score(game1, player2, 0.0);
            Score score3 = new Score(game2, player3, 0.5);
            Score score4 = new Score(game2, player4, 0.5);
            Score score5 = new Score(game3, player2, 0.0);
            Score score6 = new Score(game3, player3, 1.0);
			repPlayer.save(player1);
            repPlayer.save(player2);
			repPlayer.save(player3);
			repPlayer.save(player4);
			repGame.save(game1);
            repGame.save(game2);
            repGame.save(game3);
            repGame.save(game4);
            repGamePlayer.save(gamePlayer1);
            repGamePlayer.save(gamePlayer2);
			repGamePlayer.save(gamePlayer3);
			repGamePlayer.save(gamePlayer4);
            repGamePlayer.save(gamePlayer5);
//            repGamePlayer.save(gamePlayer6);
            repGamePlayer.save(gamePlayer7);
            repShip.save(ship1);
            repShip.save(ship2);
            repShip.save(ship3);
            repShip.save(ship4);
            repShip.save(ship5);
            repShip.save(ship6);
            repSalvo.save(salvo1_1);
            repSalvo.save(salvo1_2);
            repSalvo.save(salvo1_3);
            repSalvo.save(salvo1_4);
            repScore.save(score1);
            repScore.save(score2);
            repScore.save(score3);
            repScore.save(score4);
            repScore.save(score5);
            repScore.save(score6);
		};
	}
}

@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {
    @Autowired
    PlayerRepository playerRepo;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService());
    }

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsService() {

            @Override
            public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
                Player player = playerRepo.findByUserName(name);
                if (player != null) {
                        return new User(player.getUserName(), player.getpassword(),
                                AuthorityUtils.createAuthorityList("USER"));
                } else {
                        throw new UsernameNotFoundException("Unknown user: " + name);
                }
            }
        };
    }
}

@EnableWebSecurity
@Configuration
class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/web/games.html").permitAll()
                .antMatchers("/api/games").permitAll()
                .antMatchers("/api/leaderboard").permitAll()
                .antMatchers("/web/assets/**").permitAll()
                .antMatchers("/api/players").permitAll()
                .anyRequest().fullyAuthenticated();

        http.formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .loginPage("/api/login");

        http.logout().logoutUrl("/api/logout");

        // turn off checking for CSRF tokens
        http.csrf().disable();

        // if user is not authenticated, just send an authentication failure response
        http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if login is successful, just clear the flags asking for authentication
        http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

        // if login fails, just send an authentication failure response
        http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

        // if logout is successful, just send a success response
        http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
    }

    private void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        }
    }
}