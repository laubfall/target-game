package de.ludwig.target;

import java.io.IOException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class StartGroupController {
	private SimpleBooleanProperty startGame;

	@FXML
	private Button startButton;
	
	private FXMLLoader loader;

	public StartGroupController(SimpleBooleanProperty startGame) {
		this.startGame = startGame;

		loader = new FXMLLoader(getClass().getResource(
				"/de/ludwig/target/StartGroup.fxml"));
		loader.setController(this);
	}

	public Node node() {
		Node node;
		try {
			node = (Node) loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return node;
	}

	public void onStart(Event event) {
		startGame.setValue(true);
	}

	public Button getStartButton() {
		return startButton;
	}

	public void setStartButton(Button startButton) {
		this.startButton = startButton;
	}
}
