package org.example;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // Элементы формы ввода
    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthDate;
    private EditText etValidFrom;
    private EditText etValidTo;
    private Button btnGenerate;

    // Элементы отображения билета
    private ImageView qrCodeImage;
    private TextView tvPassengerName;
    private TextView tvPassengerDob;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private View shimmerBar;
    private View securityBlock;
    private BottomNavigationView bottomNavigation;

    // Аниматоры
    private ObjectAnimator rotationAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            initViews();
            setupAutoDateCalculation();
            setupNavigation();

            // Заполнение начальными данными по умолчанию
            etFirstName.setText("Max");
            etLastName.setText("Mustermann");
            etBirthDate.setText("01.01.1990");
            
            // Устанавливаем текущий месяц: 1-е число и последний день месяца
            LocalDate today = LocalDate.now();
            LocalDate firstDay = today.withDayOfMonth(1);
            LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());

            etValidFrom.setText(firstDay.format(DATE_FORMATTER));
            etValidTo.setText(lastDay.format(DATE_FORMATTER));

            // Обработка кнопки генерации билета
            btnGenerate.setOnClickListener(v -> updateTicketFromInput());

            // Первая генерация при запуске
            updateTicketFromInput();

            // Запуск анимаций: вращение кода и бегающий блик
            startAztecRotationAnimation();
            startSecurityShimmerAnimation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    private void initViews() {
        // Поля ввода
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etValidFrom = findViewById(R.id.etValidFrom);
        etValidTo = findViewById(R.id.etValidTo);
        btnGenerate = findViewById(R.id.btnGenerate);

        // Карточка билета
        qrCodeImage = findViewById(R.id.qrCodeImage);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerDob = findViewById(R.id.tvPassengerDob);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        shimmerBar = findViewById(R.id.shimmerBar);
        securityBlock = findViewById(R.id.securityBlock);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    /**
     * Динамический расчёт дат: при изменении даты начала (etValidFrom)
     * автоматически подставляется 1-е число и последний день этого же месяца в etValidTo.
     */
    private void setupAutoDateCalculation() {
        if (etValidFrom == null) return;

        etValidFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (input.length() == 10) { // Ожидаем формат dd.MM.yyyy
                    try {
                        LocalDate parsedDate = LocalDate.parse(input, DATE_FORMATTER);
                        
                        // Первый и последний дни выбранного месяца
                        LocalDate firstDayOfMonth = parsedDate.withDayOfMonth(1);
                        LocalDate lastDayOfMonth = parsedDate.withDayOfMonth(parsedDate.lengthOfMonth());

                        // Корректируем поле "От" на 1-е число месяца, если ввели другое
                        if (!parsedDate.equals(firstDayOfMonth)) {
                            etValidFrom.removeTextChangedListener(this);
                            etValidFrom.setText(firstDayOfMonth.format(DATE_FORMATTER));
                            etValidFrom.addTextChangedListener(this);
                        }

                        // Устанавливаем поле "До" на последний день этого же месяца
                        if (etValidTo != null) {
                            etValidTo.setText(lastDayOfMonth.format(DATE_FORMATTER));
                        }
                    } catch (Exception ignored) {
                        // Игнорируем неполный или некорректный ввод даты
                    }
                }
            }
        });
    }

    private void setupNavigation() {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_fahrkarten);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                return itemId == R.id.nav_fahrkarten || itemId == R.id.nav_fahrplan || itemId == R.id.nav_profil;
            });
        }
    }

    private void updateTicketFromInput() {
        try {
            String firstName = etFirstName != null ? etFirstName.getText().toString().trim() : "Max";
            String lastName = etLastName != null ? etLastName.getText().toString().trim() : "Mustermann";
            String fullName = firstName + " " + lastName;

            String dob = etBirthDate != null ? etBirthDate.getText().toString().trim() : "01.01.1990";
            String validTo = etValidTo != null ? etValidTo.getText().toString().trim() : "31.10.2026";
            String ticketId = "VRN-DT-89230492";

            if (tvPassengerName != null) tvPassengerName.setText(fullName);
            if (tvPassengerDob != null) tvPassengerDob.setText(dob);
            if (tvValidityInfo != null) tvValidityInfo.setText(validTo);
            if (tvTicketNumberInfo != null) tvTicketNumberInfo.setText(ticketId);

            // Генерация Aztec-кода
            String barcodeData = String.format("VRN|%s|%s|%s", ticketId, fullName, validTo);
            generateDenseAztecCode(barcodeData);

        } catch (Exception e) {
            Log.e(TAG, "Error updating ticket data", e);
        }
    }

    private void generateDenseAztecCode(String data) {
        if (qrCodeImage == null) return;

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 0);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.AZTEC, 600, 600, hints);

            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrCodeImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error generating Aztec code", e);
        }
    }

    /**
     * Анимация непрерывного вращения Aztec-кода по часовой стрелке
     */
    private void startAztecRotationAnimation() {
        if (qrCodeImage == null) return;

        rotationAnimator = ObjectAnimator.ofFloat(qrCodeImage, "rotation", 0f, 360f);
        rotationAnimator.setDuration(12000); // Полный оборот за 12 секунд
        rotationAnimator.setInterpolator(new LinearInterpolator());
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotationAnimator.start();
    }

    /**
     * Анимация бегающего блика на блоке защиты
     */
    private void startSecurityShimmerAnimation() {
        if (securityBlock == null || shimmerBar == null) return;

        securityBlock.post(() -> {
            try {
                int parentWidth = securityBlock.getWidth();
                int shimmerWidth = shimmerBar.getWidth();

                if (parentWidth <= 0) parentWidth = 1000;
                if (shimmerWidth <= 0) shimmerWidth = 150;

                ObjectAnimator animator = ObjectAnimator.ofFloat(
                        shimmerBar,
                        "translationX",
                        -shimmerWidth,
                        parentWidth
                );
                animator.setDuration(2200);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.start();
            } catch (Exception e) {
                Log.e(TAG, "Error starting shimmer animation", e);
            }
        });
    }
}
