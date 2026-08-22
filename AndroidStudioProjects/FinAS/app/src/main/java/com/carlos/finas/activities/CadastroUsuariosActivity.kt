package com.carlos.finas.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.models.Usuario
import com.carlos.finas.adapters.UsuarioAdapter
import com.carlos.finas.databinding.ActivityCadastroUsuariosBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroUsuariosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCadastroUsuariosBinding
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var usuarioAdapter: UsuarioAdapter
    private var usuarioSelecionado: Usuario? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroUsuariosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)

        usuarioAdapter = UsuarioAdapter(
            emptyList(),
            onClick = { usuario -> carregarUsuarioParaEdicao(usuario) }
        )
        binding.recyclerViewUsuarios.apply {
            layoutManager = LinearLayoutManager(this@CadastroUsuariosActivity)
            adapter = usuarioAdapter
        }

        carregarUsuarios()

        binding.btnSalvar.setOnClickListener {
            salvarUsuario()
        }

        binding.btnDelete.setOnClickListener {
            usuarioSelecionado?.let { confirmarExclusao(it) }
                ?: Toast.makeText(this, "Selecione um usuário para excluir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarUsuarios() {
        lifecycleScope.launch(Dispatchers.Main) {
            val usuarios = withContext(Dispatchers.IO) {
                dbHelper.getAllUsuarios()
            }
            usuarioAdapter.updateData(usuarios)
        }
    }

    private fun carregarUsuarioParaEdicao(usuario: Usuario) {
        usuarioSelecionado = usuario
        binding.edtUsuario.setText(usuario.usuario)
        binding.edtSenha.setText("")
        binding.btnSalvar.text = "Atualizar"
        binding.btnDelete.visibility = View.VISIBLE
    }

    private fun salvarUsuario() {
        val usuario = binding.edtUsuario.text.toString().trim()
        val senha = binding.edtSenha.text.toString()

        if (usuario.isEmpty() || (senha.isEmpty() && usuarioSelecionado == null)) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.Main) {
            if (usuarioSelecionado == null) {
                val result = withContext(Dispatchers.IO) {
                    dbHelper.insertUsuario(usuario, senha)
                }
                if (result != -1L) {
                    Toast.makeText(this@CadastroUsuariosActivity, "Usuário cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
                    limparCampos()
                    carregarUsuarios()
                } else {
                    Toast.makeText(this@CadastroUsuariosActivity, "Erro ao cadastrar usuário!", Toast.LENGTH_SHORT).show()
                }
            } else {
                usuarioSelecionado?.id?.let { id ->
                    val senhaAtual = if (senha.isNotEmpty()) senha else usuarioSelecionado?.senha ?: ""
                    val result = withContext(Dispatchers.IO) {
                        dbHelper.updateUsuario(id, usuario, senhaAtual)
                    }
                    if (result > 0) {
                        Toast.makeText(this@CadastroUsuariosActivity, "Usuário atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        limparCampos()
                        carregarUsuarios()
                    } else {
                        Toast.makeText(this@CadastroUsuariosActivity, "Erro ao atualizar usuário!", Toast.LENGTH_SHORT).show()
                    }
                } ?: Toast.makeText(this@CadastroUsuariosActivity, "ID do usuário inválido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmarExclusao(usuario: Usuario) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Deseja realmente excluir o usuário ${usuario.usuario}?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch(Dispatchers.Main) {
                    usuario.id?.let { id ->
                        val result = withContext(Dispatchers.IO) {
                            dbHelper.deleteUsuario(id)
                        }
                        if (result > 0) {
                            Toast.makeText(this@CadastroUsuariosActivity, "Usuário excluído com sucesso!", Toast.LENGTH_SHORT).show()
                            limparCampos()
                            carregarUsuarios()
                        } else {
                            Toast.makeText(this@CadastroUsuariosActivity, "Erro ao excluir usuário!", Toast.LENGTH_SHORT).show()
                        }
                    } ?: Toast.makeText(this@CadastroUsuariosActivity, "ID do usuário inválido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun limparCampos() {
        usuarioSelecionado = null
        binding.edtUsuario.text.clear()
        binding.edtSenha.text.clear()
        binding.btnSalvar.text = "Salvar"
        binding.btnDelete.visibility = View.GONE
    }
}