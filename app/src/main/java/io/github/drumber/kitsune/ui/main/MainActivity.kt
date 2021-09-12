package io.github.drumber.kitsune.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.kitsune.R
import com.example.kitsune.databinding.ActivityMainBinding
import io.github.drumber.kitsune.ui.adapter.AnimeAdapter
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val viewModel: MainActivityViewModel by viewModel()

    private val binding: ActivityMainBinding by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        initAdapter()
    }

    private fun initAdapter() {
        val animeAdapter = AnimeAdapter()

        binding.rvAnime.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = animeAdapter
        }

        lifecycleScope.launchWhenCreated {
            viewModel.anime.collectLatest {
                animeAdapter.submitData(it)
            }
        }
    }

}