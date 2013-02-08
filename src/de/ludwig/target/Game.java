package de.ludwig.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javafx.animation.Animation.Status;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TimelineBuilder;
import javafx.application.Application;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Game extends Application {

	private final static Duration LEVEL_TIME_SEC = Duration.seconds(30);

	/**
	 * Anzahl der Teile in die die gesamte Level Dauer {@link #LEVEL_TIME_SEC}
	 * unterteil wird.
	 */
	private final static int TIME_INTERVAL_COUNT = 30;

	public final static int SCENE_WIDTH = 400;

	public final static int SCENE_HEIGHT = 450;

	private final static int TARGET_RADIUS = 14;

	private final static Duration TARGET_PLAYTIME = Duration.millis(3000);

	private final static int MAX_LEVELS = 99;

	private final static int START_TARGET_COUNT = 3;

	/**
	 * Anzahl Ziele die zu Beginn eines neuen Levels zu den bereits vorhandenen
	 * Zielen hinzugefügt werden.
	 */
	private final static int NEW_TARGETS_AT_NEW_LEVEL = 1;
	
	/**
	 * Maximale Anzahl der Targets pro Zeitintervall
	 */
	private int targetsPerTimeIntervall = START_TARGET_COUNT;

	private List<Target> targets = new ArrayList<>();

	private SimpleIntegerProperty playerScore = new SimpleIntegerProperty(0);

	private SimpleIntegerProperty currentLevel = new SimpleIntegerProperty(1);

	private SimpleBooleanProperty playAgain = new SimpleBooleanProperty(false);

	private SimpleBooleanProperty startGame = new SimpleBooleanProperty(false);

	/**
	 * Anzahl an nicht getroffener Targets. Wird beim ersten Hit zurückgesetzt.
	 */
	private int misses = 0;

	private SimpleBooleanProperty gameOver = new SimpleBooleanProperty(false);

	final Timeline gameTimeline = gameTimeline();

	private final Group topLevelGroup = new Group();

	private final PlayGroup playGroup = new PlayGroup();

	private Scene scene;

	final AnimationTimer at = new AnimationTimer() {
		int frameCnt = 0;

		@Override
		public void handle(long now) {
			frameCnt++;
			if (frameCnt != 25) {
				return;
			}

			Random r = new Random();
			for (final Target t : targets) {
				boolean hit = t.isHit();

				// score berechnen
				if (hit) {
					float scoreMul = (float) 1000 / (float) t.getTimeUntilHit();
					playerScore.setValue(playerScore.getValue() + 100
							* scoreMul);
					t.reset();
				}

				// prüfen ob das Spiel zu ende ist und per Zufall entscheiden ob
				// weitere Targets gestartet werden.
				Status status = t.animation().getStatus();
				if (status.equals(Status.STOPPED)) {
					if (r.nextBoolean()) {
						int[] targetPos = targetPos();
						t.animation().play();
						t.reposition(targetPos[0], targetPos[1]);
						t.readyUp();
					}
				}
			}

			frameCnt = 0;
		}
	};

	@Override
	public void start(final Stage primaryStage) {
		scene = new Scene(topLevelGroup);
		scene.getStylesheets().add("de/ludwig/target/main.css");
		topLevelGroup.getChildren().add(
				new StartGroupController(startGame).node());

		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.sizeToScene();

		for (int i = 0; i < targetsPerTimeIntervall; i++) {
			Target target = target();
			targets.add(target);
			playGroup.addTarget(target);
		}

		final CountDownGroup cdg = new CountDownGroup(200, 200);
		cdg.cyclesProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				if (arg2.intValue() == 0) {
					topLevelGroup.getChildren().clear();
					topLevelGroup.getChildren().add(playGroup);
					gameTimeline.playFromStart();
				}
			}
		});

		startGame.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				topLevelGroup.getChildren().clear();
				topLevelGroup.getChildren().add(cdg);
				cdg.startCountdown();
			}
		});

		playerScore.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				playGroup.updatePlayerScore(newValue.intValue());
			}
		});

		currentLevel.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				playGroup.updateCurrentLevel(arg2.toString());
			}
		});

		gameOver.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				if (arg2.equals(false)) {
					return;
				}

				at.stop();
				gameTimeline.stop();
				for (Target t : targets) {
					t.animation().stop();
				}

				topLevelGroup.getChildren().clear();
				GameOverController goc = new GameOverController(playerScore
						.getValue().toString(), playAgain);
				topLevelGroup.getChildren().add(goc.node());
			}
		});

		playAgain.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				topLevelGroup.getChildren().clear();
				reset();
				
				playGroup.reset();
				playGroup.init();
				
				for (int i = 0; i < targetsPerTimeIntervall; i++) {
					Target target = target();
					targets.add(target);
					playGroup.addTarget(target);
				}
				
				topLevelGroup.getChildren().add(playGroup);
				playAgain.set(false);
				gameTimeline.play();
			}
		});
	}

	private final Timeline gameTimeline() {
		final Collection<KeyFrame> keyFrames = new ArrayList<>();

		final Duration intervalDuration = LEVEL_TIME_SEC
				.divide(TIME_INTERVAL_COUNT);

		for (int i = 0; i <= TIME_INTERVAL_COUNT; i++) {
			IntervalEventHandler ieh = null;
			if (i == 0) {
				ieh = new IntervalEventHandler(
						IntervalEventHandler.INTERVAL_TYPE_START);
			} else if (i == TIME_INTERVAL_COUNT) {
				ieh = new IntervalEventHandler(
						IntervalEventHandler.INTERVAL_TYPE_END);
			}

			KeyFrame k1 = new KeyFrame(intervalDuration.multiply(i), ieh,
					(KeyValue[]) null);
			keyFrames.add(k1);
		}

		final Timeline build = TimelineBuilder.create().keyFrames(keyFrames)
				.cycleCount(MAX_LEVELS).build();
		return build;
	}

	private final Target target() {
		int[] targetPos = targetPos();
		return new Target(TARGET_RADIUS, targetPos[0], targetPos[1],
				TARGET_PLAYTIME) {
			@Override
			protected void onFinish() {
				misses++;
				gameOver();
			}
		};
	}

	public static void main(String[] args) {
		launch(args);
	}

	private void gameOver() {
		final Integer curLevel = currentLevel.getValue();

		float levelMissRatio = 100 / MAX_LEVELS;
		int negoateLvl = MAX_LEVELS - curLevel;

		int targetLvlCnt = ((int) (LEVEL_TIME_SEC.toSeconds() * targetsPerTimeIntervall) / (TIME_INTERVAL_COUNT / 2));
		int allowedMisses = (int) ((targetLvlCnt * levelMissRatio * negoateLvl) / 100);
		gameOver.set(misses != 0 && allowedMisses < misses);
	}

	class IntervalEventHandler implements EventHandler<ActionEvent> {
		// Am Anfang eines Levels
		public static final int INTERVAL_TYPE_START = 0;

		// Am Ende eines Levels
		public static final int INTERVAL_TYPE_END = 2;

		private final int type;

		public IntervalEventHandler(int type) {
			super();
			this.type = type;
		}

		@Override
		public void handle(ActionEvent arg0) {
			switch (type) {
			case INTERVAL_TYPE_START:
				atStart();
				break;
			case INTERVAL_TYPE_END:
				atEnd();
				break;
			default:
				break;
			}
		}

		private void atStart() {
			if (targetsPerTimeIntervall > targets.size()) {
				for (int i = 0; i < NEW_TARGETS_AT_NEW_LEVEL; i++) {
					Target target = target();
					targets.add(target);
					playGroup.addTarget(target);
				}
			}

			final Random r = new Random();
			for (Target t : targets) {
				if (r.nextBoolean()) {
					t.readyUp();
					t.animation().play();
				}
			}

			misses = 0;

			at.start();
		}

		private void atEnd() {
			targetsPerTimeIntervall = targetsPerTimeIntervall
					+ NEW_TARGETS_AT_NEW_LEVEL;
			currentLevel.set(currentLevel.intValue() + 1);
			at.stop();
		}
	}

	private int[] targetPos() {
		Random r = new Random();
		int[] pos = new int[2];
		int sWidth = 400;
		int sHeight = 400;
		int ranX = r.nextInt(sWidth);
		if (ranX < TARGET_RADIUS) {
			ranX += TARGET_RADIUS;
		} else if (ranX > sWidth - TARGET_RADIUS) {
			ranX -= TARGET_RADIUS;
		}

		pos[0] = ranX;

		int ranY = r.nextInt(sHeight);
		if (ranY < TARGET_RADIUS) {
			ranY += TARGET_RADIUS;
		} else if (ranY > sHeight - TARGET_RADIUS) {
			ranY -= TARGET_RADIUS;
		}

		pos[1] = ranY;
		return pos;
	}
	
	/**
	 * resets every member that has to be reset to play a new game
	 */
	private void reset() {
		targetsPerTimeIntervall = START_TARGET_COUNT;
		targets.clear();
	}
}
