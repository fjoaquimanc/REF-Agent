package com.example.refagent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.refagent.database.BancoDados;

import java.text.NumberFormat;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private BancoDados bancoDados;

    private TextView txtSaudacao;
    private TextView txtSaldo;
    private TextView txtReceitas;
    private TextView txtDespesas;

    private Button btnAdicionar;
    private Button btnHistorico;
    private Button btnDiagnostico;
    private Button btnRadarDireitos;
    private Button btnDividas;

    private NumberFormat formatoMoeda;

    private static final String PREFS_NAME = "REFAgentPrefs";
    private static final String CHAVE_NOME = "nome_usuario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_dashboard);

        bancoDados = new BancoDados(this);

        formatoMoeda = NumberFormat.getCurrencyInstance(
                new Locale("pt", "BR")
        );

        // Componentes do Dashboard
        txtSaudacao = findViewById(R.id.txtSaudacao);
        txtSaldo = findViewById(R.id.txtSaldo);
        txtReceitas = findViewById(R.id.txtReceitas);
        txtDespesas = findViewById(R.id.txtDespesas);

        btnAdicionar = findViewById(R.id.btnAdicionar);
        btnHistorico = findViewById(R.id.btnHistorico);
        btnDiagnostico = findViewById(R.id.btnDiagnostico);
        btnRadarDireitos = findViewById(R.id.btnRadarDireitos);
        btnDividas = findViewById(R.id.btnDividas);

        // Saudação personalizada
        atualizarSaudacao();

        // Adicionar lançamento
        btnAdicionar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    LancamentoActivity.class
            );

            startActivity(intent);
        });

        // Histórico
        btnHistorico.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    HistoricoActivity.class
            );

            startActivity(intent);
        });

        // Diagnóstico financeiro
        btnDiagnostico.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    DiagnosticoActivity.class
            );

            startActivity(intent);
        });

        // Radar de direitos
        btnRadarDireitos.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    RadarDireitosActivity.class
            );

            startActivity(intent);
        });

        // Organizar dívidas
        btnDividas.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    DividasActivity.class
            );

            startActivity(intent);
        });

        atualizarDashboard();
    }

    private void atualizarSaudacao() {

        SharedPreferences preferencias = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        String nome = preferencias.getString(CHAVE_NOME, "");

        if (nome != null && !nome.trim().isEmpty()) {

            txtSaudacao.setText("Olá! 👋, " + nome);

        } else {

            txtSaudacao.setText("Olá! 👋");
        }
    }

    private void atualizarDashboard() {

        double receitas = bancoDados.calcularTotal("RECEITA");
        double despesas = bancoDados.calcularTotal("DESPESA");

        double saldo = receitas - despesas;

        txtSaldo.setText(formatoMoeda.format(saldo));
        txtReceitas.setText(formatoMoeda.format(receitas));
        txtDespesas.setText(formatoMoeda.format(despesas));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bancoDados != null) {
            atualizarDashboard();
        }
    }
}
