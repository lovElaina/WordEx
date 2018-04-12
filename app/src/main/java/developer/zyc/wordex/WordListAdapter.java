package developer.zyc.wordex;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Lenovo on 2017/10/5.
 */

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private Context mContext;
    private List<WordBook> mWordBook;
    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textword;
        TextView textphon;

        public ViewHolder(View view) {
            super(view);
            cardView = (CardView) view;
            textword = (TextView) view.findViewById(R.id.textword);
            textphon = (TextView) view.findViewById(R.id.textphon);
        }
    }
        public WordListAdapter(List<WordBook> wordBooks){
            mWordBook = wordBooks;
        }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();}
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_word_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WordBook wordBook = mWordBook.get(position);
        holder.textword.setText(wordBook.getWord());
        holder.textphon.setText(wordBook.getTrans());
    }

    @Override
    public int getItemCount() {
        return mWordBook.size();
    }
}


