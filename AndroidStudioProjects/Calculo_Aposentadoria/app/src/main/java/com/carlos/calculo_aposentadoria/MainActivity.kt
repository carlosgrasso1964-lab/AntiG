package com.carlos.calculo_aposentadoria

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spn_sexo = findViewById<Spinner>(R.id.spn_sexo)
        val txt_idade = findViewById<EditText>(R.id.txt_idade)
        val btn_calcular = findViewById<Button>(R.id.btn_calcular)
        val txt_resultado = findViewById<TextView>(R.id.txt_resultado)

        // 1. Crie a lista de opções para o Spinner
        val generos = arrayOf("masculino", "feminino")

        // 2. Crie um ArrayAdapter
        //    - O primeiro parâmetro é o contexto (this)
        //    - O segundo é um layout padrão do Android para um item de spinner simples
        //    - O terceiro é a lista de dados
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, // Layout para o item selecionado
            generos
        )

        // 3. Especifique o layout a ser usado quando a lista de opções aparecer
        //    (geralmente um pouco diferente para melhor espaçamento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 4. Associe o ArrayAdapter ao Spinner
        spn_sexo.adapter = adapter

        btn_calcular.setOnClickListener {
            val sexo = spn_sexo.selectedItem as String
            val idade = txt_idade.text.toString().toIntOrNull() ?: 0

            var resultado = 0
            if (sexo == "masculino") {
                resultado = 65 - idade
            } else {
                resultado = 60 - idade
            }
            txt_resultado.text = "Faltam $resultado anos para você se aposentar."
        }
    }
}