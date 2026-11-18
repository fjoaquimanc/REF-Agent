package com.example.refagent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RadarDireitosActivity extends AppCompatActivity {

    private Button btnBeneficiosSociais;
    private Button btnDireitosTrabalhistas;
    private Button btnDireitosPrevidenciarios;
    private Button btnServicosPublicos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar_direitos);

        inicializarComponentes();
        configurarBotoes();
    }

    private void inicializarComponentes() {

        btnBeneficiosSociais = findViewById(
                R.id.btnBeneficiosSociais
        );

        btnDireitosTrabalhistas = findViewById(
                R.id.btnDireitosTrabalhistas
        );

        btnDireitosPrevidenciarios = findViewById(
                R.id.btnDireitosPrevidenciarios
        );

        btnServicosPublicos = findViewById(
                R.id.btnServicosPublicos
        );
    }

    private void configurarBotoes() {

        // Benefícios sociais
        btnBeneficiosSociais.setOnClickListener(v -> {
            abrirPagina(
                    "https://www.gov.br/mds/pt-br/acoes-e-programas/cadastro-unico",
                    "Cadastro Único"
            );
        });

        // Direitos trabalhistas
        btnDireitosTrabalhistas.setOnClickListener(v -> {
            abrirPagina(
                    "https://www.gov.br/trabalho-e-emprego/pt-br/acesso-a-informacao/acoes-e-programas/programas-projetos-acoes-obras-e-atividades/proteja/duvidas-frequentes",
                    "Direitos trabalhistas"
            );
        });

        // Direitos previdenciários
        btnDireitosPrevidenciarios.setOnClickListener(v -> {
            abrirPagina(
                    "https://www.gov.br/pt-br/temas/meu-inss",
                    "Meu INSS"
            );
        });

        // Serviços públicos
        btnServicosPublicos.setOnClickListener(v -> {
            abrirPagina(
                    "https://www.gov.br/pt-br/servicos",
                    "Serviços públicos"
            );
        });
    }

    private void abrirPagina(String endereco, String nomePagina) {

        try {

            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(endereco)
            );

            startActivity(intent);

        } catch (ActivityNotFoundException e) {

            Toast.makeText(
                    this,
                    "Não foi possível abrir " + nomePagina + ".",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
