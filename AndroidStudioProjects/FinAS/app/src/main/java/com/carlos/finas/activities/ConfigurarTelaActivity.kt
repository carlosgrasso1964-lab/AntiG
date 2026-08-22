package com.carlos.finas.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.carlos.finas.databinding.ActivityConfigurarTelaBinding
import com.carlos.finas.R

class ConfigurarTelaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurarTelaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarTelaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Lista de IDs de recursos das imagens (f01 a f24)
        val imageIds = (1..24).map { index ->
            resources.getIdentifier("f${index.toString().padStart(2, '0')}", "drawable", packageName)
        }.filter { it != 0 } // Filtra IDs inválidos, caso alguma imagem não exista

        binding.rvBackgroundImages.layoutManager = GridLayoutManager(this, 3) // 3 colunas
        binding.rvBackgroundImages.adapter = BackgroundImageAdapter(imageIds) { imageId ->
            // Salvar a escolha do usuário em SharedPreferences
            getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                .edit()
                .putInt("background_image_id", imageId)
                .apply()
            // Retornar para a MainActivity com a nova escolha
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}

class BackgroundImageAdapter(
    private val imageIds: List<Int>,
    private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<BackgroundImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ImageViewHolder(ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(8, 8, 8, 8)
        })
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(imageIds[position])
        holder.imageView.setOnClickListener { onImageClick(imageIds[position]) }
    }

    override fun getItemCount(): Int = imageIds.size

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}