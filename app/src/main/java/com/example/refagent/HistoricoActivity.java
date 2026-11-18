package com.example.refagent;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refagent.database.BancoDados;

import java.text.NumberFormat;
import java.util.Locale;

public class HistoricoActivity extends AppCompatActivity {

    private BancoDados bancoDados;

    private LinearLayout containerHistorico;

    private TextView txtHistoricoVazio;
    private TextView txtTotalReceitas;
    private TextView txtTotalDespesas;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_historico);

        bancoDados = new BancoDados(this);

        containerHistorico = findViewById(R.id.containerHistorico);
        txtHistoricoVazio = findViewById(R.id.txtHistoricoVazio);

        txtTotalReceitas = findViewById(R.id.txtTotalReceitas);
        txtTotalDespesas = findViewById(R.id.txtTotalDespesas);

        carregarHistorico();
    }

    private void carregarHistorico() {

        containerHistorico.removeAllViews();

        double totalReceitas = 0;
        double totalDespesas = 0;

        Cursor cursor = bancoDados.listarLancamentos();

        try {

            if (cursor.getCount() == 0) {
                txtHistoricoVazio.setVisibility(View.VISIBLE);
            } else {
                txtHistoricoVazio.setVisibility(View.GONE);
            }

            while (cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String tipo = cursor.getString(1);
                String descricao = cursor.getString(2);
                double valor = cursor.getDouble(3);
                String data = cursor.getString(4);

                if ("RECEITA".equals(tipo)) {
                    totalReceitas += valor;
                } else {
                    totalDespesas += valor;
                }

                adicionarItemHistorico(
                        id, tipo, descricao, valor, data
                );
            }

        } finally {
            cursor.close();
        }

        txtTotalReceitas.setText(
                formatoMoeda.format(totalReceitas)
        );

        txtTotalDespesas.setText(
                formatoMoeda.format(totalDespesas)
        );
    }

    private void adicionarItemHistorico(
            int id,
            String tipo,
            String descricao,
            double valor,
            String data) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 18, 20, 18);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = 14;
        card.setLayoutParams(cardParams);

        TextView txtDescricao = new TextView(this);

        txtDescricao.setText(descricao);
        txtDescricao.setTextColor(getColor(R.color.text_primary));
        txtDescricao.setTextSize(16);
        txtDescricao.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtData = new TextView(this);

        txtData.setText(data + " • " + tipo);
        txtData.setTextColor(getColor(R.color.text_secondary));
        txtData.setTextSize(12);

        TextView txtValor = new TextView(this);

        String sinal = "RECEITA".equals(tipo) ? "+ " : "- ";

        txtValor.setText(sinal + formatoMoeda.format(valor));

        int cor = "RECEITA".equals(tipo)
                ? R.color.success
                : R.color.danger;

        txtValor.setTextColor(getColor(cor));
        txtValor.setTextSize(16);
        txtValor.setTypeface(null, android.graphics.Typeface.BOLD);
        txtValor.setGravity(Gravity.END);

        LinearLayout linhaBotoes = new LinearLayout(this);

        linhaBotoes.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams botoesParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        botoesParams.topMargin = 12;
        linhaBotoes.setLayoutParams(botoesParams);

        Button btnEditar = new Button(this);

        btnEditar.setText("Editar");
        btnEditar.setTransformationMethod(null);
        btnEditar.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.primary)
                )
        );
        btnEditar.setTextColor(getColor(R.color.white));

        Button btnExcluir = new Button(this);

        btnExcluir.setText("Excluir");
        btnExcluir.setTransformationMethod(null);
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

        botaoParams.setMargins(4, 0, 4, 0);

        linhaBotoes.addView(btnEditar, botaoParams);
        linhaBotoes.addView(btnExcluir, botaoParams);

        btnEditar.setOnClickListener(v ->
                abrirEdicao(id, tipo, descricao, valor, data)
        );

        btnExcluir.setOnClickListener(v ->
                confirmarExclusao(id, descricao)
        );

        card.addView(txtDescricao);
        card.addView(txtData);
        card.addView(txtValor);
        card.addView(linhaBotoes);

        containerHistorico.addView(card);
    }

    private void abrirEdicao(
            int id,
            String tipoAtual,
            String descricaoAtual,
            double valorAtual,
            String dataAtual) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 12, 24, 8);

        TextView txtTipo = new TextView(this);
        txtTipo.setText("Tipo de lançamento");
        txtTipo.setTextSize(15);

        RadioGroup grupoTipo = new RadioGroup(this);
        grupoTipo.setOrientation(RadioGroup.HORIZONTAL);

        RadioButton rbReceita = new RadioButton(this);
        rbReceita.setText("Receita");
        rbReceita.setId(View.generateViewId());

        RadioButton rbDespesa = new RadioButton(this);
        rbDespesa.setText("Despesa");
        rbDespesa.setId(View.generateViewId());

        grupoTipo.addView(rbReceita);
        grupoTipo.addView(rbDespesa);

        if ("RECEITA".equals(tipoAtual)) {
            rbReceita.setChecked(true);
        } else {
            rbDespesa.setChecked(true);
        }

        TextView txtDescricao = new TextView(this);
        txtDescricao.setText("Descrição");

        EditText edtDescricao = new EditText(this);
        edtDescricao.setSingleLine(true);
        edtDescricao.setText(descricaoAtual);
        edtDescricao.setHint("Digite a descrição");

        TextView txtValor = new TextView(this);
        txtValor.setText("Valor");

        EditText edtValor = new EditText(this);
        edtValor.setSingleLine(true);
        edtValor.setInputType(InputType.TYPE_CLASS_TEXT);
        edtValor.setHint("R$ 0,00");

        String valorInicial = String.format(
                Locale.US,
                "%.2f",
                valorAtual
        ).replace(".", ",");

        edtValor.setText(formatarValorDigitado(valorInicial));

        configurarCampoMonetario(edtValor);

        TextView txtData = new TextView(this);
        txtData.setText("Data");

        EditText edtData = new EditText(this);
        edtData.setSingleLine(true);
        edtData.setText(dataAtual);
        edtData.setHint("dd/MM/yyyy");

        layout.addView(txtTipo);
        layout.addView(grupoTipo);
        layout.addView(txtDescricao);
        layout.addView(edtDescricao);
        layout.addView(txtValor);
        layout.addView(edtValor);
        layout.addView(txtData);
        layout.addView(edtData);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar lançamento")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String novaDescricao =
                                edtDescricao.getText().toString().trim();

                        String valorTexto = edtValor.getText().toString()
                                .replace("R$", "")
                                .replace(" ", "")
                                .replace(".", "")
                                .replace(",", ".")
                                .trim();

                        String novaData =
                                edtData.getText().toString().trim();

                        if (novaDescricao.isEmpty()
                                || valorTexto.isEmpty()
                                || novaData.isEmpty()) {

                            Toast.makeText(
                                    this,
                                    "Preencha todos os campos.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        double novoValor;

                        try {
                            novoValor = Double.parseDouble(valorTexto);
                        } catch (NumberFormatException e) {

                            Toast.makeText(
                                    this,
                                    "Digite um valor válido.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (novoValor <= 0) {

                            Toast.makeText(
                                    this,
                                    "O valor precisa ser maior que zero.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        if (grupoTipo.getCheckedRadioButtonId() == -1) {
                            Toast.makeText(
                                    this,
                                    "Selecione o tipo de lançamento.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        String novoTipo =
                                rbReceita.isChecked()
                                        ? "RECEITA"
                                        : "DESPESA";

                        boolean atualizado =
                                bancoDados.atualizarLancamento(
                                        id,
                                        novoTipo,
                                        novaDescricao,
                                        novoValor,
                                        novaData
                                );

                        if (atualizado) {

                            Toast.makeText(
                                    this,
                                    "Lançamento atualizado!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            carregarHistorico();
                            dialog.dismiss();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Não foi possível atualizar.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    // Configura a máscara monetária do campo
    private void configurarCampoMonetario(EditText campo) {

        campo.addTextChangedListener(new TextWatcher() {

            private boolean atualizando = false;

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (atualizando) {
                    return;
                }

                String texto = editable.toString();
                String formatado = formatarValorDigitado(texto);

                if (!texto.equals(formatado)) {

                    atualizando = true;

                    campo.setText(formatado);
                    campo.setSelection(formatado.length());

                    atualizando = false;
                }
            }
        });
    }

    // Formata os números no padrão brasileiro
    private String formatarValorDigitado(String texto) {

        String valor = texto.replace("R$", "")
                .replace(" ", "")
                .replace(".", "");

        StringBuilder limpo = new StringBuilder();

        boolean virgulaEncontrada = false;

        for (int i = 0; i < valor.length(); i++) {

            char caractere = valor.charAt(i);

            if (Character.isDigit(caractere)) {
                limpo.append(caractere);
            } else if (caractere == ',' && !virgulaEncontrada) {
                limpo.append(',');
                virgulaEncontrada = true;
            }
        }

        if (limpo.length() == 0) {
            return "";
        }

        String[] partes = limpo.toString().split(",", -1);

        String inteiro = partes[0];
        String centavos = partes.length > 1 ? partes[1] : null;

        if (centavos != null && centavos.length() > 2) {
            centavos = centavos.substring(0, 2);
        }

        StringBuilder inteiroFormatado = new StringBuilder();

        for (int i = 0; i < inteiro.length(); i++) {

            if (i > 0 && (inteiro.length() - i) % 3 == 0) {
                inteiroFormatado.append('.');
            }

            inteiroFormatado.append(inteiro.charAt(i));
        }

        String resultado = "R$ " + inteiroFormatado;

        if (centavos != null) {
            resultado += "," + centavos;
        }

        return resultado;
    }

    private void confirmarExclusao(int id, String descricao) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir lançamento")
                .setMessage(
                        "Deseja realmente excluir o lançamento \""
                                + descricao + "\"?"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Excluir", (dialog, which) -> {

                    boolean excluido =
                            bancoDados.excluirLancamento(id);

                    if (excluido) {

                        Toast.makeText(
                                this,
                                "Lançamento excluído!",
                                Toast.LENGTH_SHORT
                        ).show();

                        carregarHistorico();

                    } else {

                        Toast.makeText(
                                this,
                                "Não foi possível excluir.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bancoDados != null && containerHistorico != null) {
            carregarHistorico();
        }
    }
}
