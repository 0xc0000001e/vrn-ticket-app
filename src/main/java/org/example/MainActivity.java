package org.example;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class MainActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etDob, etDocNum;
    private LinearLayout formLayout, ticketLayout;
    private TextView tvTicketDetails, tvQrCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Главный контейнер с прокруткой
        ScrollView scrollView = new ScrollView(this);
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(40, 40, 40, 40);
        scrollView.addView(mainLayout);

        // 1. Форма ввода данных
        formLayout = new LinearLayout(this);
        formLayout.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("VRN Deutschlandticket\nDaten eingeben");
        tvTitle.setTextSize(22);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(0, 0, 0, 30);
        formLayout.addView(tvTitle);

        etFirstName = createInputField("Vorname", "z.B. Alex");
        etLastName = createInputField("Nachname", "z.B. Müller");
        etDob = createInputField("Geburtsdatum (DD.MM.YYYY)", "z.B. 12.04.1998");
        etDocNum = createInputField("Ausweisnummer", "z.B. DE987654321");

        Button btnGenerate = new Button(this);
        btnGenerate.setText("Ticket erstellen");
        btnGenerate.setPadding(20, 20, 20, 20);
        btnGenerate.setOnClickListener(v -> generateTicket());
        formLayout.addView(btnGenerate);

        mainLayout.addView(formLayout);

        // 2. Экран билета (скрыт по умолчанию)
        ticketLayout = new LinearLayout(this);
        ticketLayout.setOrientation(LinearLayout.VERTICAL);
        ticketLayout.setVisibility(View.GONE);

        tvTicketDetails = new TextView(this);
        tvTicketDetails.setTextSize(16);
        tvTicketDetails.setPadding(0, 20, 0, 20);

        tvQrCode = new TextView(this);
        tvQrCode.setText(" [ QR-CODE ] \n\n[ █ ▄ █ ▄ █ ]\n[ ▄ █ ▄ █ ▄ ]\n[ █ ▄ █ ▄ █ ]\n\n(Tippen zum Drehen)");
        tvQrCode.setTextSize(16);
        tvQrCode.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvQrCode.setGravity(Gravity.CENTER);
        tvQrCode.setBackgroundColor(Color.LTGRAY);
        tvQrCode.setPadding(40, 60, 40, 60);

        tvQrCode.setOnClickListener(this::spinQrCode);

        Button btnBack = new Button(this);
        btnBack.setText("Daten ändern");
        btnBack.setOnClickListener(v -> {
            ticketLayout.setVisibility(View.GONE);
            formLayout.setVisibility(View.VISIBLE);
        });

        ticketLayout.addView(tvTicketDetails);
        ticketLayout.addView(tvQrCode);
        ticketLayout.addView(btnBack);

        mainLayout.addView(ticketLayout);

        setContentView(scrollView);
    }

    private EditText createInputField(String label, String hint) {
        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTypeface(null, Typeface.BOLD);
        tvLabel.setPadding(0, 10, 0, 5);
        formLayout.addView(tvLabel);

        EditText editText = new EditText(this);
        editText.setHint(hint);
        formLayout.addView(editText);
        return editText;
    }

    private void generateTicket() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dobStr = etDob.getText().toString().trim();
        String docNum = etDocNum.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || dobStr.isEmpty() || docNum.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen!", Toast.LENGTH_SHORT).show();
            return;
        }

        LocalDate dob;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            dob = LocalDate.parse(dobStr, formatter);
        } catch (Exception e) {
            Toast.makeText(this, "Format: DD.MM.YYYY (z.B. 12.04.1998)", Toast.LENGTH_LONG).show();
            return;
        }

        Passenger passenger = new Passenger(firstName, lastName, dob, docNum);
        DeutschlandTicket ticket = DeutschlandTicket.issueVrnTicket(passenger, YearMonth.now());

        String detailsText = "====================================\n" +
                "    DIGITALES VRN DEUTSCHLANDTICKET\n" +
                "====================================\n\n" +
                "Ticket-ID:        " + ticket.ticketId() + "\n" +
                "Inhaber:          " + ticket.passenger().getFullName() + "\n" +
                "Geburtsdatum:     " + ticket.passenger().dateOfBirth() + "\n" +
                "Gültigkeitsmonat: " + ticket.validMonth() + "\n" +
                "Preis:            " + ticket.price() + " €\n" +
                "Status:           " + ticket.status().getDisplayName() + "\n" +
                "Aussteller:       " + ticket.issuer() + "\n\n" +
                "--- Fahrscheinprüfung ---\n" +
                "Heute gültig?     " + (ticket.isValidForDate(LocalDate.now()) ? "JA" : "NEIN") + "\n" +
                "S-Bahn erlaubt?   " + (ticket.canRide(TransitType.S_BAHN) ? "JA" : "NEIN") + "\n" +
                "ICE erlaubt?      " + (ticket.canRide(TransitType.EXPRESS_TRAIN) ? "JA" : "NEIN") + "\n";

        tvTicketDetails.setText(detailsText);

        formLayout.setVisibility(View.GONE);
        ticketLayout.setVisibility(View.VISIBLE);
    }

    private void spinQrCode(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotationY", 0f, 360f);
        animator.setDuration(800);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }
}
