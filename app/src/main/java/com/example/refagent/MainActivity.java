package com.example.refagent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "REFAgentPrefs";
    private static final String CHAVE_NOME = "nome_usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main);

        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnSair = findViewById(R.id.btnSair);

        btnIniciar.setOnClickListener(v -> solicitarNome());

        btnSair.setOnClickListener(v -> finishAffinity());
    }

    private void solicitarNome() {

        SharedPreferences preferencias = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        String nomeSalvo = preferencias.getString(CHAVE_NOME, "");

        EditText edtNome = new EditText(this);

        edtNome.setHint("Digite seu nome");
        edtNome.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        edtNome.setSingleLine(true);
        edtNome.setText(nomeSalvo);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bem-vindo ao REF Agent!")
                .setMessage("Como podemos chamar você?")
                .setView(edtNome)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Continuar", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(botao -> {

                        String nome = edtNome.getText()
                                .toString()
                                .trim();

                        if (nome.isEmpty()) {

                            edtNome.setError("Informe seu nome");

                            return;
                        }

                        preferencias.edit()
                                .putString(CHAVE_NOME, nome)
                                .apply();

                        Intent intent = new Intent(
                                MainActivity.this,
                                DashboardActivity.class
                        );

                        startActivity(intent);

                        dialog.dismiss();
                    });
        });

        dialog.show();
    }
}
