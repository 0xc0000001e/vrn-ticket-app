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

        // Инициализация UI компонентов
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

        // Создаем Пассажира с использованием LocalDate
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        Passenger passenger = new Passenger("Max", "Mustermann", birthDate, "max.mustermann@example.com");

        // Создаем Билет согласно конструктору класса DeutschlandTicket
        String ticketId = "VRN-DT-89230492";
        YearMonth validityMonth = YearMonth.of(2026, 10);
        BigDecimal price = new BigDecimal("49.00");
        String cardNumber = "DE89370001";
        TicketStatus status = TicketStatus.VALID; // Использование перечисления TicketStatus
        EnumSet<TransitType> transitTypes = EnumSet.allOf(TransitType.class); // Все типы транспорта

        DeutschlandTicket ticket = new DeutschlandTicket(
                ticketId,
                passenger,
                validityMonth,
                price,
                cardNumber,
                status,
                transitTypes
        );

        // Отображение данных билета
        displayTicketData(ticket);

        // Запуск динамической полосы защиты
        startSecurityShimmerAnimation();
    }

    private void displayTicketData(DeutschlandTicket ticket) {
        Passenger passenger = ticket.getPassenger();

        // Форматирование даты рождения и срока действия
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String fullName = passenger.getFirstName() + " " + passenger.getLastName();
        String formattedDob = passenger.getBirthDate().format(dateFormatter);
        
        // Конец месяца действия билета (например, 31.10.2026)
        LocalDate validUntilDate = ticket.getValidityMonth().atEndOfMonth();
        String formattedValidUntil = validUntilDate.format(dateFormatter);

        tvPassengerName.setText(fullName);
        tvPassengerDob.setText(formattedDob);
        tvValidityInfo.setText(formattedValidUntil);
        tvTicketNumberInfo.setText(ticket.getTicketId());

        // Формирование строки для Aztec-кода
        String barcodeData = String.format("VRN|%s|%s|%s",
                ticket.getTicketId(),
                fullName,
                formattedValidUntil);

        generateDenseAztecCode(barcodeData);
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
