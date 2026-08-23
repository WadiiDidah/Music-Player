package com.example.architecturedistribu;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_REQUEST_CODE = 100;
    private static final String SERVER_BASE_URL = "http://10.0.2.2:5000";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ImageView recordButton;
    private ImageView playPauseButton;
    private ImageView previousButton;
    private ImageView nextButton;
    private TextView songName;
    private TextView statusText;
    private TextView currentTime;
    private TextView totalTime;
    private SeekBar seekBar;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private File recordedFile;

    private boolean recording = false;
    private boolean playing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        bindActions();
        updatePlayerState(false);

        statusText.setText("Touchez le micro pour enregistrer une commande");
    }

    private void bindViews() {
        recordButton = findViewById(R.id.music_icon_big);
        playPauseButton = findViewById(R.id.pause_play);
        previousButton = findViewById(R.id.previous);
        nextButton = findViewById(R.id.next);
        songName = findViewById(R.id.nom_music);
        statusText = findViewById(R.id.status_text);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
    }

    private void bindActions() {

        findViewById(R.id.voice_button).setOnClickListener(view -> {
            Toast.makeText(
                    this,
                    "Micro activé",
                    Toast.LENGTH_SHORT
            ).show();

            toggleRecording();
        });
        playPauseButton.setOnClickListener(view -> togglePlayback());



        previousButton.setOnClickListener(
                view -> showMessage("Titre précédent indisponible")
        );

        nextButton.setOnClickListener(
                view -> showMessage("Titre suivant indisponible")
        );

        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar seekBar,
                    int progress,
                    boolean fromUser
            ) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    currentTime.setText(formatDuration(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        }
        );
    }

    private void toggleRecording() {

        Toast.makeText(
                this,
                "Clic micro détecté",
                Toast.LENGTH_SHORT
        ).show();

        if (recording) {
            stopRecording();
            return;
        }

        if (!hasRecordPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                        Manifest.permission.RECORD_AUDIO
                    },
                    RECORD_AUDIO_REQUEST_CODE
            );
            return;
        }

        startRecording();
    }

    private void startRecording() {
        if (recording) {
            return;
        }

        File directory = getExternalFilesDir(null);

        if (directory == null) {
            showMessage("Stockage indisponible");
            return;
        }

        recordedFile = new File(directory, "voice-command.m4a");

        try {
            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(
                    MediaRecorder.AudioSource.MIC
            );

            mediaRecorder.setOutputFormat(
                    MediaRecorder.OutputFormat.MPEG_4
            );

            mediaRecorder.setAudioEncoder(
                    MediaRecorder.AudioEncoder.AAC
            );

            mediaRecorder.setOutputFile(
                    recordedFile.getAbsolutePath()
            );

            mediaRecorder.prepare();
            mediaRecorder.start();

            recording = true;

            recordButton.setAlpha(0.55f);

            statusText.setText(
                    "Enregistrement en cours… Touchez à nouveau pour envoyer"
            );

            showMessage("Parlez maintenant");

        } catch (IOException | RuntimeException exception) {
            releaseRecorder();

            recording = false;

            recordButton.setAlpha(1f);

            statusText.setText(
                    "Impossible d'utiliser le microphone"
            );

            showMessage("Erreur microphone");
        }
    }

    private void stopRecording() {
        if (!recording) {
            return;
        }

        boolean success = true;

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (RuntimeException exception) {
            success = false;
        } finally {
            releaseRecorder();

            recording = false;

            recordButton.setAlpha(1f);
        }

        if (!success) {
            statusText.setText(
                    "Enregistrement trop court. Réessayez."
            );
            return;
        }

        if (recordedFile == null
                || !recordedFile.exists()
                || recordedFile.length() == 0) {
            statusText.setText(
                    "Aucun enregistrement disponible"
            );
            return;
        }

        statusText.setText(
                "Commande enregistrée. Envoi au serveur…"
        );

        sendAudioAsync();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != RECORD_AUDIO_REQUEST_CODE) {
            return;
        }

        if (grantResults.length > 0
                && grantResults[0]
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {
            statusText.setText(
                    "Permission microphone refusée"
            );

            showMessage(
                    "Autorisez le microphone pour utiliser les commandes vocales"
            );
        }
    }

    private boolean hasRecordPermission() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendAudioAsync() {
        executor.execute(() -> {
            try {
                JSONObject response = sendAudio(recordedFile);

                runOnUiThread(
                        () -> applyServerResponse(response)
                );

            } catch (Exception exception) {
                runOnUiThread(() -> {
                    statusText.setText(
                            "Commande enregistrée mais serveur indisponible"
                    );

                    showMessage(
                            "Impossible de joindre le serveur"
                    );
                });
            }
        });
    }

    private JSONObject sendAudio(
            File audioFile
    ) throws IOException, JSONException {

        byte[] audioBytes
                = Files.readAllBytes(audioFile.toPath());

        URL url = new URL(
                SERVER_BASE_URL + "/upload-audio"
        );

        HttpURLConnection connection
                = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/octet-stream"
        );

        try (
                OutputStream outputStream
                = connection.getOutputStream()) {
            outputStream.write(audioBytes);
        }

        int statusCode = connection.getResponseCode();

        if (statusCode < 200 || statusCode >= 300) {
            connection.disconnect();

            throw new IOException(
                    "HTTP " + statusCode
            );
        }

        String response;

        try (
                InputStream inputStream
                = connection.getInputStream()) {
            response = new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }

        connection.disconnect();

        return new JSONObject(response);
    }

    private void applyServerResponse(
            JSONObject response
    ) {
        String action = response.optString(
                "action",
                response.optString(
                        "type_opeation",
                        ""
                )
        );

        String track = response.optString(
                "track",
                response.optString(
                        "nom",
                        ""
                )
        );

        if (!track.isBlank()) {
            songName.setText(track);
        }

        if ("play".equalsIgnoreCase(action)
                || "start".equalsIgnoreCase(action)) {
            statusText.setText(
                    "Lecture demandée : " + track
            );

        } else if ("stop".equalsIgnoreCase(action)) {
            statusText.setText(
                    "Arrêt demandé"
            );

        } else if ("delete".equalsIgnoreCase(action)
                || "supprimer".equalsIgnoreCase(action)) {
            statusText.setText(
                    "Suppression demandée : " + track
            );

        } else {
            statusText.setText(
                    "Commande reçue du serveur"
            );
        }
    }

    private void togglePlayback() {
        if (mediaPlayer != null && playing) {
            mediaPlayer.pause();

            playing = false;

            updatePlayerState(false);

            statusText.setText(
                    "Lecture en pause"
            );

            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.start();

            playing = true;

            updatePlayerState(true);

            statusText.setText(
                    "Lecture en cours"
            );

            updateProgress();

            return;
        }

        startRecordedPlayback();
    }

    private void startRecordedPlayback() {
        if (recordedFile == null
                || !recordedFile.exists()) {
            showMessage(
                    "Enregistrez d'abord une commande vocale"
            );
            return;
        }

        releasePlayer();

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(
                    recordedFile.getAbsolutePath()
            );

            mediaPlayer.setOnPreparedListener(
                    player -> {
                        seekBar.setMax(
                                player.getDuration()
                        );

                        totalTime.setText(
                                formatDuration(
                                        player.getDuration()
                                )
                        );

                        player.start();

                        playing = true;

                        updatePlayerState(true);

                        statusText.setText(
                                "Lecture de l'enregistrement"
                        );

                        updateProgress();
                    }
            );

            mediaPlayer.setOnCompletionListener(
                    player -> {
                        playing = false;

                        updatePlayerState(false);

                        seekBar.setProgress(0);

                        currentTime.setText("0:00");

                        statusText.setText(
                                "Lecture terminée"
                        );
                    }
            );

            mediaPlayer.prepareAsync();

        } catch (IOException exception) {
            releasePlayer();

            showMessage(
                    "Impossible de lire l'enregistrement"
            );
        }
    }

    private void updateProgress() {
        if (mediaPlayer == null || !playing) {
            return;
        }

        int position
                = mediaPlayer.getCurrentPosition();

        seekBar.setProgress(position);

        currentTime.setText(
                formatDuration(position)
        );

        seekBar.postDelayed(
                this::updateProgress,
                500
        );
    }

    private String formatDuration(
            int milliseconds
    ) {
        int totalSeconds
                = milliseconds / 1000;

        int minutes
                = totalSeconds / 60;

        int seconds
                = totalSeconds % 60;

        return String.format(
                Locale.getDefault(),
                "%d:%02d",
                minutes,
                seconds
        );
    }

    private void updatePlayerState(
            boolean isPlaying
    ) {
        playPauseButton.setImageResource(
                isPlaying
                        ? R.drawable.ic_baseline_pause_circle_outline_24
                        : R.drawable.ic_baseline_play_circle_outline_24
        );
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (RuntimeException ignored) {
            }

            mediaRecorder = null;
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (RuntimeException ignored) {
            }

            mediaPlayer = null;
        }

        playing = false;
    }

    private void showMessage(
            String message
    ) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        releaseRecorder();
        releasePlayer();

        executor.shutdownNow();

        super.onDestroy();
    }
}
