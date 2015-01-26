package de.conradowatz.isaacvision;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class RecyclerItemsAdapter extends RecyclerView.Adapter<RecyclerItemsAdapter.ProfileHolder> {

    private Bitmap[] images;
    private int selectedItem;
    private Context context;

    public RecyclerItemsAdapter(Bitmap[] images) {
        this.images = images;
    }

    @Override
    public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_itemlist, parent, false);

        return new ProfileHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerItemsAdapter.ProfileHolder holder, int position) {

        holder.vImage.setImageBitmap(images[position]);

        //give the image the right width and height
        ViewGroup.LayoutParams params = holder.vLayout.getLayoutParams();
        params.width = (int) Math.ceil((float) images[position].getWidth() / images[position].getHeight() * params.height);
        holder.vLayout.setLayoutParams(params);

        if (position==selectedItem) {
            // if item is selected
            holder.vFrame.setVisibility(View.VISIBLE);
        } else {
            // if not
            holder.vFrame.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    public void selectItem(int position) {
        int prevSelected = selectedItem;
        selectedItem = position;
        notifyItemChanged(position);
        notifyItemChanged(prevSelected);
    }

    public class ProfileHolder extends RecyclerView.ViewHolder {

        protected ImageView vImage;
        protected View vFrame;
        protected RelativeLayout vLayout;

        public ProfileHolder(View itemView, int viewType) {
            super(itemView);

            vImage = (ImageView) itemView.findViewById(R.id.itemListItem_image_imageView);
            vFrame = itemView.findViewById(R.id.itemListItem_frame_View);
            vLayout = (RelativeLayout) itemView.findViewById(R.id.itemListItem_layout_RelativeLayout);
        }
    }

}
