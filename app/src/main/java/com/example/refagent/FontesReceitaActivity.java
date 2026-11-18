package com.example.refagent;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refagent.database.BancoDados;

import java.util.List;

public class FontesReceitaActivity extends AppCompatActivity {

    private BancoDados bancoDados;
    private LinearLayout containerFontes;
    private TextView txtListaVazia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_fontes_receita);

        bancoDados = new BancoDados(this);

        containerFontes = findViewById(R.id.containerFontes);
        txtListaVazia = findViewById(R.id.txtListaVazia);

        Button btnAdicionar = findViewById(R.id.btnAdicionarFonte);

        btnAdicionar.setOnClickListener(v -> adicionarFonte());

        carregarFontes();
    }

    private void carregarFontes() {

        containerFontes.removeAllViews();

        List<String> fontes = bancoDados.listarFontesReceita();

        txtListaVazia.setVisibility(
                fontes.isEmpty() ? View.VISIBLE : View.GONE
        );

        for (String fonte : fontes) {
            adicionarCardFonte(fonte);
        }
    }

    private void adicionarCardFonte(String nome) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = 16;
        card.setLayoutParams(cardParams);

        TextView txtNome = new TextView(this);
        txtNome.setText(nome);
        txtNome.setTextColor(getColor(R.color.text_primary));
        txtNome.setTextSize(17);
        txtNome.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(txtNome);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams botoesParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        botoesParams.topMargin = 12;
        botoes.setLayoutParams(botoesParams);

        Button btnEditar = new Button(this);
        btnEditar.setText("Editar");
        btnEditar.setAllCaps(false);
        btnEditar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.secondary)
                )
        );
        btnEditar.setTextColor(getColor(R.color.white));

        Button btnExcluir = new Button(this);
        btnExcluir.setText("Excluir");
        btnExcluir.setAllCaps(false);
        btnExcluir.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.danger)
                )
        );
        btnExcluir.setTextColor(getColor(R.color.white));

        LinearLayout.LayoutParams botaoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        botaoParams.setMargins(0, 0, 8, 0);
        btnEditar.setLayoutParams(botaoParams);

        LinearLayout.LayoutParams excluirParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        excluirParams.setMargins(8, 0, 0, 0);
        btnExcluir.setLayoutParams(excluirParams);

        btnEditar.setOnClickListener(v -> editarFonte(nome));
        btnExcluir.setOnClickListener(v -> confirmarExclusao(nome));

        botoes.addView(btnEditar);
        botoes.addView(btnExcluir);

        card.addView(botoes);

        containerFontes.addView(card);
    }

    private void adicionarFonte() {

        EditText campo = criarCampoTexto("Ex.: Salário principal");

        new AlertDialog.Builder(this)
                .setTitle("Nova fonte de renda")
                .setView(campo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String nome = campo.getText().toString().trim();

                    if (nome.isEmpty()) {
                        Toast.makeText(this,
                                "Informe um nome.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (bancoDados.adicionarFonteReceita(nome)) {
                        carregarFontes();

                        Toast.makeText(this,
                                "Fonte cadastrada!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Essa fonte já existe ou não pôde ser cadastrada.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void editarFonte(String nomeAntigo) {

        EditText campo = criarCampoTexto(nomeAntigo);
        campo.setText(nomeAntigo);
        campo.setSelection(campo.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Editar fonte de renda")
                .setView(campo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar alterações", (dialog, which) -> {

                    String novoNome = campo.getText().toString().trim();

                    if (novoNome.isEmpty()) {
                        Toast.makeText(this,
                                "Informe um nome.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (novoNome.equalsIgnoreCase(nomeAntigo)) {
                        return;
                    }

                    if (bancoDados.atualizarFonteReceita(
                            nomeAntigo, novoNome)) {

                        carregarFontes();

                        Toast.makeText(this,
                                "Fonte atualizada!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Não foi possível atualizar. Verifique se o nome já existe.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void confirmarExclusao(String nome) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir fonte de renda")
                .setMessage(
                        "Deseja realmente excluir \"" + nome + "\" da lista?\n\n" +
                                "Os lançamentos financeiros já registrados serão preservados."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {

                    if (bancoDados.excluirFonteReceita(nome)) {
                        carregarFontes();

                        Toast.makeText(this,
                                "Fonte excluída!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Não foi possível excluir a fonte.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private EditText criarCampoTexto(String dica) {

        EditText campo = new EditText(this);
        campo.setSingleLine(true);
        campo.setHint(dica);
        campo.setInputType(InputType.TYPE_CLASS_TEXT);
        campo.setPadding(40, 20, 40, 20);

        return campo;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bancoDados != null && containerFontes != null) {
            carregarFontes();
        }
    }
}
