package dip.twotwotwo.com.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songArrayList;
    private Context context;

    public SongAdapter(ArrayList<Song> songArrayList, Context context) {
        this.songArrayList = songArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return songArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return songArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.music_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.txtName = convertView.findViewById(R.id.txtSongName);
            holder.txtArtist = convertView.findViewById(R.id.txtArtist);
            holder.txtAlbum = convertView.findViewById(R.id.txtAlbum);

            convertView.setTag(holder);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        holder.txtName.setText(songArrayList.get(position).title);
        holder.txtArtist.setText(songArrayList.get(position).artist);
        holder.txtAlbum.setText(songArrayList.get(position).album);
        return convertView;
    }

    static class ViewHolder{
        TextView txtName, txtArtist, txtAlbum;
    }
}
