package test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tankbattle.core.TankBattle;
import tankbattle.core.entity.Entity;
import tankbattle.core.entity.EntityGroupEvent;
import tankbattle.core.event.Listener;
import tankbattle.core.paint.EntityPaintEvent;
import tankbattle.core.position.Vector;
import tankbattle.core.time.TimeListener;
import tankbattle.core.view.EntityNode;
import tankbattle.core.view.View;

public class GameTest extends Application {

	@Override
	public void start(Stage primaryStage) {

		Group root = new Group();
		Scene scene = new Scene(root, 600, 480);

		View v = new View(600, 480);
		v.setScale(0.5);
		root.getChildren().add(v.getCanvas());
		// v.getCanvas().setTranslateX(300);
		// v.getCanvas().setTranslateY(240);

		TankBattle.getGame().getProcess().addListener(Listener.EXECUTE, EntityPaintEvent.class, e -> {
			EntityNode node = e.getNode();
			node.setWidth(50).setHeight(50);
			node.setVector(new Vector(-25, -25));

			node.setImage(new BufferedImage(50, 50, BufferedImage.TYPE_4BYTE_ABGR));
			Graphics2D g = node.getImage().createGraphics();
			g.setColor(Color.blue);
			g.fillRect(0, 0, 50, 50);
			g.dispose();
			e.setExecuted(true);
		});

		TankBattle.getGame().getTimer().addListener(new TimeListener(1000 / TankBattle.getGame().getFPS(), e -> {
			v.paint();
		}));

		Entity entity = new Entity();
		TankBattle.getGame().getProcess().send(new EntityGroupEvent(entity, EntityGroupEvent.ADD_ENTITY));

		TankBattle.getGame().getTimer().start();
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest(e -> {
			TankBattle.getGame().getTimer().stop();
			System.exit(0);
		});

	}

	public static void main(String[] args) {
		launch(args);
	}

}
