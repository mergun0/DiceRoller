package com.merg.diceroller;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceTheme;
import com.merg.diceroller.renderer.DiceGLSurfaceView;
import com.merg.diceroller.sensor.ShakeDetector;
import com.merg.diceroller.viewmodel.MainUiState;
import com.merg.diceroller.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private ShakeDetector shakeDetector;
    private DiceGLSurfaceView diceView;
    private TextView rollButton;
    private TextView singleModeButton;
    private TextView doubleModeButton;
    private TextView resultText;
    private View resultCard;
    private DiceMode displayedMode;
    private MainUiState latestState;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        bindViews();
        setupInsets();
        setupRollFlow();
        setupModeControl();
        setupShake();
        observeState();
    }

    private void bindViews() {
        FrameLayout stage = findViewById(R.id.diceStage);
        diceView = new DiceGLSurfaceView(this);
        stage.addView(diceView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        diceView.setDiceTheme(DiceTheme.all()[0]);
        diceView.setDiceMode(DiceMode.SINGLE);
        rollButton = findViewById(R.id.rollButton);
        singleModeButton = findViewById(R.id.singleModeButton);
        doubleModeButton = findViewById(R.id.doubleModeButton);
        resultText = findViewById(R.id.resultText);
        resultCard = findViewById(R.id.resultCard);
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupRollFlow() {
        rollButton.setOnClickListener(v -> requestRoll());
        viewModel.rollCommand.observe(this, command ->
                diceView.roll(command.mode, command.first, command.second, command.speed));
        diceView.setRollFinishedListener((mode, first, second) -> viewModel.finishRoll());
        viewModel.rollSettled.observe(this, roll -> {
            pulseResult();
            if (roll.mode == DiceMode.DOUBLE) {
                int total = roll.first + roll.second;
                rollButton.announceForAccessibility(getString(R.string.roll_accessibility_double, roll.first, roll.second, total));
            } else {
                rollButton.announceForAccessibility(getString(R.string.roll_accessibility_single, roll.first));
            }
        });
    }

    private void setupModeControl() {
        singleModeButton.setOnClickListener(v -> selectMode(DiceMode.SINGLE));
        doubleModeButton.setOnClickListener(v -> selectMode(DiceMode.DOUBLE));
    }

    private void setupShake() {
        shakeDetector = new ShakeDetector(this, () -> runOnUiThread(this::requestRoll));
    }

    private void observeState() {
        viewModel.state().observe(this, state -> {
            latestState = state;
            if (displayedMode != state.diceMode) {
                displayedMode = state.diceMode;
                diceView.setDiceMode(state.diceMode);
            }
            updateModeControl(state.diceMode, state.isRolling);
            updateRollButton(state.isRolling);
            updateResult(state);
        });
    }

    private void requestRoll() {
        viewModel.roll();
    }

    private void selectMode(DiceMode mode) {
        MainUiState state = latestState;
        if (state != null && state.isRolling) return;
        viewModel.setMode(mode);
    }

    private void updateModeControl(DiceMode mode, boolean rolling) {
        boolean single = mode == DiceMode.SINGLE;
        singleModeButton.setEnabled(!rolling);
        doubleModeButton.setEnabled(!rolling);
        singleModeButton.setBackgroundResource(single ? R.drawable.mode_pill_selected_bg : R.drawable.mode_pill_unselected_bg);
        doubleModeButton.setBackgroundResource(single ? R.drawable.mode_pill_unselected_bg : R.drawable.mode_pill_selected_bg);
        int selectedText = getColor(R.color.text_on_accent);
        int unselectedText = getColor(R.color.primary_navy);
        singleModeButton.setTextColor(single ? selectedText : unselectedText);
        doubleModeButton.setTextColor(single ? unselectedText : selectedText);
        singleModeButton.setAlpha(rolling ? 0.62f : 1f);
        doubleModeButton.setAlpha(rolling ? 0.62f : 1f);
    }

    private void updateRollButton(boolean rolling) {
        rollButton.setEnabled(!rolling);
        rollButton.setText(rolling ? R.string.rolling : R.string.roll_dice);
    }

    private void updateResult(MainUiState state) {
        if (state.isRolling || state.firstDiceValue == 0) {
            resultText.setText(state.diceMode == DiceMode.DOUBLE ? R.string.total_placeholder : R.string.result_placeholder);
        } else if (state.diceMode == DiceMode.DOUBLE) {
            resultText.setText(getString(R.string.double_result_value, state.firstDiceValue, state.secondDiceValue, state.total));
        } else {
            resultText.setText(getString(R.string.result_value, state.firstDiceValue));
        }
    }

    private void pulseResult() {
        resultCard.setScaleX(0.96f);
        resultCard.setScaleY(0.96f);
        resultCard.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(160L)
                .start();
    }

    @Override protected void onResume() {
        super.onResume();
        diceView.onResume();
        if (shakeDetector != null) shakeDetector.start(true);
    }

    @Override protected void onPause() {
        diceView.stopRoll();
        viewModel.cancelRoll();
        rollButton.setEnabled(true);
        if (shakeDetector != null) shakeDetector.stop();
        diceView.onPause();
        super.onPause();
    }

    @Override protected void onDestroy() {
        diceView.releaseGl();
        super.onDestroy();
    }
}
