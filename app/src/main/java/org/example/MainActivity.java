package org.example;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView qrCodeImage;
    private TextView tvPassengerName;
    private TextView tvPassengerDob;
    private TextView tvValidityInfo;
    private TextView tvTicketNumberInfo;
    private View shimmerBar;
    private View securityBlock;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Инициализация View
        qrCodeImage = findViewById(R.id.qrCodeImage);
        tvPassengerName = findViewById(R.id.tvPassengerName);
        tvPassengerDob = findViewById(R.id.tvPassengerDob);
        tvValidityInfo = findViewById(R.id.tvValidityInfo);
        tvTicketNumberInfo = findViewById(R.id.tvTicketNumberInfo);
        shimmerBar = findViewById(R.id.shimmerBar);
        securityBlock = findViewById(R.id.securityBlock);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Активная вкладка Fahrkarten
        bottomNavigation.setSelectedItemId(R.id.nav_fahrkarten);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            return itemId == R.id.nav_fahrkarten || itemId == R.id.nav_fahrplan || itemId == R.id.nav_profil;
        });

        // Создание объекта Passenger
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Passenger passenger = new Passenger("Max", "Mustermann", birthDate, "max.mustermann@example.com");

        // Инициализация TicketStatus (получаем первый активный статус из Enum)
        TicketStatus status = TicketStatus.values()[0]; 

        // Создание объекта DeutschlandTicket
        String ticketId = "VRN-DT-89230492";
        YearMonth validityMonth = YearMonth.of(2026, 10);
        BigDecimal price = new BigDecimal("49.00");
        String cardNumber = "DE89370001";
        EnumSet<TransitType> transitTypes = EnumSet.allOf(TransitType.class);

        DeutschlandTicket ticket = new DeutschlandTicket(
                ticketId,
                passenger,
                validityMonth,
                price,
                cardNumber,
                status,
                transitTypes
        );

        // Отображение билета
        displayTicketData(ticket, passenger, validityMonth, ticketId);

        // Запуск анимации защиты
        startSecurityShimmerAnimation();
    }

    private void displayTicketData(DeutschlandTicket ticket, Passenger passenger, YearMonth validityMonth, String ticketId) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        
        // Чтение данных пассажира (совместимо с Record и стандартными геттерами)
        String firstName = getPassengerFirstName(passenger);
        String lastName = getPassengerLastName(passenger);
        LocalDate dob = getPassengerBirthDate(passenger);

        String fullName = firstName + " " + lastName;
        String formattedDob = dob != null ? dob.format(dateFormatter) : "01.01.1990";
        
        LocalDate validUntilDate = validityMonth.atEndOfMonth();
        String formattedValidUntil = validUntilDate.format(dateFormatter);

        tvPassengerName.setText(fullName);
        tvPassengerDob.setText(formattedDob);
        tvValidityInfo.setText(formattedValidUntil);
        tvTicketNumberInfo.setText(ticketId);

        // Генерация Aztec-кода
        String barcodeData = String.format("VRN|%s|%s|%s",
                ticketId,
                fullName,
                formattedValidUntil);

        generateDenseAztecCode(barcodeData);
    }

    private String getPassengerFirstName(Passenger p) {
        try {
            return p.firstName();
        } catch (NoSuchMethodError e) {
            return "Max";
        }
    }

    private String getPassengerLastName(Passenger p) {
        try {
            return p.lastName();
        } catch (NoSuchMethodError e) {
            return "Mustermann";
        }
    }

    private LocalDate getPassengerBirthDate(Passenger p) {
        try {
            return p.birthDate();
        } catch (NoSuchMethodError e) {
            return LocalDate.of(1990, 1, 1);
        }
    }

    private void generateDenseAztecCode(String data) {
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
            e.printStackTrace();
        }
    }

    private void startSecurityShimmerAnimation() {
        securityBlock.post(() -> {
            int parentWidth = securityBlock.getWidth();
            int shimmerWidth = shimmerBar.getWidth();

            ObjectAnimator animator = ObjectAnimator.ofFloat(
                    shimmerBar,
                    "translationX",
                    -shimmerWidth,
                    parentWidth
            );
            animator.setDuration(2000);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();
        });
    }
}
