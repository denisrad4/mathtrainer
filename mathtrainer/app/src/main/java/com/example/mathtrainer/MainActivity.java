package com.example.mathtrainer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Элементы интерфейса
    private TextView tvLevel, tvWrongs, tvTimer, tvProblem;
    private EditText etAnswer;
    private Button btnSubmit, btnYes, btnNo, btnStart;

    // Переменные состояния игры
    private int currentLevel = 1;
    private int wrongAnswers = 0;
    private int correctInLevel = 0;
    private final int PROBLEMS_PER_LEVEL = 5;
    private final int MAX_LEVEL = 5;

    // Управление таймером
    private CountDownTimer timer;
    private long timeLeftInMillis;

    // Текущая задача
    private Problem currentProblem;

    // Флаг статуса игры
    private boolean gameInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Инициализация элементов интерфейса
        tvLevel = findViewById(R.id.tv_level);
        tvWrongs = findViewById(R.id.tv_wrongs);
        tvTimer = findViewById(R.id.tv_timer);
        tvProblem = findViewById(R.id.tv_problem);
        etAnswer = findViewById(R.id.et_answer);
        btnSubmit = findViewById(R.id.btn_submit);
        btnYes = findViewById(R.id.btn_yes);
        btnNo = findViewById(R.id.btn_no);
        btnStart = findViewById(R.id.btn_start);

        // Установка слушателей кликов кнопок
        btnStart.setOnClickListener(v -> startGame());

        btnSubmit.setOnClickListener(v -> checkNumericAnswer());
        btnYes.setOnClickListener(v -> checkYesNoAnswer("yes"));
        btnNo.setOnClickListener(v -> checkYesNoAnswer("no"));

        // Сброс интерфейса в исходное состояние
        resetUI();
    }

    // Запуск новой игры
    private void startGame() {
        currentLevel = 1;
        wrongAnswers = 0;
        correctInLevel = 0;
        gameInProgress = true;
        btnStart.setVisibility(View.GONE);
        updateUI();
        generateNextProblem();
    }

    // Генерация и отображение следующей задачи
    private void generateNextProblem() {
        currentProblem = generateProblem(currentLevel);
        tvProblem.setText(currentProblem.getDisplay());

        if (currentProblem instanceof NumericProblem) {
            etAnswer.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.VISIBLE);
            btnYes.setVisibility(View.GONE);
            btnNo.setVisibility(View.GONE);
            etAnswer.setText("");
        } else {
            etAnswer.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.GONE);
            btnYes.setVisibility(View.VISIBLE);
            btnNo.setVisibility(View.VISIBLE);
        }

        startTimer(getTimeForLevel(currentLevel));
    }

    // Запуск таймера обратного отсчета
    private void startTimer(long millis) {
        timeLeftInMillis = millis;
        if (timer != null) {
            timer.cancel();
        }
        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                tvTimer.setText("Время: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                handleWrongAnswer("Время вышло!");
            }
        }.start();
    }

    // Проверка числового ответа
    private void checkNumericAnswer() {
        String userAnswer = etAnswer.getText().toString().trim();
        if (userAnswer.isEmpty()) return;

        if (timer != null) {
            timer.cancel();
        }

        if (currentProblem.isCorrect(userAnswer)) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer("Неправильно! Правильно: " + ((NumericProblem) currentProblem).getAnswer());
        }
    }

    // Проверка ответа да/нет
    private void checkYesNoAnswer(String answer) {
        if (timer != null) {
            timer.cancel();
        }

        if (currentProblem.isCorrect(answer)) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer("Неправильно! Правильно: " + (((ComparisonProblem) currentProblem).isTrue() ? "Да" : "Нет"));
        }
    }

    // Обработка правильного ответа
    private void handleCorrectAnswer() {
        correctInLevel++;
        if (correctInLevel >= PROBLEMS_PER_LEVEL) {
            correctInLevel = 0;
            currentLevel++;
            if (currentLevel > MAX_LEVEL) {
                showGameOver("Поздравляем! Вы прошли все уровни!");
                return;
            }
        }
        updateUI();
        generateNextProblem();
    }

    // Обработка неправильного ответа
    private void handleWrongAnswer(String message) {
        wrongAnswers++;
        updateUI();
        showAlert(message);
        if (wrongAnswers >= 3) {
            showGameOver("Игра окончена! 3 ошибки.");
        } else {
            generateNextProblem();
        }
    }

    // Показать диалог оповещения
    private void showAlert(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Показать диалог окончания игры
    private void showGameOver(String message) {
        gameInProgress = false;
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Перезапустить", (dialog, which) -> startGame())
                .setNegativeButton("Выход", (dialog, which) -> finish())
                .show();
        resetUI();
    }

    // Обновление интерфейса с текущим уровнем и ошибками
    private void updateUI() {
        tvLevel.setText("Уровень: " + currentLevel);
        tvWrongs.setText("Ошибок: " + wrongAnswers);
    }

    // Сброс интерфейса в исходное состояние
    private void resetUI() {
        tvLevel.setText("Уровень: 0");
        tvWrongs.setText("Ошибок: 0");
        tvTimer.setText("Время: --");
        tvProblem.setText("");
        etAnswer.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.GONE);
        btnYes.setVisibility(View.GONE);
        btnNo.setVisibility(View.GONE);
        btnStart.setVisibility(View.VISIBLE);
    }

    // Получить лимит времени для уровня
    private long getTimeForLevel(int level) {
        return (40 - (level - 1) * 5) * 1000;
    }

    // Генерация задачи на основе уровня
    private Problem generateProblem(int level) {
        Random rand = new Random();
        boolean isComparison = rand.nextBoolean();

        int maxNum = 5 + level * 5;
        String[] ops = getOpsForLevel(level);

        if (isComparison) {
            int a = rand.nextInt(maxNum) + 1;
            int b = rand.nextInt(maxNum) + 1;
            int c = rand.nextInt(maxNum * 2) + 1;
            String op = ops[rand.nextInt(ops.length)];
            int left = calculate(a, op, b);
            boolean isTrue = left < c;
            String display = a + op + b + " < " + c + "?";
            return new ComparisonProblem(display, isTrue);
        } else {
            int numOps = level > 3 ? rand.nextInt(2) + 1 : 1;
            String expr = "";
            int result = rand.nextInt(maxNum) + 1;
            expr += result;

            for (int i = 0; i < numOps; i++) {
                String op = ops[rand.nextInt(ops.length)];
                int num;
                if (op.equals("*") || op.equals("/")) {
                    num = rand.nextInt(10) + 1;
                } else {
                    num = rand.nextInt(maxNum) + 1;
                }
                if (op.equals("/") ) {
                    while (result % num != 0) {
                        num = rand.nextInt(10) + 1;
                    }
                }
                if (op.equals("-") && num > result) {
                    num = rand.nextInt(result) + 1;
                }
                expr += op + num;
                result = calculate(result, op, num);
            }

            String display = expr + " = ?";
            return new NumericProblem(display, result);
        }
    }

    // Получить операторы для уровня
    private String[] getOpsForLevel(int level) {
        if (level == 1) return new String[]{"+", "-"};
        if (level == 2) return new String[]{"+", "-", "*"};
        return new String[]{"+", "-", "*", "/"};
    }

    // Вычислить результат операции
    private int calculate(int a, String op, int b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/": return a / b;
            default: return 0;
        }
    }

    // Абстрактный класс задачи
    private abstract class Problem {
        private String display;

        public Problem(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }

        public abstract boolean isCorrect(String answer);
    }

    // Подкласс числовой задачи
    private class NumericProblem extends Problem {
        private int answer;

        public NumericProblem(String display, int answer) {
            super(display);
            this.answer = answer;
        }

        public int getAnswer() {
            return answer;
        }

        @Override
        public boolean isCorrect(String userAnswer) {
            try {
                return Integer.parseInt(userAnswer) == answer;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    // Подкласс задачи сравнения
    private class ComparisonProblem extends Problem {
        private boolean isTrue;

        public ComparisonProblem(String display, boolean isTrue) {
            super(display);
            this.isTrue = isTrue;
        }

        public boolean isTrue() {
            return isTrue;
        }

        @Override
        public boolean isCorrect(String userAnswer) {
            return (isTrue && userAnswer.equals("yes")) || (!isTrue && userAnswer.equals("no"));
        }
    }
}