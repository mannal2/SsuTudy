package com.example.ssutudy;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class UserHolder extends RecyclerView.ViewHolder {
    public final ImageView imageField;
    private final TextView aliasField;
    private final TextView stateField;
    private final TextView rankingField;
    private final TextView studytimeField;

    public UserHolder(@NonNull View itemView) {
        super(itemView);
        imageField = itemView.findViewById(R.id.friend_image);
        aliasField = itemView.findViewById(R.id.friend_alias);
        stateField = itemView.findViewById(R.id.friend_state);
        rankingField = itemView.findViewById(R.id.friend_ranking);
        studytimeField = itemView.findViewById(R.id.friend_studytime);
    }

    public void bind(@NonNull User user){
        String t = user.getTotalStudyTime()+" Min";
        aliasField.setText(user.getAlias());
        stateField.setText(user.getState());
        //rankingField.setText(user.getRanking());
        studytimeField.setText(t);
    }

    public void setRankingField(int rank){
        String te = "#"+rank;
        rankingField.setText(te);
    }

    /*
    public void setImageField(View view, @NonNull User user){
        //Glide.with(InitActivity.this).load(uri2).into(binding.imageView);
        Glide.with(view).load(user.getImage_url()).into(imageField);
    }
    */
}
