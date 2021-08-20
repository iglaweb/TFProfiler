package ru.igla.tfprofiler.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.igla.tfprofiler.utils.safeLet

class RecyclerViewEmptySupport @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var emptyView: View? = null

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            safeLet(adapter, emptyView) { adapter, view ->
                if (adapter.itemCount == 0) {
                    view.visibility = View.VISIBLE
                    this@RecyclerViewEmptySupport.visibility = View.GONE
                } else {
                    view.visibility = View.GONE
                    this@RecyclerViewEmptySupport.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(emptyObserver)
        emptyObserver.onChanged()
    }

    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
    }
}