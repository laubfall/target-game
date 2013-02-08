package de.ludwig.target;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class PlayGroup extends Group {

	@FXML
	private Text playerScore;

	@FXML
	private Text currentLevel;

	@FXML
	private Pane playPane;
	
	public PlayGroup() {
		init();
	}

	/**
	 * also used to (re)init the Play-Scene
	 */
	public void init() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource(
				"/de/ludwig/target/PlayScene.fxml"));
		try {
			loader.setController(this);
			Node load = (Node) loader.load();
			getChildren().add(load);
			currentLevel.setText("1");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Node node() {
		return null;
	}
	
	public void reset() {
		getChildren().clear();
	}
	
	public void addTarget(final Target target) {
		playPane.getChildren().add(target.graphics());
	}

	public void removeTarget(final Target target) {
		playPane.getChildren().remove(target.graphics());
	}

	public void updatePlayerScore(final int score) {
		playerScore.setText(String.valueOf(score));
	}

	public void updateCurrentLevel(final String level) {
		currentLevel.setText(level);
	}

	public Text getPlayerScore() {
		return playerScore;
	}

	public void setPlayerScore(Text playerScore) {
		this.playerScore = playerScore;
	}

	public Text getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(Text currentLevel) {
		this.currentLevel = currentLevel;
	}

	public Pane getPlayPane() {
		return playPane;
	}

	public void setPlayPane(Pane playPane) {
		this.playPane = playPane;
	}
}
