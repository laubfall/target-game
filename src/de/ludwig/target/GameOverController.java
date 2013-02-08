package de.ludwig.target;

import java.io.IOException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.text.Text;

public class GameOverController {

	@FXML
	private Text score;

	private SimpleBooleanProperty playAgain;

	private Node load;

	public GameOverController(String score, SimpleBooleanProperty playAgain) {
		super();
		this.playAgain = playAgain;

		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"/de/ludwig/target/GameOverScene.fxml"));
		loader.setController(this);
		try {
			load = (Node) loader.load();
			this.score.setText(score);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void again(Event event) {
		playAgain.set(true);
	}

	public void bye(Event event) {
		System.exit(0);
	}

	public final Node node() {
		return load;
	}

	public Text getScore() {
		return score;
	}

	public void setScore(Text score) {
		this.score = score;
	}
}
