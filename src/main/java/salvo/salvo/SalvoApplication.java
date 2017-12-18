package salvo.salvo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
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
                                      SalvoRepository repSalvo) {
		return (args) -> {
            Player player1 = new Player("t.almeida@ctu.gov","Tony", "Almeida");
            Player player2 = new Player("j.bauer@ctu.gov","Jack", "Bauer");
            Player player3 = new Player("c.obrian@ctu.gov","Chloe", "O'Brian");
            Player player4 = new Player("kim_bauer@gmail.com","Kim", "Bauer");
            Game game1 = new Game();
            Game game2 = new Game();
            GamePlayer gamePlayer1 = new GamePlayer(game1, player1);
            GamePlayer gamePlayer2 = new GamePlayer(game1, player2);
            GamePlayer gamePlayer3 = new GamePlayer(game2, player3);
            GamePlayer gamePlayer4 = new GamePlayer(game2, player4);
            List<String> sh1_loc = new ArrayList<>(Arrays.asList("E1", "E2", "E3", "E4"));
            List<String> sh2_loc = new ArrayList<>(Arrays.asList("C2", "C3", "C4"));
            List<String> sh3_loc = new ArrayList<>(Arrays.asList("A1", "A2", "A3", "A4"));
            Ship ship1 = new Ship("Battleship", sh1_loc, gamePlayer2);
            Ship ship2 = new Ship("Destroyer", sh2_loc, gamePlayer4);
            Ship ship3 = new Ship("Battleship", sh3_loc, gamePlayer4);
            Ship ship4 = new Ship("Battleship", sh3_loc, gamePlayer1);
            Ship ship5 = new Ship("Battleship", sh2_loc, gamePlayer1);
            List<String> sal1_loc1_1 = new ArrayList<>(Arrays.asList("E2", "D4"));
            List<String> sal1_loc2_1 = new ArrayList<>(Arrays.asList("F6", "H3"));
            List<String> sal1_loc1_2 = new ArrayList<>(Arrays.asList("A1", "G5"));
            List<String> sal1_loc2_2 = new ArrayList<>(Arrays.asList("C7", "A3"));
            Salvo salvo1_1 = new Salvo(1, sal1_loc1_1, gamePlayer1);
            Salvo salvo1_2 = new Salvo(1, sal1_loc2_1, gamePlayer2);
            Salvo salvo1_3 = new Salvo(2, sal1_loc1_2, gamePlayer1);
            Salvo salvo1_4 = new Salvo(2, sal1_loc2_2, gamePlayer2);
			repPlayer.save(player1);
            repPlayer.save(player2);
			repPlayer.save(player3);
			repPlayer.save(player4);
			repGame.save(game1);
            repGame.save(game2);
            repGamePlayer.save(gamePlayer1);
            repGamePlayer.save(gamePlayer2);
			repGamePlayer.save(gamePlayer3);
			repGamePlayer.save(gamePlayer4);
			repShip.save(ship1);
            repShip.save(ship2);
            repShip.save(ship3);
            repShip.save(ship4);
            repShip.save(ship5);
            repSalvo.save(salvo1_1);
            repSalvo.save(salvo1_2);
            repSalvo.save(salvo1_3);
            repSalvo.save(salvo1_4);
		};
	}
}
