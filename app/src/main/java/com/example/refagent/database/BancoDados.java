package com.example.refagent.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BancoDados extends SQLiteOpenHelper {

    private static final String NOME_BANCO = "ref_agent.db";
    private static final int VERSAO_BANCO = 3;

    private static final String TABELA = "lancamentos";
    private static final String TABELA_FONTES = "fontes_receita";
    private static final String TABELA_DIVIDAS = "dividas";

    public BancoDados(Context context) {
        super(context, NOME_BANCO, null, VERSAO_BANCO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String sqlLancamentos = "CREATE TABLE " + TABELA + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tipo TEXT NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "valor REAL NOT NULL, " +
                "data TEXT NOT NULL)";

        db.execSQL(sqlLancamentos);

        criarTabelaFontes(db);
        criarTabelaDividas(db);
    }

    private void criarTabelaFontes(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_FONTES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL COLLATE NOCASE UNIQUE)";

        db.execSQL(sql);
    }

    private void criarTabelaDividas(SQLiteDatabase db) {

        String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_DIVIDAS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "credor TEXT NOT NULL, " +
                "descricao TEXT NOT NULL, " +
                "valor_total REAL NOT NULL, " +
                "valor_pago REAL NOT NULL DEFAULT 0, " +
                "valor_parcela REAL NOT NULL, " +
                "vencimento TEXT NOT NULL)";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            criarTabelaFontes(db);
        }

        if (oldVersion < 3) {
            criarTabelaDividas(db);
        }
    }

    // =====================================
    // LANÇAMENTOS FINANCEIROS
    // =====================================

    public boolean adicionarLancamento(
            String tipo,
            String descricao,
            double valor,
            String data) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("tipo", tipo);
        valores.put("descricao", descricao);
        valores.put("valor", valor);
        valores.put("data", data);

        long resultado = db.insert(TABELA, null, valores);

        return resultado != -1;
    }

    public double calcularTotal(String tipo) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(valor) FROM " + TABELA + " WHERE tipo = ?",
                new String[]{tipo}
        );

        double total = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }

    public Cursor listarLancamentos() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, tipo, descricao, valor, data " +
                        "FROM " + TABELA +
                        " ORDER BY id DESC",
                null
        );
    }

    public boolean atualizarLancamento(
            int id,
            String tipo,
            String descricao,
            double valor,
            String data) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("tipo", tipo);
        valores.put("descricao", descricao);
        valores.put("valor", valor);
        valores.put("data", data);

        int resultado = db.update(
                TABELA,
                valores,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    public boolean excluirLancamento(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int resultado = db.delete(
                TABELA,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    // =====================================
    // FONTES DE RECEITA
    // =====================================

    public boolean adicionarFonteReceita(String nome) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("nome", nome.trim());

        long resultado = db.insert(TABELA_FONTES, null, valores);

        return resultado != -1;
    }

    public List<String> listarFontesReceita() {

        List<String> fontes = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT nome FROM " + TABELA_FONTES +
                        " ORDER BY nome COLLATE NOCASE ASC",
                null
        );

        while (cursor.moveToNext()) {
            fontes.add(cursor.getString(0));
        }

        cursor.close();

        return fontes;
    }

    public boolean atualizarFonteReceita(String nomeAntigo, String novoNome) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("nome", novoNome.trim());

        try {

            int resultado = db.update(
                    TABELA_FONTES,
                    valores,
                    "nome = ?",
                    new String[]{nomeAntigo}
            );

            return resultado > 0;

        } catch (SQLiteConstraintException e) {
            return false;
        }
    }

    public boolean excluirFonteReceita(String nome) {

        SQLiteDatabase db = this.getWritableDatabase();

        int resultado = db.delete(
                TABELA_FONTES,
                "nome = ?",
                new String[]{nome}
        );

        return resultado > 0;
    }

    // =====================================
    // ORGANIZAÇÃO DE DÍVIDAS
    // =====================================

    public boolean adicionarDivida(
            String credor,
            String descricao,
            double valorTotal,
            double valorParcela,
            String vencimento) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("credor", credor);
        valores.put("descricao", descricao);
        valores.put("valor_total", valorTotal);
        valores.put("valor_pago", 0);
        valores.put("valor_parcela", valorParcela);
        valores.put("vencimento", vencimento);

        long resultado = db.insert(TABELA_DIVIDAS, null, valores);

        return resultado != -1;
    }

    public Cursor listarDividas() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, credor, descricao, valor_total, " +
                        "valor_pago, valor_parcela, vencimento " +
                        "FROM " + TABELA_DIVIDAS +
                        " ORDER BY vencimento ASC",
                null
        );
    }

    public boolean atualizarDivida(
            int id,
            String credor,
            String descricao,
            double valorTotal,
            double valorParcela,
            String vencimento) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("credor", credor);
        valores.put("descricao", descricao);
        valores.put("valor_total", valorTotal);
        valores.put("valor_parcela", valorParcela);
        valores.put("vencimento", vencimento);

        int resultado = db.update(
                TABELA_DIVIDAS,
                valores,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    public boolean excluirDivida(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int resultado = db.delete(
                TABELA_DIVIDAS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return resultado > 0;
    }

    public boolean registrarPagamentoDivida(int id, double valorPagamento) {

        if (valorPagamento <= 0) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT valor_pago, valor_total FROM " + TABELA_DIVIDAS +
                        " WHERE id = ?",
                new String[]{String.valueOf(id)}
        );

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }

        double valorPagoAtual = cursor.getDouble(0);
        double valorTotal = cursor.getDouble(1);

        cursor.close();

        if (valorPagoAtual + valorPagamento > valorTotal) {
            return false;
        }

        ContentValues valores = new ContentValues();
        valores.put("valor_pago", valorPagoAtual + valorPagamento);

        return db.update(
                TABELA_DIVIDAS,
                valores,
                "id = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    public double calcularTotalDividas() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(valor_total - valor_pago) FROM " + TABELA_DIVIDAS,
                null
        );

        double total = 0;

        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }

    public int contarDividas() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABELA_DIVIDAS +
                        " WHERE valor_total > valor_pago",
                null
        );

        int quantidade = 0;

        if (cursor.moveToFirst()) {
            quantidade = cursor.getInt(0);
        }

        cursor.close();

        return quantidade;
    }
}
