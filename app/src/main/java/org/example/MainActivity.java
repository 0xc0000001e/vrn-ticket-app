package org.example;

import org.example.R;

import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "VrnTicketPrefs";

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthDate;
    private EditText etValidFrom;
    private EditText etValidTo;

    private TextView tvVrnLogo;
    private CardView cardTicket;
    private CardView cardInputForm;
    private TextView tvTicketHeader;
    private TextView tvPassengerInfo;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private ImageView ivQrCode;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Инициализация элементов
        tvVrnLogo = findViewById(R.id.tvVrnLogo);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etValidFrom = findViewById(R.id.etValidFrom);
        etValidTo = findViewById(R.id.etValidTo);

        cardTicket = findViewById(R.id.cardTicket);
        cardInputForm = findViewById(R.id.cardInputForm);
        tvTicketHeader = findViewById(R.id.tvTicketHeader);
        tvPassengerInfo = findViewById(R.id.tvPassengerInfo);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        ivQrCode = findViewById(R.id.ivQrCode);

        Button btnGenerate = findViewById(R.id.btnGenerate);

        // Выбор дат
        setupDatePicker(etBirthDate);
        setupDatePicker(etValidFrom);
        setupDatePicker(etValidTo);

        // 2D-вращение QR-кода при нажатии
        ivQrCode.setOnClickListener(this::spinQrCode2D);

        // Нажатие на логотип VRN переключает видимость формы редактирования
        tvVrnLogo.setOnClickListener(v -> toggleInputForm());

        btnGenerate.setOnClickListener(v -> generateAndSaveTicket());

        // Загрузка сохранённых данных
        loadSavedTicketData();
    }

    private void setupDatePicker(EditText editText) {
        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setOnClickListener(v -> showDatePickerDialog(editText));
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                MainActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.GERMANY, "%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear);
                    targetEditText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void spinQrCode2D(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        animator.setDuration(800);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    private void toggleInputForm() {
        if (cardInputForm.getVisibility() == View.VISIBLE) {
            cardInputForm.setVisibility(View.GONE);
            clearFocusAndHideKeyboard();
        } else {
            cardInputForm.setVisibility(View.VISIBLE);
        }
    }

    private void clearFocusAndHideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private String generateOrGetTicketId() {
        String savedId = sharedPreferences.getString("ticketId", "");
        if (savedId.isEmpty()) {
            int randomNum = 10000000 + new Random().nextInt(90000000);
            savedId = "VRN-DT-" + randomNum;
            sharedPreferences.edit().putString("ticketId", savedId).apply();
        }
        return savedId;
    }

    private void generateAndSaveTicket() {
        clearFocusAndHideKeyboard();

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String validFrom = etValidFrom.getText().toString().trim();
        String validTo = etValidTo.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty()) {
            return;
        }

        String ticketId = generateOrGetTicketId();

        // Сохранение
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("birthDate", birthDate);
        editor.putString("validFrom", validFrom);
        editor.putString("validTo", validTo);
        editor.apply();

        displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);

        // Прячем форму ввода после генерации — остаётся только карточка билета
        cardInputForm.setVisibility(View.GONE);
    }

    private void loadSavedTicketData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String birthDate = sharedPreferences.getString("birthDate", "");
        String validFrom = sharedPreferences.getString("validFrom", "");
        String validTo = sharedPreferences.getString("validTo", "");
        String ticketId = sharedPreferences.getString("ticketId", "");

        etFirstName.setText(firstName);
        etLastName.setText(lastName);
        etBirthDate.setText(birthDate);
        etValidFrom.setText(validFrom);
        etValidTo.setText(validTo);

        if (!firstName.isEmpty() && !lastName.isEmpty() && !birthDate.isEmpty()) {
            if (ticketId.isEmpty()) {
                ticketId = generateOrGetTicketId();
            }
            displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);
            cardInputForm.setVisibility(View.GONE); // При запуске показываем только готовый билет
        }
    }

    private void displayTicket(String firstName, String lastName, String birthDate, String validFrom, String validTo, String ticketId) {
        tvTicketHeader.setText("Deutschlandticket");
        tvPassengerInfo.setText(String.format("Inhaber: %s %s\nGeburtsdatum: %s", firstName, lastName, birthDate));
        tvValidityInfo.setText(String.format("Gültig ab: %s\nGültig bis: %s", validFrom, validTo));
        tvTicketNumberInfo.setText(String.format("ID Ticket: %s", ticketId));

        String qrContent = String.format(
                "VRN|DEUTSCHLANDTICKET|Name:%s %s|DOB:%s|ValidFrom:%s|ValidTo:%s|ID:%s",
                firstName, lastName, birthDate, validFrom, validTo, ticketId
        );

        Bitmap qrBitmap = generateQrCodeBitmap(qrContent, 800, 800);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
            cardTicket.setVisibility(View.VISIBLE);
        }
    }

    private Bitmap generateQrCodeBitmap(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    text, BarcodeFormat.QR_CODE, width, height);
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
