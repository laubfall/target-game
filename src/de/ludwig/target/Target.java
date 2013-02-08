package de.ludwig.target;

import java.net.URL;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.ScaleTransitionBuilder;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayerBuilder;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Target implements Entity {
	private Circle target;

	private Text hitTime;

	private boolean hit;

	private long startedAt;

	private long timeUntilHit;

	private ScaleTransition scaleTransition;

	final Timeline hitTimeAnim;
	
	private Text miss;
	
	private Timeline missAnim;
	
	public Target(int targetRadius, int posX, int posY, Duration targetPlayTime) {
		target = new Circle(1);
		target.setFill(Color.RED);
		target.setCenterX(posX);
		target.setCenterY(posY);
		target.setVisible(false);
		target.setOnMouseClicked(new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				hit();
			}
		});

		hitTime = new Text("");
		hitTime.setVisible(false);		

		scaleTransition = ScaleTransitionBuilder.create().cycleCount(2)
				.autoReverse(true).node(target).fromX(0).fromY(0).byX(2).byY(2)
				.toX(targetRadius).toY(targetRadius).duration(targetPlayTime).onFinished(new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						miss();
						onFinish();
					}
				})
				.build();
		
		hitTimeAnim = new Timeline();
		KeyFrame k1 = new KeyFrame(Duration.millis(0), new KeyValue(hitTime.visibleProperty(), true));
		hitTimeAnim.getKeyFrames().add(k1);
		k1 = new KeyFrame(Duration.millis(1500), new KeyValue(hitTime.visibleProperty(), false));
		hitTimeAnim.getKeyFrames().add(k1);
		
		miss = new Text("miss!");
		miss.setId("miss");
		miss.setVisible(false);
		missAnim = new Timeline();
		k1 = new KeyFrame(Duration.millis(0), new KeyValue(miss.visibleProperty(), true));
		missAnim.getKeyFrames().add(k1);
		k1 = new KeyFrame(Duration.millis(1000), new KeyValue(miss.visibleProperty(), false));
		missAnim.getKeyFrames().add(k1);
	}

	@Override
	public Node graphics() {
		return new Group(target, hitTime, miss);
	}

	@Override
	public Animation animation() {
		return scaleTransition;
	}

	public void readyUp() {
		target.setVisible(true);

		// die Scale-Transition modifiziert die Größe, wenn wir das nicht
		// zurücksetzen wird beim nächsten Starten der Animation kurz das Target
		// nach der Skalierung angezeigt, das führt zu unschönen
		// Flacker-Effekten.
		target.setScaleX(0);
		target.setScaleX(0);
		startedAt = System.currentTimeMillis();
	}

	public void reset() {
		timeUntilHit = 0l;
		hit = false;
		target.setVisible(false);
		scaleTransition.stop();
	}

	public boolean isHit() {
		return hit;
	}

	private void hit() {
		final URL resource = getClass().getClassLoader().getResource("resources/96132__bmaczero__bing1.wav");
		
		AudioClip ac = new AudioClip(resource.toString());
		ac.play();

		this.hit = true;
		this.timeUntilHit = System.currentTimeMillis() - startedAt;
		hitTime.setText("+" + String.valueOf(timeUntilHit) + " ms");
		hitTime.setId("hitTime");
		hitTimeAnim.play();
		hitTime.setX(target.getCenterX());
		hitTime.setY(target.getCenterY());
	}

	private void miss() {
		final URL resource = getClass().getClassLoader().getResource("resources/14609__man__swosh.aif");
		final Media media = new Media(resource.toString());
		final MediaPlayer player = MediaPlayerBuilder.create()
		                      .media(media).startTime(Duration.millis(3350)).stopTime(Duration.seconds(3).add(Duration.millis(900)))
		                      .build();

		player.play();
		
		missAnim.play();
		miss.setX(target.getCenterX());
		miss.setY(target.getCenterY());
	}
	
	public void reposition(int posX, int posY) {
		target.setCenterX(posX);
		target.setCenterY(posY);
	}

	public long getTimeUntilHit() {
		return timeUntilHit;
	}

	protected void onFinish() {
		
	}
}
