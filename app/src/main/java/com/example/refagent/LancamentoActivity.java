package com.example.refagent;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.refagent.database.BancoDados;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LancamentoActivity extends AppCompatActivity {

    private EditText edtValor;
    private Spinner spnFonteReceita, spnDescricaoDespesa;
    private LinearLayout layoutReceita, layoutDespesa;
    private RadioGroup rgTipo;
    private BancoDados bancoDados;

    private ArrayAdapter<String> adapterReceitas;

    private boolean atualizandoValor = false;

    private final String[] categoriasDespesas = {
            "Selecione um gasto",
            "Aluguel",
            "Conta de Energia",
            "Conta de Água",
            "Conta de Gás",
            "Streamings",
            "Wifi",
            "Pacote do Celular",
            "Compras",
            "Cartão de crédito",
            "Plano de Saúde",
            "Empréstimo"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_lancamento);

        bancoDados = new BancoDados(this);

        edtValor = findViewById(R.id.edtValor);
        rgTipo = findViewById(R.id.rgTipo);

        spnFonteReceita = findViewById(R.id.spnFonteReceita);
        spnDescricaoDespesa = findViewById(R.id.spnDescricaoDespesa);

        layoutReceita = findViewById(R.id.layoutReceita);
        layoutDespesa = findViewById(R.id.layoutDespesa);

        Button btnSalvar = findViewById(R.id.btnSalvar);
        Button btnNovaFonte = findViewById(R.id.btnNovaFonte);
        Button btnGerenciarFontes = findViewById(R.id.btnGerenciarFontes);

        configurarCampoMonetario();

        btnGerenciarFontes.setOnClickListener(v -> {
            Intent intent = new Intent(
                    LancamentoActivity.this,
                    FontesReceitaActivity.class
            );

            startActivity(intent);
        });

        configurarCategoriasDespesas();
        atualizarFontesReceita();

        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            atualizarTipo();
        });

        btnNovaFonte.setOnClickListener(v -> cadastrarNovaFonte());

        btnSalvar.setOnClickListener(v -> salvarLancamento());

        atualizarTipo();
    }

    // Formatação monetária brasileira
    private void configurarCampoMonetario() {

        edtValor.setInputType(InputType.TYPE_CLASS_TEXT);
        edtValor.setHint("R$ 0,00");

        edtValor.addTextChangedListener(new TextWatcher() {

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

                if (atualizandoValor) {
                    return;
                }

                String texto = editable.toString();

                String formatado = formatarValorDigitado(texto);

                if (!texto.equals(formatado)) {

                    atualizandoValor = true;

                    edtValor.setText(formatado);
                    edtValor.setSelection(formatado.length());

                    atualizandoValor = false;
                }
            }
        });

        // Ao sair do campo, completa os centavos.
        edtValor.setOnFocusChangeListener((v, temFoco) -> {

            String texto = edtValor.getText().toString();

            if (!temFoco && !texto.isEmpty()) {

                String valor = texto;

                if (!valor.contains(",")) {
                    valor += ",00";
                } else {
                    String[] partes = valor.split(",", -1);

                    if (partes.length == 2) {
                        String centavos = partes[1];

                        if (centavos.length() == 0) {
                            valor += "00";
                        } else if (centavos.length() == 1) {
                            valor += "0";
                        }
                    }
                }

                edtValor.setText(valor);
            }

            if (temFoco && texto.endsWith(",00")) {
                edtValor.setText(texto.substring(0, texto.length() - 3));
                edtValor.setSelection(edtValor.length());
            }
        });
    }

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

    private void configurarCategoriasDespesas() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoriasDespesas
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnDescricaoDespesa.setAdapter(adapter);
    }

    private void atualizarFontesReceita() {

        List<String> fontes = new ArrayList<>();
        fontes.add("Selecione uma fonte de renda");
        fontes.addAll(bancoDados.listarFontesReceita());

        adapterReceitas = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                fontes
        );

        adapterReceitas.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spnFonteReceita.setAdapter(adapterReceitas);
    }

    private void atualizarTipo() {

        boolean receita =
                rgTipo.getCheckedRadioButtonId() == R.id.rbReceita;

        layoutReceita.setVisibility(
                receita ? View.VISIBLE : View.GONE
        );

        layoutDespesa.setVisibility(
                receita ? View.GONE : View.VISIBLE
        );
    }

    private void cadastrarNovaFonte() {

        EditText campo = new EditText(this);
        campo.setHint("Ex.: Salário da esposa");

        campo.setSingleLine(true);
        campo.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Nova fonte de renda")
                .setMessage("Informe o nome da nova fonte:")
                .setView(campo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salvar", (dialog, which) -> {

                    String nome = campo.getText().toString().trim();

                    if (nome.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Informe o nome da fonte.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    boolean salvo = bancoDados.adicionarFonteReceita(nome);

                    if (salvo) {

                        atualizarFontesReceita();

                        int posicao = adapterReceitas.getPosition(nome);

                        if (posicao >= 0) {
                            spnFonteReceita.setSelection(posicao);
                        }

                        Toast.makeText(
                                this,
                                "Fonte de renda cadastrada!",
                                Toast.LENGTH_SHORT
                        ).show();

                    } else {

                        Toast.makeText(
                                this,
                                "Essa fonte já existe ou não pôde ser cadastrada.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    private void salvarLancamento() {

        String valorTexto = edtValor.getText().toString().trim();

        if (valorTexto.isEmpty()) {
            Toast.makeText(this, "Informe o valor.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Remove o símbolo e os separadores de milhar.
        valorTexto = valorTexto
                .replace("R$", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");

        String tipo;
        String descricao;

        if (rgTipo.getCheckedRadioButtonId() == R.id.rbReceita) {

            tipo = "RECEITA";

            if (spnFonteReceita.getSelectedItemPosition() == 0) {
                Toast.makeText(
                        this,
                        "Selecione uma fonte de renda.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            descricao = spnFonteReceita.getSelectedItem().toString();

        } else {

            tipo = "DESPESA";

            if (spnDescricaoDespesa.getSelectedItemPosition() == 0) {
                Toast.makeText(
                        this,
                        "Selecione uma categoria de gasto.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            descricao = spnDescricaoDespesa.getSelectedItem().toString();
        }

        double valor;

        try {
            valor = Double.parseDouble(valorTexto);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite um valor válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (valor <= 0) {
            Toast.makeText(this, "O valor precisa ser maior que zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        String data = new SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
        ).format(new Date());

        boolean salvo = bancoDados.adicionarLancamento(
                tipo,
                descricao,
                valor,
                data
        );

        if (salvo) {

            Toast.makeText(
                    this,
                    "Lançamento salvo com sucesso!",
                    Toast.LENGTH_SHORT
            ).show();

            // Limpa somente o valor e mantém a tela aberta.
            edtValor.setText("");
            edtValor.requestFocus();

        } else {

            Toast.makeText(
                    this,
                    "Não foi possível salvar o lançamento.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bancoDados != null && spnFonteReceita != null) {

            String fonteSelecionada = "";

            if (spnFonteReceita.getSelectedItem() != null) {
                fonteSelecionada =
                        spnFonteReceita.getSelectedItem().toString();
            }

            atualizarFontesReceita();

            int posicao = adapterReceitas.getPosition(fonteSelecionada);

            if (posicao >= 0) {
                spnFonteReceita.setSelection(posicao);
            }
        }
    }
}
