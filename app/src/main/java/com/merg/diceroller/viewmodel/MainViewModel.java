package com.merg.diceroller.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.merg.diceroller.model.AnimationSpeed;
import com.merg.diceroller.model.AppTheme;
import com.merg.diceroller.model.DiceMode;
import com.merg.diceroller.model.DiceRoll;
import com.merg.diceroller.model.DiceTheme;
import com.merg.diceroller.repository.DiceRepository;
import com.merg.diceroller.util.DiceRandom;
import java.util.List;

public final class MainViewModel extends AndroidViewModel {
    private final DiceRepository repository;
    private final DiceRandom random = new DiceRandom();
    private final MutableLiveData<MainUiState> state = new MutableLiveData<>();
    public final SingleLiveEvent<RollCommand> rollCommand = new SingleLiveEvent<>();
    public final SingleLiveEvent<DiceRoll> rollSettled = new SingleLiveEvent<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new DiceRepository(application);
        publish(0, 0, false);
    }

    public LiveData<MainUiState> state() { return state; }

    public boolean roll() {
        MainUiState current = state.getValue();
        if (current != null && current.isRolling) return false;
        DiceMode mode = current == null ? repository.diceMode() : current.diceMode;
        int first = random.nextDie();
        int second = mode == DiceMode.DOUBLE ? random.nextDie() : 0;
        publish(first, second, true);
        rollCommand.setValue(new RollCommand(mode, first, second, repository.animationSpeed()));
        return true;
    }

    public void finishRoll() {
        MainUiState current = state.getValue();
        if (current == null || !current.isRolling) return;
        DiceRoll roll = new DiceRoll(current.diceMode, current.firstDiceValue, current.secondDiceValue, System.currentTimeMillis());
        repository.addRoll(roll);
        publish(current.firstDiceValue, current.secondDiceValue, false);
        rollSettled.setValue(roll);
    }

    public void cancelRoll() {
        MainUiState current = state.getValue();
        if (current != null) publish(current.firstDiceValue, current.secondDiceValue, false);
    }

    public void setMode(DiceMode mode) {
        MainUiState s = state.getValue();
        if (s != null && s.isRolling) return;
        repository.setDiceMode(mode);
        publish(0, 0, false);
    }
    public void setSound(boolean value) { repository.setSoundEnabled(value); republish(); }
    public void setVibration(boolean value) { repository.setVibrationEnabled(value); republish(); }
    public void setShake(boolean value) { repository.setShakeEnabled(value); republish(); }
    public void setDiceTheme(DiceTheme value) { repository.setDiceTheme(value); republish(); }
    public void setAppTheme(AppTheme value) { repository.setAppTheme(value); republish(); }
    public void setAnimationSpeed(AnimationSpeed value) { repository.setAnimationSpeed(value); republish(); }
    public void clearHistory() { repository.clearHistory(); republish(); }

    private void republish() {
        MainUiState s = state.getValue();
        publish(s == null ? 0 : s.firstDiceValue, s == null ? 0 : s.secondDiceValue, s != null && s.isRolling);
    }

    private void publish(int first, int second, boolean rolling) {
        List<DiceRoll> history = repository.history();
        state.setValue(new MainUiState(repository.diceMode(), first, second, rolling, repository.diceTheme(),
                repository.soundEnabled(), repository.vibrationEnabled(), repository.shakeEnabled(),
                repository.appTheme(), repository.animationSpeed(), history));
    }
}
