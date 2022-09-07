package com.cafeyvinowinebar.cafe_y_vino_client.ui.introduction.walkthrough

import android.content.Context
import android.content.res.Resources
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.cafeyvinowinebar.cafe_y_vino_client.R
import com.cafeyvinowinebar.cafe_y_vino_client.databinding.SlidePagerBinding

class OnBoardingAdapter(
    private val context: Context,
    private val slide_headings: Array<out String> = Resources.getSystem()
        .getStringArray(R.array.pager_headings),
    private val slide_descs: Array<out String> = Resources.getSystem()
        .getStringArray(R.array.pager_descriptions)
) : PagerAdapter() {

    override fun getCount() = slide_headings.size
    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.slide_pager, container, false)
        val binding = SlidePagerBinding.bind(view)

        binding.apply {
            txtSliderTitle.text = slide_headings[position]
            txtSliderBody.text = slide_descs[position]
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ConstraintLayout)
    }
}