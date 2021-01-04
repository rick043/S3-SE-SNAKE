package client;

import communicator.rest.ISnakeRest;
import communicator.rest.SnakeCommunicatorClientREST;
import communicator.websocket.SnakeCommunicatorWebSocket;
import communicator.websocket.SnakeCommunicatorClientWebSocket;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import shared.messages.Direction;
import shared.messages.MessageCreator;
import shared.messages.MessageOperation;
import shared.messages.response.ResponseGeneratedFruit;
import shared.messages.response.ResponseMove;
import shared.messages.response.ResponseRegister;
import shared.rest.Authentication;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnakeClient extends Application implements Observer {

    private static final Logger LOGGER = Logger.getLogger(SnakeClient.class.getName());

    private static final int FRUITS = 3;


    private static final double RECTANGLE_SIZE = 20;
    private static final int NR_SQUARES_HORIZONTAL = 75;
    private static final int NR_SQUARES_VERTICAL = 40;

    private VBox mainMenu = new VBox(20);
    private VBox loginMenu = new VBox(20);
    private VBox registerMenu = new VBox(20);
    private VBox playersMenu = new VBox(20);
    private Scene scene;
    private StackPane layout = new StackPane();

    private TextField txtUsernameLogin = new TextField();
    private TextField txtUsernameRegister = new TextField();
    private PasswordField txtPasswordLogin = new PasswordField();
    private PasswordField txtPasswordRegister = new PasswordField();
    private TextField txtEmail = new TextField();

    private Button btnLogin = new Button("Login");
    private Button btnRegister = new Button("Register");
    private Button btnSignIn = new Button("Sign In");
    private Button btnSignUp = new Button("Sign Up");
    private Button btnSinglePlayer = new Button("Single Player");
    private Button btnMultiPlayer = new Button("Multi Player");
    private Button btnHistory = new Button("History");
    private Button btnLogout = new Button("Exit");

    private Label lblPlayer1 = new Label();
    private Label lblPlayer2 = new Label();

    private Rectangle[][] playingFieldArea;

    private SnakeCommunicatorWebSocket communicatorWebSocket = null;
    private ISnakeRest communicatorREST = new SnakeCommunicatorClientREST();


    private String username;
    private int playerId;

    @Override
    public void start(Stage stage) throws Exception {
        LOGGER.info("Snake Client started");
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("snake-icon.png")));

        Group root = new Group();
        scene = new Scene(root, NR_SQUARES_HORIZONTAL * RECTANGLE_SIZE, NR_SQUARES_VERTICAL * RECTANGLE_SIZE);
        scene.getStylesheets().add(this.getClass().getClassLoader().getResource("style.css").toExternalForm());

        // Main playing field
        playingFieldArea = new Rectangle[NR_SQUARES_HORIZONTAL][NR_SQUARES_VERTICAL];
        for (int i = 0; i < NR_SQUARES_HORIZONTAL; i++) {
            for (int j = 0; j < NR_SQUARES_VERTICAL; j++) {
                double x = i * (RECTANGLE_SIZE);
                double y = j * (RECTANGLE_SIZE);
                Rectangle rectangle = new Rectangle(x, y, RECTANGLE_SIZE, RECTANGLE_SIZE);
                rectangle.setStroke(Color.BLACK);
                rectangle.setStrokeWidth(0.05);
                rectangle.setFill(Color.web("#424242"));
                rectangle.setVisible(true);
                playingFieldArea[i][j] = rectangle;
                root.getChildren().add(rectangle);
            }
        }

        Label label = new Label("Snake XI");
        label.setStyle("-fx-text-fill: #bebebe; -fx-font: 5em Consolas; -fx-padding: 0 0 500 0");

        btnLogin.setOnAction(event -> {
            try {
                loginPlayer();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Register Player error: {}", e.getMessage());
            }
        });

        btnRegister.setOnAction(event -> {
            try {
                registerPlayer();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Register Player error: {}", e.getMessage());
            }
        });

        btnSignIn.setOnAction(event -> {
            registerMenu.setVisible(false);
            loginMenu.setVisible(true);
            }
        );

        btnSignUp.setOnAction(event -> {
            loginMenu.setVisible(false);
            registerMenu.setVisible(true);
        });

        txtUsernameLogin.setPromptText("Username...");
        txtUsernameRegister.setPromptText("Username...");
        txtEmail.setPromptText("Email...");
        txtPasswordLogin.setPromptText("Password...");
        txtPasswordRegister.setPromptText("Password...");

        btnSinglePlayer.setOnAction(event -> {
            try {
                startGame(false);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Start game error: {}", e.getMessage());
            }
        });

        btnSinglePlayer.setOnAction(event -> {
            try {
                startGame(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Start game error: {}", e.getMessage());
            }
        });

        registerMenu.setAlignment(Pos.CENTER);
        registerMenu.getChildren().addAll(txtUsernameRegister, txtEmail, txtPasswordRegister, btnRegister, btnSignIn);
        registerMenu.setVisible(false);
        mainMenu.setAlignment(Pos.CENTER);
        mainMenu.getChildren().addAll(btnSinglePlayer, btnMultiPlayer, btnHistory, btnLogout);
        mainMenu.setVisible(false);
        playersMenu.setAlignment(Pos.CENTER);
        playersMenu.getChildren().addAll(lblPlayer1, lblPlayer2);
        playersMenu.setVisible(false);
        loginMenu.setAlignment(Pos.CENTER);
        loginMenu.getChildren().addAll(txtUsernameLogin, txtPasswordLogin, btnLogin, btnSignUp);
        loginMenu.setVisible(true);


        StackPane glass = new StackPane();
        glass.getChildren().addAll(label, loginMenu, mainMenu, registerMenu);

        glass.setStyle("-fx-background-color: rgba(10, 10, 10, 0.4);");
        glass.setMinWidth(scene.getWidth() - 80);
        glass.setMinHeight(scene.getHeight() - 80);

        layout.getChildren().add(glass);
        layout.setStyle("-fx-padding: 40;");
        root.getChildren().add(layout);

        stage.setTitle("Snake XI");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

    }

    private void loginPlayer() {
        loginMenu.setVisible(false);
        mainMenu.setVisible(true);

        communicatorREST.postSignIn(new Authentication(txtUsernameLogin.getText(), txtPasswordLogin.getText()));

        username = txtUsernameLogin.getText();


    }

    private void registerPlayer() {
        registerMenu.setVisible(false);
        mainMenu.setVisible(true);

        communicatorREST.postSignUp(new Authentication(txtUsernameRegister.getText(), txtEmail.getText(), txtPasswordRegister.getText()));

        username = txtUsernameRegister.getText();
    }

    private void startGame(boolean singlePlayer) {
        mainMenu.setVisible(false);

        //TODO: make players menu visible to know whos in lobby and whos ready, playersMenu.setVisible(true);

        layout.setVisible(false);


        communicatorWebSocket = SnakeCommunicatorClientWebSocket.getInstance();
        communicatorWebSocket.addObserver(this);
        communicatorWebSocket.start();

        communicatorWebSocket.register(username, singlePlayer);
        communicatorWebSocket.generateFruits(FRUITS);
        scene.setOnKeyPressed(SnakeClient.this::keyPressed);

    }

    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case KP_UP:
            case UP:
            case W:
                communicatorWebSocket.move(Direction.UP);
                break;
            case KP_DOWN:
            case DOWN:
            case S:
                communicatorWebSocket.move(Direction.DOWN);
                break;
            case KP_LEFT:
            case LEFT:
            case A:
                communicatorWebSocket.move(Direction.LEFT);
                break;
            case KP_RIGHT:
            case RIGHT:
            case D:
                communicatorWebSocket.move(Direction.RIGHT);
                break;
            case SPACE:
                communicatorWebSocket.toggleReady();
                break;
            default:
                break;
        }

    }

    private void updatePosition(int playerId, int[][] cells) {
        for (int column = 0; column < NR_SQUARES_HORIZONTAL; column++) {
            for (int row = 0; row < NR_SQUARES_VERTICAL; row++) {
                if (cells[row][column] == 0)
                    playingFieldArea[column][row].setFill(Color.web("#424242"));
                else if (cells[row][column] == this.playerId)
                    playingFieldArea[column][row].setFill(Color.GREEN);
                else if (isBetween(cells[row][column], 1, 8))
                    playingFieldArea[column][row].setFill(Color.DARKGREEN);
                else if (cells[row][column] == 9)
                    playingFieldArea[column][row].setFill(Color.YELLOW);
            }
        }
    }

    private static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    private void placeFruit(int row, int column) {
        playingFieldArea[row][column].setFill(Color.YELLOW);
    }

    public void endGame() {
        scene.setOnKeyPressed(null);
    }

    @Override
    public void update(Observable o, Object arg) {
        MessageOperation message = (MessageOperation) arg;
        MessageCreator messageCreator = new MessageCreator();

        switch (message.getOperation()) {
            case RESPONSE_REGISTER:
                ResponseRegister responseRegister = (ResponseRegister) messageCreator.createResult(message);
                playerId = responseRegister.getPlayerId();
                break;
            case RESPONSE_MOVE:
                ResponseMove messageMove = (ResponseMove) messageCreator.createResult(message);
                updatePosition(messageMove.getPlayerId(), messageMove.getCells());
                break;
            case RESPONSE_GENERATE_FRUIT:
                ResponseGeneratedFruit responseGeneratedFruit = (ResponseGeneratedFruit) messageCreator.createResult(message);
                for (int i = 0; i < responseGeneratedFruit.getColumn().size(); i++) {
                    placeFruit(responseGeneratedFruit.getRow().get(i), responseGeneratedFruit.getColumn().get(i));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message.getOperation());
        }
    }
}
