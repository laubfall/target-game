package de.ludwig.target;

import javafx.animation.ScaleTransition;
import javafx.animation.ScaleTransitionBuilder;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class CountDownGroup extends Group {
	private final Text countdown = new Text();

	private SimpleIntegerProperty cycles = new SimpleIntegerProperty(3);

	private final ScaleTransition st;
	
	public CountDownGroup(double x, double y) {
		countdown.setTranslateX(x);
		countdown.setTranslateY(y);
		countdown.setFill(Color.RED);
		
		st = ScaleTransitionBuilder.create().byX(20)
				.byY(20).fromX(0).fromY(0).cycleCount(2).autoReverse(true)
				.node(countdown).build();

		countdown.setText(String.valueOf(getCycles()));
		
		st.setOnFinished(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				setCycles(getCycles() - 1);
				if(getCycles() == 0) {
					return;
				}
				
				countdown.setText(String.valueOf(getCycles()));
				st.playFromStart();
			}
		});

		getChildren().add(countdown);
	}
	
	public void startCountdown() {
		st.playFromStart();
	}
	
	public void setCycles(final Integer val) {
		cycles.set(val);
	}
	
	public Integer getCycles() {
		return cycles.getValue();
	}
	
	public SimpleIntegerProperty cyclesProperty() {
		return cycles;
	}
}
