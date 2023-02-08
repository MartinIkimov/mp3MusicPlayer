package com.example.mp3musicplayer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {

    @FXML
    private Pane pane;
    @FXML
    private Label songLabel;
    @FXML
    private Button playButton, pauseButton, resetButton, previousButton, nextButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;
    @FXML
    private MenuItem menuItemAddSongs;

    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private boolean paused = true;

    private int songNumber;
    private int[] speeds = {25,50,75,100,125,150,175,200};

    private Media media;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private TimerTask task;
    private boolean running;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        songs = new ArrayList<>();
//        directory = new File("src/main/resources/com/example/mp3musicplayer/music");
//        files = directory.listFiles();
//
//        if(files != null) {
//            songs.addAll(Arrays.asList(files));
//        }



        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(speeds[i] + "%");
        }
        speedBox.setOnAction(this::changeSpeed);

        volumeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
            }
        });

        timer = new Timer();

        songProgressBar.setStyle("-fx-accent: #16D3A1;");
        disableButtons(true);

        playButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(Mp3Player.class.getResourceAsStream("play-icon.png")))));

        menuItemAddSongs.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Audio Files", "*.mp3"));
            List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
            if (selectedFiles != null) {
                songs.addAll(selectedFiles);
                if(media == null) {
                    media = new Media(songs.get(songNumber).toURI().toString());
                    mediaPlayer = new MediaPlayer(media);
                    mediaPlayer.setOnEndOfMedia(this::nextMedia);


                    songLabel.setText(songs.get(songNumber).getName());

                    disableButtons(false);
                }
                if(paused) {
                    playMedia();
                }
            }
        });
    }

    private void disableButtons(boolean value) {
        playButton.setDisable(value);
        nextButton.setDisable(value);
        previousButton.setDisable(value);
        volumeSlider.setDisable(value);
        resetButton.setDisable(value);
        speedBox.setDisable(value);
    }

    public void playMedia() {
        toggleMedia();
        changeSpeed(null);
        mediaPlayer.setVolume(volumeSlider.getValue() * 0.01);
    }

    public void toggleMedia() {
        if (!paused) {
            cancelTimer();
            mediaPlayer.pause();
            paused = true;
            playButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(Mp3Player.class.getResourceAsStream("play-icon.png")))));
        } else {
            beginTimer();
            mediaPlayer.play();
            paused = false;
            playButton.setGraphic(new ImageView(new Image(Objects.requireNonNull(Mp3Player.class.getResourceAsStream("pause.png")))));
        }
    }
    public void resetMedia() {
        songProgressBar.setProgress(0);
        mediaPlayer.seek(Duration.seconds(0));
    }
    public void previousMedia() {
        if(songNumber > 0) {
            songNumber--;
        } else {
            songNumber = songs.size() - 1;
        }
        changeSong();
    }
    public void nextMedia() {
        if(songNumber < songs.size() - 1) {
            songNumber++;
        } else {
            songNumber = 0;
        }
        changeSong();
    }

    private void changeSong() {
        mediaPlayer.stop();
        if(running) {
            cancelTimer();
        }
        media = new Media(songs.get(songNumber).toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        songLabel.setText(songs.get(songNumber).getName());
        paused = !paused;
        playMedia();
    }

    public void changeSpeed(ActionEvent event) {
        if(speedBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)) * 0.01);
        }
    }
    public void beginTimer() {
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                running = true;
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = media.getDuration().toSeconds();
                songProgressBar.setProgress(current/end);

                if(current/end == 1) {
                    cancelTimer();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
    }
    public void cancelTimer() {
        running = false;
        timer.cancel();
    }
}
