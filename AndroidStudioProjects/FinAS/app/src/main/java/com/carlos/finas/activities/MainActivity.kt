package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.carlos.finas.DatabaseHelper
import com.carlos.finas.databinding.ActivityMainBinding
import com.carlos.finas.R
import com.carlos.finas.workers.DailyReportScheduler
import org.mindrot.jbcrypt.BCrypt
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("FinasApp", "Iniciando onCreate em MainActivity")
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("FinasApp", "Layout activity_main carregado com sucesso")

            // Aplicar imagem de fundo
            applyBackgroundImage()

            // Inicializar DatabaseHelper
            dbHelper = DatabaseHelper(this)
            Log.d("FinasApp", "DatabaseHelper inicializado")

            // Verificar integridade do banco em background
            Thread {
                try {
                    if (!dbHelper.checkDatabaseIntegrity()) {
                        runOnUiThread {
                            Toast.makeText(this, "Erro ao inicializar o banco!", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FinasApp", "Erro verificação banco: ${e.message}")
                }
            }.start()

            // Calcular o dígito esperado com base na data atual
            val digitoEsperado = calcularDigitoData()
            Log.d("FinasApp", "Dígito esperado calculado: $digitoEsperado")

            binding.btnLogin.setOnClickListener {
                Log.d("FinasApp", "Botão de login clicado")
                val usuario = binding.edtUsuario.text.toString().trim()
                val senhaDigitada = binding.edtSenha.text.toString()
                Log.d("FinasApp", "Usuário: $usuario, Senha digitada: [oculta]")

                if (usuario.isEmpty() || senhaDigitada.isEmpty()) {
                    Log.w("FinasApp", "Campos de usuário ou senha vazios")
                    Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Verificar se a senha tem pelo menos 2 caracteres
                if (senhaDigitada.length < 2) {
                    Log.w("FinasApp", "Senha com menos de 2 caracteres")
                    Toast.makeText(this, "A senha deve ter pelo menos 2 caracteres!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Extrair o último dígito e a senha real
                val ultimoDigito = senhaDigitada.last().toString()
                val senhaReal = senhaDigitada.dropLast(1)
                Log.d("FinasApp", "Último dígito: $ultimoDigito, Senha real: [oculta]")

                // Verificar se o último dígito corresponde ao dígito esperado
                if (ultimoDigito != digitoEsperado.toString()) {
                    Log.w("FinasApp", "Dígito final inválido. Esperado: $digitoEsperado, Recebido: $ultimoDigito")
                    Toast.makeText(this, "Dígito final da senha inválido!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Verificar usuário e senha real
                Log.d("FinasApp", "Verificando usuário no banco")
                val user = dbHelper.getUsuario(usuario)
                if (user != null) {
                    Log.d("FinasApp", "Usuário encontrado: ${user.usuario}")
                    try {
                        if (BCrypt.checkpw(senhaReal, user.senha)) {
                            Log.d("FinasApp", "Autenticação bem-sucedida para usuário: $usuario")
                            Toast.makeText(this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show()

                            // Agendar envio diário do Painel para o Telegram
                            DailyReportScheduler.schedule(this)

                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.w("FinasApp", "Senha inválida para usuário: $usuario")
                            Toast.makeText(this, "Usuário ou senha inválidos!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.e("FinasApp", "Erro ao verificar hash da senha: ${e.message}")
                        Toast.makeText(this, "Erro: Hash de senha inválido!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("FinasApp", "Erro inesperado ao verificar senha: ${e.message}")
                        Toast.makeText(this, "Erro ao processar login!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("FinasApp", "Usuário não encontrado: $usuario")
                    Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("FinasApp", "Erro crítico em onCreate: ${e.message}", e)
            Toast.makeText(this, "Erro ao iniciar o aplicativo!", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyBackgroundImage() {
        val sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val defaultImageId = R.drawable.f11 // Imagem padrão (mantida como f11, conforme original)
        val imageId = sharedPrefs.getInt("background_image_id", defaultImageId)
        binding.ivBackground.setImageResource(imageId)
    }

    private fun calcularDigitoData(): Int {
        Log.d("FinasApp", "Calculando dígito da data")
        try {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val dataAtual = String.format("%04d%02d%02d", year, month, day)
            Log.d("FinasApp", "Data atual formatada: $dataAtual")

            var soma = dataAtual.sumOf { it.digitToInt() }
            while (soma > 9) {
                soma = soma.toString().sumOf { it.digitToInt() }
            }
            Log.d("FinasApp", "Dígito calculado: $soma")
            return soma
        } catch (e: Exception) {
            Log.e("FinasApp", "Erro ao calcular dígito da data: ${e.message}")
            return 0
        }
    }
}