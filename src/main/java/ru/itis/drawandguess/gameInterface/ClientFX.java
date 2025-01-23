package ru.itis.drawandguess.gameInterface;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ClientFX extends Application {
    private TextArea chatArea;
    private TextField messageField;
    private TextField nicknameField;
    private Button sendButton;
    private Button connectButton;
    private Button clearButton;
    private Button sendNicknameButton; // Новая кнопка
    private Canvas canvas;
    private Label wordLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isDrawer = false;

    @Override
    public void start(Stage primaryStage) {
        startWelcomeWindow(primaryStage);
    }

    private void startWelcomeWindow(Stage primaryStage) {
        VBox welcomeLayout = new VBox(10);
        welcomeLayout.setAlignment(Pos.CENTER);

        Label title = new Label("Draw & Guess");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField joinCodeField = new TextField();
        joinCodeField.setPromptText("Enter lobby password");
        joinCodeField.setMaxWidth(200);

        Button joinButton = new Button("Join Lobby");
        joinButton.setOnAction(e -> {
            String code = joinCodeField.getText();
            if (!code.isEmpty()) {
                connectToLobby(code);
            } else {
                showAlert("Error", "Please enter a lobby code.");
            }
        });

        TextField passwordField = new TextField();
        passwordField.setPromptText("Set a lobby password");
        passwordField.setMaxWidth(200);

        Spinner<Integer> playerCountSpinner = new Spinner<>();
        playerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 4, 2));

        Button createLobbyButton = new Button("Create Lobby");
        createLobbyButton.setOnAction(e -> {
            String password = passwordField.getText();
            int maxPlayers = playerCountSpinner.getValue();
            if (!password.isEmpty()) {
                createLobby(password, maxPlayers);
            } else {
                showAlert("Error", "Please set a password.");
            }
        });

        welcomeLayout.getChildren().addAll(
                title,
                new Label("Join an existing lobby:"),
                joinCodeField,
                joinButton,
                new Label("Or create a new lobby:"),
                passwordField,
                new Label("Max players:"),
                playerCountSpinner,
                createLobbyButton
        );

        Scene scene = new Scene(welcomeLayout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToLobby(String password) {
        try {
            socket = new Socket("127.0.0.1", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("JOIN_LOBBY " + password);
            String response = in.readLine();
            if (response.equals("LOBBY_JOINED")) {
                startGameUI();
            } else {
                showAlert("Error", "Failed to join lobby: " + response);
            }
        } catch (IOException ex) {
            showAlert("Error", "Unable to connect to server.");
        }
    }

    private void createLobby(String password, int maxPlayers) {
        try {
            socket = new Socket("127.0.0.1", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("CREATE_LOBBY " + password + " " + maxPlayers);
            String response = in.readLine();
            if (response.equals("LOBBY_CREATED")) {
                startGameUI();
            } else {
                showAlert("Error", "Failed to create lobby: " + response);
            }
        } catch (IOException ex) {
            showAlert("Error", "Unable to connect to server.");
        }
    }

    private void startGameUI() {
        Stage gameStage = new Stage();
        gameStage.setTitle("Draw & Guess - Game");

        chatArea = new TextArea();
        chatArea.setEditable(false);

        messageField = new TextField();

        sendButton = new Button("Send");
        sendButton.setDisable(false);
        sendButton.setOnAction(e -> sendMessage());

        clearButton = new Button("Clear");
        clearButton.setDisable(true);
        clearButton.setOnAction(e -> {
            if (out != null) {
                out.println("CLEAR_REQUEST");
            }
        });

        sendNicknameButton = new Button("Send Nickname");
        sendNicknameButton.setOnAction(e -> sendNickname());

        canvas = new Canvas(600, 400);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        setupDrawing(gc);

        wordLabel = new Label("Waiting for the game to start...");
        wordLabel.setStyle("-fx-background-color: lightgray; -fx-padding: 5px; -fx-font-size: 14px; -fx-alignment: center;");
        wordLabel.setMaxWidth(Double.MAX_VALUE);

        VBox chatBox = new VBox(10, chatArea, new HBox(10, messageField, sendButton));
        chatBox.setPrefWidth(300);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        StackPane canvasPane = new StackPane(canvas);
        VBox canvasBox = new VBox(wordLabel, canvasPane);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(chatBox, canvasBox);
        mainSplitPane.setDividerPositions(0.3);

        if (nicknameField == null) {
            nicknameField = new TextField();
            nicknameField.setPromptText("Enter your nickname");
        }

        HBox topControls = new HBox(10, new Label("Nickname:"), nicknameField, sendNicknameButton, clearButton);

        VBox topPanel = new VBox(10, topControls);

        BorderPane root = new BorderPane();
        root.setTop(topPanel);
        root.setCenter(mainSplitPane);

        Scene scene = new Scene(root, 900, 600);
        gameStage.setScene(scene);
        gameStage.show();
    }

    private void setupDrawing(GraphicsContext gc) {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (isDrawer) {
                gc.beginPath();
                gc.moveTo(e.getX(), e.getY());
                gc.stroke();
                sendDrawCommand("PRESS " + e.getX() + " " + e.getY());
            }
        });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (isDrawer) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                sendDrawCommand("DRAG " + e.getX() + " " + e.getY());
            }
        });
    }

    private void sendDrawCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            messageField.clear();
        }
    }

    private void sendNickname() {
        String nickname = nicknameField.getText().trim();
        if (!nickname.isEmpty() && out != null) {
            out.println("NICKNAME " + nickname);
            nicknameField.setDisable(true);
            sendNicknameButton.setDisable(true);
        } else {
            showAlert("Error", "Please enter a valid nickname.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
