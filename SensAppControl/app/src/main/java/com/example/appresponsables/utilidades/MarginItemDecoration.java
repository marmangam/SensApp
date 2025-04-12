// Clase usada para añadir márgenes entre los elementos de un RecyclerView (usados en  las listas).

package com.example.appresponsables.utilidades;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {

    private final int spaceHeight;

    public MarginItemDecoration(int spaceHeight) {
        this.spaceHeight = spaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        // Aplica el margen inferior a todos los elementos excepto el último
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = spaceHeight;
        }
    }
}