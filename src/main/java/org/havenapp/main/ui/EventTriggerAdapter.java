package org.havenapp.main.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.derlio.waveform.SimpleWaveformView;
import com.github.derlio.waveform.soundfile.SoundFile;
import com.squareup.picasso.Picasso;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.havenapp.main.R;
import org.havenapp.main.model.EventTrigger;
import nl.changer.audiowife.AudioWife;

/**
 * Created by n8fr8 on 4/16/17.
 */

public class EventTriggerAdapter extends RecyclerView.Adapter<EventTriggerAdapter.EventTriggerVH> {

    Context context;
    List<EventTrigger> eventTriggers;
    ArrayList<String> eventTriggerImagePaths;

    OnItemClickListener clickListener;

    public EventTriggerAdapter(Context context, List<EventTrigger> eventTriggers) {
        this.context = context;
        this.eventTriggers = eventTriggers;

        this.eventTriggerImagePaths = new ArrayList<String>();
        for (EventTrigger trigger : eventTriggers)
        {
            if (trigger.getType() == EventTrigger.CAMERA)
            {
                eventTriggerImagePaths.add("file:///" + trigger.getPath());
            }
        }
    }


    @Override
    public EventTriggerVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        EventTriggerVH viewHolder = new EventTriggerVH(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EventTriggerVH holder, int position) {

        final EventTrigger eventTrigger = eventTriggers.get(position);

        String title = eventTrigger.getStringType(context);
        String desc = eventTrigger.getTriggerTime().toLocaleString();

        holder.image.setVisibility(View.GONE);
        holder.extra.setVisibility(View.GONE);
        holder.sound.setVisibility(View.GONE);


        if (eventTrigger.getPath() != null)
        {
            if (eventTrigger.getType() == EventTrigger.CAMERA)
            {
                holder.image.setVisibility(View.VISIBLE);
                Picasso.with(context).load(new File(eventTrigger.getPath())).into(holder.image);
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int startPosition = 0;
                        for (int i = 0; i < eventTriggerImagePaths.size(); i++)
                        {
                            if (eventTriggerImagePaths.get(i).contains(eventTrigger.getPath()))
                            {
                                startPosition = i;
                                break;
                            }
                        }


                        ShareOverlayView overlayView = new ShareOverlayView(context);
                        ImageViewer viewer = new ImageViewer.Builder(context, eventTriggerImagePaths)
                                .setStartPosition(startPosition)
                                .setOverlayView(overlayView)
                                .show();
                        overlayView.setImageViewer(viewer);


                    }
                });

                holder.image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        shareMedia(eventTrigger);
                        return false;
                    }
                });
            }
            else if (eventTrigger.getType() == EventTrigger.MICROPHONE)
            {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

                holder.sound.setVisibility(View.VISIBLE);
                final File fileSound = new File(eventTrigger.getPath());
                try {
                    final SoundFile soundFile = SoundFile.create(fileSound.getPath(), new SoundFile.ProgressListener() {
                        int lastProgress = 0;

                        @Override
                        public boolean reportProgress(double fractionComplete) {
                            final int progress = (int) (fractionComplete * 100);
                            if (lastProgress == progress) {
                                return true;
                            }
                            lastProgress = progress;

                            return true;
                        }
                    });
                    holder.sound.setAudioFile(soundFile);
                    holder.sound.invalidate();
                }
                catch (Exception e){}

                holder.extra.setVisibility(View.VISIBLE);
                holder.extra.removeAllViews();

                AudioWife audioWife = new AudioWife();
                audioWife.init(context, Uri.fromFile(fileSound))
                        .useDefaultUi(holder.extra, inflater);


            }
            else if (eventTrigger.getType() == EventTrigger.ACCELEROMETER)
            {
                desc += "\n" + context.getString(R.string.data_speed) + ": " + eventTrigger.getPath();

            }
            else if (eventTrigger.getType() == EventTrigger.LIGHT)
            {
                desc += "\n" + context.getString(R.string.data_light) + ": " + eventTrigger.getPath();

            }
            else if (eventTrigger.getType() == EventTrigger.PRESSURE)
            {
                desc += "\n" + context.getString(R.string.data_pressure) + ": " + eventTrigger.getPath();
            }
            else if (eventTrigger.getType() == EventTrigger.POWER)
            {
                desc += "\n" + context.getString(R.string.data_power) + ": " + eventTrigger.getPath();
            }

        }

        holder.title.setText(title);
        holder.note.setText(desc);


    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);

        AudioWife.getInstance().release();
    }

    private void shareMedia (EventTrigger eventTrigger)
    {

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(eventTrigger.getPath())));
        shareIntent.setType(eventTrigger.getMimeType());

        context.startActivity(shareIntent);
    }

    @Override
    public int getItemCount() {
        return eventTriggers.size();
    }

    class EventTriggerVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, note;
        ImageView image;
        ViewGroup extra;
        SimpleWaveformView sound;
        public EventTriggerVH(View itemView) {
            super(itemView);

           title = (TextView) itemView.findViewById(R.id.event_item_title);
            note = (TextView) itemView.findViewById(R.id.event_item_desc);
            image = (ImageView) itemView.findViewById(R.id.event_item_image);
            extra = (ViewGroup)itemView.findViewById(R.id.event_item_extra);
            sound = (SimpleWaveformView) itemView.findViewById(R.id.event_item_sound);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (clickListener != null)
                clickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }



}
