package com.example.refagent;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.util.Locale;

import com.example.refagent.database.BancoDados;

public class DiagnosticoActivity extends AppCompatActivity {

    private BancoDados bancoDados;

    private TextView txtSaldo;
    private TextView txtComprometimento;
    private TextView txtDescricaoComprometimento;
    private TextView txtCapacidadeEconomia;
    private TextView txtDescricaoEconomia;
    private TextView txtAnaliseFinanceira;
    private TextView txtAlertasFinanceiros;

    private ProgressBar progressoComprometimento;

    private NumberFormat formatoMoeda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostico);

        bancoDados = new BancoDados(this);

        formatoMoeda = NumberFormat.getCurrencyInstance(
                new Locale("pt", "BR")
        );

        inicializarComponentes();
        atualizarDiagnostico();
    }

    private void inicializarComponentes() {

        txtSaldo = findViewById(R.id.txtDiagnosticoSaldo);
        txtComprometimento = findViewById(R.id.txtComprometimento);
        txtDescricaoComprometimento = findViewById(R.id.txtDescricaoComprometimento);
        txtCapacidadeEconomia = findViewById(R.id.txtCapacidadeEconomia);
        txtDescricaoEconomia = findViewById(R.id.txtDescricaoEconomia);
        txtAnaliseFinanceira = findViewById(R.id.txtAnaliseFinanceira);
        txtAlertasFinanceiros = findViewById(R.id.txtAlertasFinanceiros);

        progressoComprometimento = findViewById(
                R.id.progressoComprometimento
        );
    }

    private void atualizarDiagnostico() {

        double receitas = bancoDados.calcularTotal("RECEITA");
        double despesas = bancoDados.calcularTotal("DESPESA");

        double saldo = receitas - despesas;

        // Saldo atual
        txtSaldo.setText(formatoMoeda.format(saldo));

        // Capacidade de economia
        txtCapacidadeEconomia.setText(formatoMoeda.format(saldo));

        if (saldo > 0) {

            txtCapacidadeEconomia.setTextColor(
                    getColor(R.color.success)
            );

            txtDescricaoEconomia.setText(
                    "Você possui um saldo positivo após suas despesas."
            );

        } else if (saldo < 0) {

            txtCapacidadeEconomia.setTextColor(
                    getColor(R.color.danger)
            );

            txtDescricaoEconomia.setText(
                    "Suas despesas ultrapassaram suas receitas."
            );

        } else {

            txtCapacidadeEconomia.setTextColor(
                    getColor(R.color.text_primary)
            );

            txtDescricaoEconomia.setText(
                    "Seu saldo está zerado."
            );
        }

        // Comprometimento da renda
        double percentual = 0;

        if (receitas > 0) {
            percentual = (despesas / receitas) * 100;
        }

        int progresso = (int) Math.min(
                100,
                Math.max(0, percentual)
        );

        progressoComprometimento.setProgress(progresso);

        txtComprometimento.setText(
                String.format(
                        new Locale("pt", "BR"),
                        "%.1f%%",
                        percentual
                )
        );

        // Descrição do comprometimento
        if (receitas == 0 && despesas == 0) {

            txtDescricaoComprometimento.setText(
                    "Cadastre receitas e despesas para iniciar a análise."
            );

        } else if (receitas == 0) {

            txtDescricaoComprometimento.setText(
                    "Não há receitas cadastradas para calcular o percentual."
            );

        } else if (percentual <= 50) {

            txtDescricaoComprometimento.setText(
                    "Bom resultado! Suas despesas representam até metade da sua renda."
            );

        } else if (percentual <= 80) {

            txtDescricaoComprometimento.setText(
                    "Atenção: uma parte considerável da sua renda está comprometida."
            );

        } else if (percentual <= 100) {

            txtDescricaoComprometimento.setText(
                    "Alerta: quase toda a sua renda está sendo utilizada."
            );

        } else {

            txtDescricaoComprometimento.setText(
                    "Crítico: suas despesas são maiores que suas receitas."
            );
        }

        gerarAnalise(receitas, despesas, saldo, percentual);
        gerarAlertas(receitas, despesas, saldo, percentual);
    }

    private void gerarAnalise(
            double receitas,
            double despesas,
            double saldo,
            double percentual
    ) {

        if (receitas == 0 && despesas == 0) {

            txtAnaliseFinanceira.setText(
                    "Ainda não existem movimentações financeiras cadastradas.\n\n" +
                            "Adicione suas receitas e despesas para que o REF Agent " +
                            "possa analisar sua situação financeira."
            );

        } else if (receitas == 0) {

            txtAnaliseFinanceira.setText(
                    "Você possui despesas cadastradas, mas ainda não possui " +
                            "receitas registradas.\n\n" +
                            "Cadastre suas fontes de renda para obter um diagnóstico mais completo."
            );

        } else if (saldo < 0) {

            txtAnaliseFinanceira.setText(
                    "Sua situação financeira exige atenção.\n\n" +
                            "Atualmente, suas despesas ultrapassam suas receitas em " +
                            formatoMoeda.format(Math.abs(saldo)) +
                            ".\n\n" +
                            "Recomendamos revisar seus gastos e identificar despesas " +
                            "que podem ser reduzidas."
            );

        } else if (saldo == 0) {

            txtAnaliseFinanceira.setText(
                    "Sua renda está sendo totalmente utilizada pelas despesas.\n\n" +
                            "Embora não exista um saldo negativo, você não possui " +
                            "uma margem financeira disponível para imprevistos."
            );

        } else if (percentual <= 50) {

            txtAnaliseFinanceira.setText(
                    "Sua situação financeira apresenta um resultado positivo.\n\n" +
                            "Suas despesas estão relativamente controladas e existe " +
                            "uma margem disponível após os gastos.\n\n" +
                            "Considere reservar parte desse valor para seus objetivos."
            );

        } else if (percentual <= 80) {

            txtAnaliseFinanceira.setText(
                    "Sua situação financeira está equilibrada, mas merece atenção.\n\n" +
                            "Uma parcela significativa da renda está comprometida.\n\n" +
                            "Avalie seus gastos para aumentar sua margem de economia."
            );

        } else {

            txtAnaliseFinanceira.setText(
                    "Sua situação financeira exige planejamento.\n\n" +
                            "A maior parte da sua renda está comprometida com despesas.\n\n" +
                            "Procure identificar gastos que possam ser reduzidos para " +
                            "recuperar sua capacidade de economia."
            );
        }
    }

    private void gerarAlertas(
            double receitas,
            double despesas,
            double saldo,
            double percentual
    ) {

        StringBuilder alertas = new StringBuilder();

        if (receitas == 0 && despesas == 0) {

            alertas.append(
                    "• Nenhuma movimentação financeira cadastrada."
            );

        } else {

            if (receitas == 0) {

                alertas.append(
                        "• Você possui despesas, mas nenhuma receita cadastrada.\n\n"
                );

            } else if (saldo < 0) {

                alertas.append(
                        "• Suas despesas ultrapassaram suas receitas em " +
                                formatoMoeda.format(Math.abs(saldo)) +
                                ".\n\n"
                );

            } else if (percentual > 80) {

                alertas.append(
                        "• Mais de 80% da sua renda está comprometida.\n\n"
                );
            }

            if (saldo > 0) {

                alertas.append(
                        "• Você possui " +
                                formatoMoeda.format(saldo) +
                                " de saldo positivo.\n\n"
                );
            }

            if (despesas == 0 && receitas > 0) {

                alertas.append(
                        "• Nenhuma despesa foi registrada até o momento.\n\n"
                );
            }
        }

        if (alertas.length() == 0) {

            alertas.append(
                    "Nenhum alerta financeiro identificado no momento."
            );

        } else {

            alertas.setLength(
                    alertas.toString().trim().length()
            );
        }

        txtAlertasFinanceiros.setText(alertas.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bancoDados != null) {
            atualizarDiagnostico();
        }
    }
}
