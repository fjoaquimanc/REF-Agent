package com.example.refagent;

import android.content.Intent;
import android.database.Cursor;
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

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DividasActivity extends AppCompatActivity {

    private TextView txtTotalDividas;
    private TextView txtQuantidadeDividas;
    private TextView txtDividasVazio;
    private LinearLayout containerDividas;

    private BancoDados bancoDados;

    private final NumberFormat formatoMoeda =
            NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        bancoDados = new BancoDados(this);

        txtTotalDividas = findViewById(R.id.txtTotalDividas);
        txtQuantidadeDividas = findViewById(R.id.txtQuantidadeDividas);
        txtDividasVazio = findViewById(R.id.txtDividasVazio);
        containerDividas = findViewById(R.id.containerDividas);

        Button btnAdicionar = findViewById(R.id.btnAdicionarDivida);

        btnAdicionar.setOnClickListener(v -> {
            Intent intent = new Intent(
                    DividasActivity.this,
                    AdicionarDividaActivity.class
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDividas();
    }

    private void carregarDividas() {

        containerDividas.removeAllViews();

        double totalDividas = bancoDados.calcularTotalDividas();
        int quantidade = bancoDados.contarDividas();

        txtTotalDividas.setText(formatoMoeda.format(totalDividas));

        txtQuantidadeDividas.setText(
                quantidade == 1
                        ? "1 dívida em aberto"
                        : quantidade + " dívidas em aberto"
        );

        Cursor cursor = bancoDados.listarDividas();

        if (cursor.getCount() == 0) {

            txtDividasVazio.setVisibility(View.VISIBLE);

        } else {

            txtDividasVazio.setVisibility(View.GONE);

            while (cursor.moveToNext()) {

                int id = cursor.getInt(0);
                String credor = cursor.getString(1);
                String descricao = cursor.getString(2);
                double valorTotal = cursor.getDouble(3);
                double valorPago = cursor.getDouble(4);
                double valorParcela = cursor.getDouble(5);
                String vencimento = cursor.getString(6);

                adicionarCardDivida(
                        id,
                        credor,
                        descricao,
                        valorTotal,
                        valorPago,
                        valorParcela,
                        vencimento
                );
            }
        }

        cursor.close();
    }

    private void adicionarCardDivida(
            int id,
            String credor,
            String descricao,
            double valorTotal,
            double valorPago,
            double valorParcela,
            String vencimento) {

        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(18, 18, 18, 18);
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        parametros.setMargins(0, 0, 0, 16);
        card.setLayoutParams(parametros);

        TextView txtCredor = new TextView(this);
        txtCredor.setText(credor);
        txtCredor.setTextSize(18);
        txtCredor.setTextColor(getColor(R.color.text_primary));
        txtCredor.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(txtCredor);

        TextView txtDescricao = new TextView(this);
        txtDescricao.setText(descricao);
        txtDescricao.setTextSize(14);
        txtDescricao.setTextColor(getColor(R.color.text_secondary));

        card.addView(txtDescricao);

        double valorRestante = Math.max(0, valorTotal - valorPago);

        adicionarInformacao(card, "Valor total: " +
                formatoMoeda.format(valorTotal));

        adicionarInformacao(card, "Valor pago: " +
                formatoMoeda.format(valorPago));

        adicionarInformacao(card, "Valor restante: " +
                formatoMoeda.format(valorRestante));

        adicionarInformacao(card, "Parcela: " +
                formatoMoeda.format(valorParcela));

        adicionarInformacao(card, "Vencimento: " + vencimento);

        boolean quitada = valorRestante <= 0.001;

        TextView txtStatus = new TextView(this);

        txtStatus.setText(
                quitada ? "Dívida quitada" : "Em aberto"
        );

        txtStatus.setTextSize(13);
        txtStatus.setPadding(0, 12, 0, 8);

        txtStatus.setTextColor(
                getColor(quitada
                        ? R.color.success
                        : R.color.danger)
        );

        card.addView(txtStatus);

        // Botão editar dívida
        Button btnEditar = new Button(this);

        btnEditar.setText("Editar dívida");
        btnEditar.setTransformationMethod(null);

        btnEditar.setOnClickListener(v ->
                abrirDialogEdicao(
                        id,
                        credor,
                        descricao,
                        valorTotal,
                        valorPago,
                        valorParcela,
                        vencimento
                )
        );

        card.addView(btnEditar);

        // Botão registrar pagamento
        if (!quitada) {

            Button btnPagamento = new Button(this);

            btnPagamento.setText("Registrar pagamento");
            btnPagamento.setTransformationMethod(null);

            btnPagamento.setOnClickListener(v ->
                    abrirDialogPagamento(id, valorRestante)
            );

            card.addView(btnPagamento);
        }

        // Botão excluir dívida
        Button btnExcluir = new Button(this);

        btnExcluir.setText("Excluir dívida");
        btnExcluir.setTransformationMethod(null);
        btnExcluir.setTextColor(getColor(R.color.danger));

        btnExcluir.setOnClickListener(v ->
                confirmarExclusao(id, credor)
        );

        card.addView(btnExcluir);

        containerDividas.addView(card);
    }

    private void confirmarExclusao(int id, String credor) {

        new AlertDialog.Builder(this)
                .setTitle("Excluir dívida")
                .setMessage(
                        "Tem certeza de que deseja excluir a dívida de "
                                + credor
                                + "?\n\nEssa ação não poderá ser desfeita."
                )
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton("Excluir", (dialog, which) -> {

                    boolean sucesso = bancoDados.excluirDivida(id);

                    if (sucesso) {

                        Toast.makeText(
                                DividasActivity.this,
                                "Dívida excluída com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                        carregarDividas();

                    } else {

                        Toast.makeText(
                                DividasActivity.this,
                                "Não foi possível excluir a dívida.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .show();
    }

    private void abrirDialogEdicao(
            int id,
            String credorAtual,
            String descricaoAtual,
            double valorTotalAtual,
            double valorPago,
            double valorParcelaAtual,
            String vencimentoAtual) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 8, 32, 8);

        EditText edtCredor = criarCampo(
                "Nome do credor",
                InputType.TYPE_CLASS_TEXT
        );

        edtCredor.setText(credorAtual);
        layout.addView(edtCredor);

        EditText edtDescricao = criarCampo(
                "Descrição da dívida",
                InputType.TYPE_CLASS_TEXT
        );

        edtDescricao.setText(descricaoAtual);
        layout.addView(edtDescricao);

        EditText edtValorTotal = criarCampo(
                "Valor total (ex.: 1.500,00)",
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        edtValorTotal.setText(formatoMoeda.format(valorTotalAtual));
        layout.addView(edtValorTotal);

        EditText edtValorParcela = criarCampo(
                "Valor da parcela",
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        edtValorParcela.setText(formatoMoeda.format(valorParcelaAtual));
        layout.addView(edtValorParcela);

        EditText edtVencimento = criarCampo(
                "Vencimento (dd/MM/yyyy)",
                InputType.TYPE_CLASS_DATETIME |
                        InputType.TYPE_DATETIME_VARIATION_NORMAL
        );

        edtVencimento.setText(vencimentoAtual);
        layout.addView(edtVencimento);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Editar dívida")
                .setView(layout)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar alterações", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(botao -> {

                        String credor = edtCredor.getText()
                                .toString().trim();

                        String descricao = edtDescricao.getText()
                                .toString().trim();

                        String vencimento = edtVencimento.getText()
                                .toString().trim();

                        if (credor.isEmpty()) {
                            edtCredor.setError("Informe o credor");
                            return;
                        }

                        if (descricao.isEmpty()) {
                            edtDescricao.setError("Informe a descrição");
                            return;
                        }

                        if (vencimento.isEmpty() ||
                                !validarData(vencimento)) {
                            edtVencimento.setError(
                                    "Informe uma data válida"
                            );
                            return;
                        }

                        double valorTotal;
                        double valorParcela;

                        try {
                            valorTotal = converterValor(
                                    edtValorTotal.getText().toString()
                            );

                            valorParcela = converterValor(
                                    edtValorParcela.getText().toString()
                            );

                        } catch (NumberFormatException e) {
                            edtValorTotal.setError("Valores inválidos");
                            return;
                        }

                        if (valorTotal <= 0) {
                            edtValorTotal.setError(
                                    "O valor deve ser maior que zero"
                            );
                            return;
                        }

                        if (valorTotal + 0.001 < valorPago) {
                            edtValorTotal.setError(
                                    "O valor total não pode ser menor que o já pago"
                            );
                            return;
                        }

                        if (valorParcela <= 0 ||
                                valorParcela > valorTotal) {
                            edtValorParcela.setError(
                                    "Informe um valor de parcela válido"
                            );
                            return;
                        }

                        boolean sucesso = bancoDados.atualizarDivida(
                                id,
                                credor,
                                descricao,
                                valorTotal,
                                valorParcela,
                                vencimento
                        );

                        if (sucesso) {

                            Toast.makeText(
                                    this,
                                    "Dívida atualizada com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                            carregarDividas();

                        } else {

                            Toast.makeText(
                                    this,
                                    "Não foi possível atualizar a dívida.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    private EditText criarCampo(String dica, int tipo) {

        EditText campo = new EditText(this);

        campo.setHint(dica);
        campo.setInputType(tipo);

        return campo;
    }

    private double converterValor(String texto) {

        texto = texto.replace("R$", "")
                .replace(" ", "")
                .trim();

        if (texto.contains(",")) {
            texto = texto.replace(".", "")
                    .replace(",", ".");
        }

        return Double.parseDouble(texto);
    }

    private boolean validarData(String data) {

        SimpleDateFormat formato =
                new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));

        formato.setLenient(false);

        try {
            Date dataConvertida = formato.parse(data);
            return dataConvertida != null;

        } catch (ParseException e) {
            return false;
        }
    }

    private void abrirDialogPagamento(int id, double valorRestante) {

        EditText edtPagamento = new EditText(this);

        edtPagamento.setHint("Ex.: 150,00");
        edtPagamento.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Registrar pagamento")
                .setMessage(
                        "Valor restante: " +
                                formatoMoeda.format(valorRestante) +
                                "\n\nInforme quanto foi pago:"
                )
                .setView(edtPagamento)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(botao -> {

                        String texto = edtPagamento.getText()
                                .toString()
                                .trim()
                                .replace(",", ".");

                        if (texto.isEmpty()) {
                            edtPagamento.setError("Informe o valor pago");
                            return;
                        }

                        double valorPagamento;

                        try {
                            valorPagamento = Double.parseDouble(texto);
                        } catch (NumberFormatException e) {
                            edtPagamento.setError("Valor inválido");
                            return;
                        }

                        if (valorPagamento <= 0) {
                            edtPagamento.setError(
                                    "O pagamento deve ser maior que zero"
                            );
                            return;
                        }

                        if (valorPagamento > valorRestante + 0.001) {
                            edtPagamento.setError(
                                    "O pagamento não pode ultrapassar o valor restante"
                            );
                            return;
                        }

                        boolean sucesso =
                                bancoDados.registrarPagamentoDivida(
                                        id,
                                        valorPagamento
                                );

                        if (sucesso) {

                            Toast.makeText(
                                    DividasActivity.this,
                                    "Pagamento registrado com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            dialog.dismiss();
                            carregarDividas();

                        } else {

                            Toast.makeText(
                                    DividasActivity.this,
                                    "Não foi possível registrar o pagamento.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        dialog.show();
    }

    private void adicionarInformacao(LinearLayout card, String texto) {

        TextView informacao = new TextView(this);

        informacao.setText(texto);
        informacao.setTextSize(14);
        informacao.setTextColor(getColor(R.color.text_secondary));
        informacao.setPadding(0, 8, 0, 0);

        card.addView(informacao);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bancoDados != null) {
            bancoDados.close();
        }
    }
}
