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
import android.widget.LinearLayout;
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

    private LinearLayout layoutTicketHeader;
    private CardView cardTicket;
    private CardView cardInputForm;
    private TextView tvTicketHeader;
    private TextView tvPassengerName;
    private TextView tvPassengerDob;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private ImageView ivQrCode;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Инициализация
        layoutTicketHeader = findViewById(R.id.layoutTicketHeader);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etValidFrom = findViewById(R.id.etValidFrom);
        etValidTo = findViewById(R.id.etValidTo);

        cardTicket = findViewById(R.id.cardTicket);
        cardInputForm = findViewById(R.id.cardInputForm);
        tvTicketHeader = findViewById(R.id.tvTicketHeader);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerDob = findViewById(R.id.tvPassengerDob);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        ivQrCode = findViewById(R.id.ivQrCode);

        Button btnGenerate = findViewById(R.id.btnGenerate);

        // Настройка дат
        setupDatePicker(etBirthDate);
        setupDatePicker(etValidFrom);
        setupDatePicker(etValidTo);

        // 2D-вращение QR
        ivQrCode.setOnClickListener(this::spinQrCode2D);

        // Нажатие на синий заголовок билета переключает форму редактирования
        layoutTicketHeader.setOnClickListener(v -> toggleInputForm());

        btnGenerate.setOnClickListener(v -> generateAndSaveTicket());

        // Загрузка сохраненного билета
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

    private String generateNewTicketId() {
        int randomNum = 10000000 + new Random().nextInt(90000000);
        String newId = "VRN-DT-" + randomNum;
        sharedPreferences.edit().putString("ticketId", newId).apply();
        return newId;
    }

    private String[] getCurrentMonthDates() {
        Calendar cal = Calendar.getInstance();
        int curYear = cal.get(Calendar.YEAR);
        int curMonth = cal.get(Calendar.MONTH) + 1; // 1..12

        String validFrom = String.format(Locale.GERMANY, "01.%02d.%04d", curMonth, curYear);

        cal.add(Calendar.MONTH, 1);
        int nextYear = cal.get(Calendar.YEAR);
        int nextMonth = cal.get(Calendar.MONTH) + 1;

        String validTo = String.format(Locale.GERMANY, "01.%02d.%04d", nextMonth, nextYear);

        return new String[]{validFrom, validTo, String.format(Locale.GERMANY, "%02d.%04d", curMonth, curYear)};
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

        if (validFrom.isEmpty() || validTo.isEmpty()) {
            String[] autoDates = getCurrentMonthDates();
            validFrom = autoDates[0];
            validTo = autoDates[1];
            etValidFrom.setText(validFrom);
            etValidTo.setText(validTo);
        }

        String ticketId = sharedPreferences.getString("ticketId", "");
        if (ticketId.isEmpty()) {
            ticketId = generateNewTicketId();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("birthDate", birthDate);
        editor.putString("validFrom", validFrom);
        editor.putString("validTo", validTo);
        editor.apply();

        displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);
        cardInputForm.setVisibility(View.GONE);
    }

    private void loadSavedTicketData() {
        String firstName = sharedPreferences.getString("firstName", "");
        String lastName = sharedPreferences.getString("lastName", "");
        String birthDate = sharedPreferences.getString("birthDate", "");

        String[] currentMonthInfo = getCurrentMonthDates();
        String autoValidFrom = currentMonthInfo[0];
        String autoValidTo = currentMonthInfo[1];
        String currentMonthKey = currentMonthInfo[2];

        String lastProcessedMonth = sharedPreferences.getString("lastMonthKey", "");
        String ticketId = sharedPreferences.getString("ticketId", "");

        // Автосмена даты и ID при смене месяца
        if (!currentMonthKey.equals(lastProcessedMonth)) {
            ticketId = generateNewTicketId();
            sharedPreferences.edit()
                    .putString("lastMonthKey", currentMonthKey)
                    .putString("validFrom", autoValidFrom)
                    .putString("validTo", autoValidTo)
                    .apply();
        }

        String validFrom = sharedPreferences.getString("validFrom", autoValidFrom);
        String validTo = sharedPreferences.getString("validTo", autoValidTo);

        etFirstName.setText(firstName);
        etLastName.setText(lastName);
        etBirthDate.setText(birthDate);
        etValidFrom.setText(validFrom);
        etValidTo.setText(validTo);

        if (!firstName.isEmpty() && !lastName.isEmpty() && !birthDate.isEmpty()) {
            displayTicket(firstName, lastName, birthDate, validFrom, validTo, ticketId);
            cardInputForm.setVisibility(View.GONE);
        } else {
            cardInputForm.setVisibility(View.VISIBLE);
        }
    }

    private void displayTicket(String firstName, String lastName, String birthDate, String validFrom, String validTo, String ticketId) {
        tvTicketHeader.setText("VRN Ticket");
        tvPassengerName.setText(String.format("%s %s", firstName, lastName));
        tvPassengerDob.setText(birthDate);
        tvValidityInfo.setText(String.format("%s - %s", validFrom, validTo));
        tvTicketNumberInfo.setText(String.format("ID Ticket: %s", ticketId));

        String qrContent = String.format(
                "VRN|DEUTSCHLANDTICKET|Name:%s %s|DOB:%s|ValidFrom:%s|ValidTo:%s|ID:%s",
                firstName, lastName, birthDate, validFrom, validTo, ticketId
        );

        Bitmap qrBitmap = generateQrCodeBitmap(qrContent, 1400, 1400);
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
