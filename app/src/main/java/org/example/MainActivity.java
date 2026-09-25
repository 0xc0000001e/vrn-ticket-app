package org.example;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etFirstName;
    private EditText etLastName;
    private EditText etBirthDate;
    private EditText etValidFrom;
    private EditText etValidTo;
    private EditText etTicketNumber;

    private TextView tvTicketHeader;
    private TextView tvPassengerInfo;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private ImageView ivQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация полей ввода
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etValidFrom = findViewById(R.id.etValidFrom);
        etValidTo = findViewById(R.id.etValidTo);
        etTicketNumber = findViewById(R.id.etTicketNumber);

        // Инициализация элементов отображения билета
        tvTicketHeader = findViewById(R.id.tvTicketHeader);
        tvPassengerInfo = findViewById(R.id.tvPassengerInfo);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        ivQrCode = findViewById(R.id.ivQrCode);

        Button btnGenerate = findViewById(R.id.btnGenerate);

        // Вызов календаря при клике на поля дат
        setupDatePicker(etBirthDate);
        setupDatePicker(etValidFrom);
        setupDatePicker(etValidTo);

        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateTicket();
            }
        });
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
                    // Форматирование даты в немецкий стандарт DD.MM.YYYY
                    String formattedDate = String.format(Locale.GERMANY, "%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear);
                    targetEditText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void generateTicket() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();
        String validFrom = etValidFrom.getText().toString().trim();
        String validTo = etValidTo.getText().toString().trim();
        String ticketNumber = etTicketNumber.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || birthDate.isEmpty()) {
            return;
        }

        // Блоки и названия на немецком языке
        tvTicketHeader.setText("Deutschlandticket / VRN Fahrkarte");
        tvPassengerInfo.setText(String.format("Name: %s %s\nGeburtsdatum: %s", firstName, lastName, birthDate));
        tvValidityInfo.setText(String.format("Gültig ab: %s\nGültig bis: %s", validFrom, validTo));
        tvTicketNumberInfo.setText(String.format("Ticket-Nr.: %s", ticketNumber.isEmpty() ? "DE99823411" : ticketNumber));

        // Данные для генерации QR-кода
        String qrContent = String.format(
                "DEUTSCHLANDTICKET|Name:%s %s|DOB:%s|ValidFrom:%s|ValidTo:%s|TicketNo:%s",
                firstName, lastName, birthDate, validFrom, validTo, ticketNumber
        );

        Bitmap qrBitmap = generateQrCodeBitmap(qrContent, 500, 500);
        if (qrBitmap != null) {
            ivQrCode.setImageBitmap(qrBitmap);
            ivQrCode.setVisibility(View.VISIBLE);
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
