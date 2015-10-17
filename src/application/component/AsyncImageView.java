package application.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class AsyncImageView extends Pane {

	/**
	 * Handler for remove placeholder when image is loaded
	 * @author thomas
	 *
	 */
	private class RemovePlaceHolderListener implements ChangeListener<Number> {
		private final AsyncImageView asyncImageView;

		public RemovePlaceHolderListener(AsyncImageView imageView) {
			super();
			this.asyncImageView = imageView;
		}

		@Override
		public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
			if (newValue.floatValue() == 1.0 && !asyncImageView.imageView.getImage().isError()) {
				asyncImageView.getChildren().remove(asyncImageView.placeholder);
				asyncImageView.getChildren().add(asyncImageView.imageView);
			}
		}
	};

	private static Color[] COLORS = new Color[]{
			Color.rgb(26, 188, 156),
			Color.rgb(52, 152, 219),
			Color.rgb(155, 89, 182),
			Color.rgb(52, 73, 94),
			Color.rgb(230, 126, 34),
			Color.rgb(231, 76, 60),
			Color.rgb(241, 196, 15)
	};
	private static Random RANDOM = new Random();
	private static Color chooseColor() {
		return COLORS[RANDOM.nextInt(COLORS.length)];
	}

	private static Map<String, List<AsyncImageView>> scrollToAsync = new HashMap<>();

	private static int TILE_WIDTH = 150;
	private static int TILE_HEIGHT = 225;

	/**
	 * the image view
	 */
	private ImageView imageView;

	/**
	 * the placeholder pane
	 */
	private final Pane placeholder;

	private final String path;

	public AsyncImageView(String path, String placeholderChar, ScrollPane scrollPane) {
		this.path = path;
		placeholder = new StackPane();
		placeholder.setBackground(new Background(new BackgroundFill(chooseColor(), CornerRadii.EMPTY, Insets.EMPTY)));
		placeholder.setPrefWidth(TILE_WIDTH);
		placeholder.setPrefHeight(TILE_HEIGHT);
		placeholder.getStyleClass().add("placeholder");

		Label label = new Label(placeholderChar);
		placeholder.getChildren().add(label);
		StackPane.setAlignment(label, Pos.CENTER);

		this.getChildren().add(placeholder);

		if (!scrollToAsync.containsKey(scrollPane.getId())) {
			ChangeListener<Number> changeListener = new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					List<AsyncImageView> asyncViews = scrollToAsync.get(scrollPane.getId());
					List<AsyncImageView> toRemove = new ArrayList<>();
					for (AsyncImageView asyncView : asyncViews) {
						if (asyncView.isVisible(scrollPane) && asyncView.imageView == null) {
							asyncView.loadImage();
							toRemove.add(asyncView);
						}
					}

					asyncViews.removeAll(toRemove);
				}
			};
			scrollPane.vvalueProperty().addListener(changeListener);
			scrollToAsync.put(scrollPane.getId(), new ArrayList<AsyncImageView>());
		}


		scrollToAsync.get(scrollPane.getId()).add(this);

		this.widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				loadIfVisible(scrollPane);
				observable.removeListener(this);
			}
		});
	}

	private void loadImage(){
		Image image = CacheImageFactory.getImage(this.path, TILE_WIDTH, TILE_HEIGHT, true, true, true);
		this.imageView = new ImageView(image);
		this.imageView.getStyleClass().add("poster");
		image.progressProperty().addListener(new RemovePlaceHolderListener(this));
	}

	private boolean isVisible(ScrollPane scrollPane) {
		Bounds paneBounds = scrollPane.localToScene(scrollPane.getBoundsInParent());
		Bounds bounds = this.localToScene(this.getBoundsInLocal());
		// on init bound for this have not the correct idth and height
		Bounds correctBound = new BoundingBox(bounds.getMinX(), bounds.getMinY(), TILE_WIDTH, TILE_HEIGHT);

		return paneBounds.intersects(correctBound);
	}

	public void loadIfVisible(ScrollPane scrollPane) {
		if ( this.imageView == null && isVisible(scrollPane)) {
			loadImage();
			scrollToAsync.get(scrollPane.getId()).remove(this);
		}
	}
}
