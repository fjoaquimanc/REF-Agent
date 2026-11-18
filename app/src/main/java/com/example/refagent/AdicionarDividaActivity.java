package com.example.refagent;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.refagent.database.BancoDados;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdicionarDividaActivity extends AppCompatActivity {

    private EditText edtCredor;
    private EditText edtDescricaoDivida;
    private EditText edtValorTotalDivida;
    private EditText edtValorParcela;
    private EditText edtVencimentoDivida;

    private BancoDados bancoDados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_divida);

        bancoDados = new BancoDados(this);

        edtCredor = findViewById(R.id.edtCredor);
        edtDescricaoDivida = findViewById(R.id.edtDescricaoDivida);
        edtValorTotalDivida = findViewById(R.id.edtValorTotalDivida);
        edtValorParcela = findViewById(R.id.edtValorParcela);
        edtVencimentoDivida = findViewById(R.id.edtVencimentoDivida);

        // Máscaras automáticas dos valores financeiros
        aplicarMascaraMonetaria(edtValorTotalDivida);
        aplicarMascaraMonetaria(edtValorParcela);

        // Máscara automática de data
        aplicarMascaraData(edtVencimentoDivida);

        Button btnSalvar = findViewById(R.id.btnSalvarDivida);
        Button btnCancelar = findViewById(R.id.btnCancelarDivida);

        btnSalvar.setOnClickListener(v -> salvarDivida());
        btnCancelar.setOnClickListener(v -> finish());
    }

    // Máscara monetária no padrão brasileiro
    private void aplicarMascaraMonetaria(EditText campo) {

        campo.addTextChangedListener(new TextWatcher() {

            private boolean editando;

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editando) return;

                editando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.isEmpty()) {
                    campo.setText("");
                    editando = false;
                    return;
                }

                try {

                    double valor = Long.parseLong(numeros) / 100.0;

                    NumberFormat formato =
                            NumberFormat.getCurrencyInstance(
                                    new Locale("pt", "BR")
                            );

                    String valorFormatado = formato.format(valor);

                    campo.setText(valorFormatado);
                    campo.setSelection(campo.getText().length());

                } catch (NumberFormatException e) {

                    campo.setText("");
                }

                editando = false;
            }
        });
    }

    // Máscara automática DD/MM/AAAA
    private void aplicarMascaraData(EditText campo) {

        campo.addTextChangedListener(new TextWatcher() {

            private boolean editando;

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editando) return;

                editando = true;

                String numeros = s.toString().replaceAll("[^0-9]", "");

                if (numeros.length() > 8) {
                    numeros = numeros.substring(0, 8);
                }

                StringBuilder data = new StringBuilder();

                for (int i = 0; i < numeros.length(); i++) {

                    if (i == 2 || i == 4) {
                        data.append("/");
                    }

                    data.append(numeros.charAt(i));
                }

                campo.setText(data.toString());
                campo.setSelection(data.length());

                editando = false;
            }
        });
    }

    private void salvarDivida() {

        String credor = edtCredor.getText().toString().trim();
        String descricao = edtDescricaoDivida.getText().toString().trim();
        String valorTotalTexto = edtValorTotalDivida.getText().toString().trim();
        String valorParcelaTexto = edtValorParcela.getText().toString().trim();
        String vencimento = edtVencimentoDivida.getText().toString().trim();

        if (credor.isEmpty() || descricao.isEmpty() ||
                valorTotalTexto.isEmpty() || valorParcelaTexto.isEmpty() ||
                vencimento.isEmpty()) {

            Toast.makeText(this,
                    "Preencha todos os campos.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        double valorTotal;
        double valorParcela;

        try {

            valorTotal = converterValor(valorTotalTexto);
            valorParcela = converterValor(valorParcelaTexto);

        } catch (NumberFormatException e) {

            Toast.makeText(this,
                    "Informe valores financeiros válidos.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (valorTotal <= 0 || valorParcela <= 0) {

            Toast.makeText(this,
                    "Os valores precisam ser maiores que zero.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (valorParcela > valorTotal) {

            Toast.makeText(this,
                    "A parcela não pode ser maior que a dívida total.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        if (!validarData(vencimento)) {

            Toast.makeText(this,
                    "Informe uma data válida no formato DD/MM/AAAA.",
                    Toast.LENGTH_SHORT).show();

            return;
        }

        boolean sucesso = bancoDados.adicionarDivida(
                credor,
                descricao,
                valorTotal,
                valorParcela,
                vencimento
        );

        if (sucesso) {

            Toast.makeText(this,
                    "Dívida cadastrada com sucesso!",
                    Toast.LENGTH_SHORT).show();

            setResult(RESULT_OK);
            finish();

        } else {

            Toast.makeText(this,
                    "Não foi possível cadastrar a dívida.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Converte o valor monetário para número
    private double converterValor(String valor) {

        String numeros = valor.replaceAll("[^0-9]", "");

        if (numeros.isEmpty()) {
            throw new NumberFormatException("Valor inválido");
        }

        return Long.parseLong(numeros) / 100.0;
    }

    private boolean validarData(String data) {

        SimpleDateFormat formato =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        formato.setLenient(false);

        try {

            Date dataConvertida = formato.parse(data);

            return dataConvertida != null;

        } catch (ParseException e) {

            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bancoDados != null) {
            bancoDados.close();
        }
    }
}
