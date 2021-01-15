package com.moviedb.details

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.core.data.remote.ApiService.Companion.IMG_URL
import com.example.core.domain.Genre
import com.example.core.domain.Item
import com.example.core.utils.Constant.ITEM
import com.example.core.utils.Constant.TYPE
import com.google.android.material.snackbar.Snackbar
import com.moviedb.R
import com.moviedb.databinding.ActivityDetailsBinding
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.coroutines.InternalCoroutinesApi
import org.koin.android.viewmodel.ext.android.viewModel

@InternalCoroutinesApi
class DetailsActivity : AppCompatActivity() {
    private val viewModel: DetailsViewModel by viewModel()
    private lateinit var itemDB: Item
    private var booked = false
    private var loaded = false
    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val view = listOf(
            binding.detailPoster,
            binding.detailTitle,
            binding.detailRelease,
            binding.detailGenre,
            binding.detailRating,
            binding.detailTime,
            binding.detailTagline,
            binding.detailOverview
        )
        startAnim(view)

        itemDB = intent.getSerializableExtra(ITEM) as Item

        if (savedInstanceState == null) {
            viewModel.getData(intent.getStringExtra(TYPE), itemDB.id)
        }

        viewModel.getMovie().observe(this, {
            binding.detailEpisode.visibility = View.GONE
            binding.detailSeason.visibility = View.GONE
            Glide.with(this).load(IMG_URL + it.poster)
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.error_placeholder)
                .override(500, 750)
                .into(binding.detailPoster)
            clearAnim(view)
            binding.detailTitle.text = it.title
            binding.detailRelease.text = it.release_date.toRelease()
            binding.detailGenre.text = it.genre.toGenres()
            binding.detailRating.text = it.vote
            binding.detailTime.text = resources.getString(R.string.runtime, it.runtime)
            binding.detailTagline.text = it.tagline
            binding.detailTagline.visibility =
                if (it.tagline.isEmpty()) View.GONE else View.VISIBLE
            binding.detailOverview.text = it.overview
            loaded = true
        })

        viewModel.getTV().observe(this, {
            binding.detailTime.visibility = View.GONE
            binding.detailTagline.visibility = View.GONE
            Glide.with(this).load(IMG_URL + it.poster)
                .placeholder(R.drawable.loading_placeholder)
                .error(R.drawable.error_placeholder)
                .override(500, 750)
                .into(binding.detailPoster)
            clearAnim(view)
            binding.detailTitle.text = it.name
            binding.detailRelease.text = it.air_date.toRelease()
            binding.detailGenre.text = it.genre.toGenres()
            binding.detailRating.text = it.vote
            binding.detailOverview.text = it.overview
            binding.detailSeason.text =
                resources.getQuantityString(R.plurals.season, it.season, it.season)
            binding.detailEpisode.text =
                resources.getQuantityString(R.plurals.episode, it.episode, it.episode)
            loaded = true
        })

        viewModel.getError().observe(this, {
            if (it == true) {
                FancyToast.makeText(
                    this,
                    getString(R.string.net_err),
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,
                    R.drawable.no_internet,
                    false
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500L)
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("BOOL", true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.details_menu, menu)
        if (itemDB.isBookmarked) {
            menu?.getItem(0)?.setIcon(R.drawable.bookmarked)
            booked = true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.book && loaded) {
            if (!booked) {
                viewModel.updateBookmark(itemDB, true)
                Snackbar.make(
                    binding.snackParent,
                    getString(R.string.bookmarked),
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(getColor(R.color.colorSuccess)).show()
                item.setIcon(R.drawable.bookmarked)
                booked = true
            } else {
                viewModel.updateBookmark(itemDB, false)
                Snackbar.make(
                    binding.snackParent,
                    getString(R.string.remove_bookmark),
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(getColor(R.color.colorPrimary)).show()
                item.setIcon(R.drawable.bookmark)
                booked = false
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startAnim(list: List<View>) {
        list.map {
            it.startAnimation(AnimationUtils.loadAnimation(this@DetailsActivity, R.anim.flashing))
        }
    }

    private fun clearAnim(view: List<View>) {
        binding.detailProgress.visibility = View.GONE
        clearItem(view)
    }

    private fun clearItem(view: List<View>) {
        view.map {
            it.clearAnimation()
            it.setBackgroundResource(0)
        }
    }

    private fun List<Genre>.toGenres(): String {
        var genre = ""
        this.forEach { i ->
            genre += if (i == this.last()) {
                i.name
            } else {
                "${i.name}, "
            }
        }
        return genre
    }

    private fun String.toRelease(): String {
        val splitting = this.split("-")
        val year = splitting[0]
        val month = arrayOf(
            "Jan",
            "Feb",
            "Mar",
            "Apr",
            "Mei",
            "Jun",
            "Jul",
            "Aug",
            "Sep",
            "Okt",
            "Nov",
            "Des"
        )[splitting[1].toInt() - 1]
        val date = splitting[2]
        return "Release : $date $month $year"
    }

}

