package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction.onboarding

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.FragmentOnBoardingBinding

/**
 * Displays a ViewPager with descriptions for the functionalities of the app
 */
class OnBoardingFragment : Fragment(R.layout.fragment_on_boarding), ViewPager.OnPageChangeListener {

    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<TextView?>
    private lateinit var btnOmitir: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentOnBoardingBinding.bind(view)
        dotsLayout = binding.dotsLayout
        btnOmitir = binding.btnOmitir

        val onBoardingAdapter = OnBoardingAdapter(requireContext())
        binding.slidePager.apply {
            adapter = onBoardingAdapter
            addOnPageChangeListener(this@OnBoardingFragment)
        }

        addDotsIndicator(0)

        btnOmitir.setOnClickListener {
            val action = OnBoardingFragmentDirections.actionWalkthroughFragmentToBienvenidoFragment()
            findNavController().navigate(action)
        }

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        addDotsIndicator(position)
        if (position == dots.size - 1) {
            btnOmitir.text = "OK"
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    /**
     * Builds a dots layout to correspond with the pages of the ViewPager
     */
    private fun addDotsIndicator(position: Int) {
        dots = arrayOfNulls(4)
        dotsLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(requireContext())
            dots[i]?.apply {
                typeface = ResourcesCompat.getFont(requireContext(), R.font.star_dings)
                setPadding(5)
                text = "z"
                textSize = 20F
                setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_olive))
            }
            dotsLayout.addView(dots[i])
        }
        if (dots.isNotEmpty()) {
            dots[position]?.setTextColor(Color.WHITE)
        }
    }

}