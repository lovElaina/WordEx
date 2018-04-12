package developer.zyc.wordex;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.net.URL;
import java.util.List;

final class TipViewController implements View.OnClickListener, View.OnTouchListener, ViewContainer.KeyEventHandler {

    private WindowManager mWindowManager;
    private Context mContext;
    private ViewContainer mWholeView;
    private View mContentView;
    private ViewDismissHandler mViewDismissHandler;
    private CharSequence mContent;
    private TextView mTextView;
    private TextView mTextViewEnglish;
    private ImageView pop;
    public int i;




    class WordTask extends AsyncTask {

        private String url;
        private Document doc;
        public String trans;
        public String result;
        public WordTask() {
            url = "http://dict.youdao.com/search?q=";
            url = url + mContent;
        }

        @Override
        public Object doInBackground(Object[] params) {
            try {
                doc = Jsoup.connect(url).get();

                trans = doc.select(".trans-container")
                        .first().text();
                result = trans.replaceAll("n\\.", "\n n.").replaceAll("adj\\.", "\n adj.").replaceAll("adv\\.", "\n adv.")
                        .replaceAll("vi\\.", "\n vi.").replaceAll("prep\\.", "\n prep.").replaceAll("vt\\.", "\n vt.").replaceAll("\\[", "\n [").replaceAll("在例句中比较","").replaceAll("论文要发表？","").replaceAll("专家帮你译！","");


            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException s) {

                s.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            mTextView.setText(result);
            pop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(i==-1){
                        pop.setImageResource(R.drawable.ic_3d_dic4);
                        WordBook wordBook = new WordBook();
                        wordBook.setWord(mTextViewEnglish.getText().toString());
                        wordBook.setTrans(result);
                        if (wordBook.save()) {
                            Toast.makeText(mContext, "成功添加至单词本", Toast.LENGTH_SHORT).show();
                        }

                    }else if(i==1){
                        pop.setImageResource(R.drawable.ic_3d_dic3);
                        DataSupport.deleteAll(WordBook.class,"word = ?",mTextViewEnglish.getText().toString());
                        Toast.makeText(mContext, "已从单词本中移除", Toast.LENGTH_SHORT).show();
                    }
                    i = -i;
                }
            });

            super.onPostExecute(o);
        }
    }


    public TipViewController(Context application, CharSequence content) {
        mContext = application;
        mContent = content;
        mWindowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setViewDismissHandler(ViewDismissHandler viewDismissHandler) {
        mViewDismissHandler = viewDismissHandler;
    }

    public void updateContent(CharSequence content) {
        mContent = content;
        mTextView.setText(mContent);

    }

    public void show() {

        ViewContainer view = (ViewContainer) View.inflate(mContext, R.layout.pop_view2, null);

        // display content
        mTextViewEnglish = (TextView) view.findViewById(R.id.pop_view_text_english);
        mTextViewEnglish.setText(mContent);

        mTextView = (TextView)view.findViewById(R.id.pop_view_text);
        pop = (ImageView) view.findViewById(R.id.pop);
        String findword = mTextViewEnglish.getText().toString();
        List<WordBook> wb = DataSupport.select("word").where("word = ?",findword).find(WordBook.class);
        if(wb!=null&&!wb.isEmpty()){
            i=1;
            pop.setImageResource(R.drawable.ic_3d_dic4);
        }else{
            i=-1;
            pop.setImageResource(R.drawable.ic_3d_dic3);
        }




        WordTask task = new WordTask();
        task.execute();




        mWholeView = view;
        mContentView = view.findViewById(R.id.pop_view_content_view);

        // event listeners
        mContentView.setOnClickListener(this);
        mWholeView.setOnTouchListener(this);
        mWholeView.setKeyEventHandler(this);

        int w = WindowManager.LayoutParams.MATCH_PARENT;
        int h = WindowManager.LayoutParams.MATCH_PARENT;

        int flags = 0;
        int type = 0;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //解决Android 7.1.1起不能再用Toast的问题（先解决crash）
            if(Build.VERSION.SDK_INT > 24){
                type = WindowManager.LayoutParams.TYPE_PHONE;
            }else{
                type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        } else {
            type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(w, h, type, flags, PixelFormat.TRANSLUCENT);
        layoutParams.gravity = Gravity.TOP;

        mWindowManager.addView(mWholeView, layoutParams);
    }

    @Override
    public void onClick(View v) {
        removePoppedViewAndClear();
        //MainActivity.startForContent(mContext, mContent.toString());
    }

    private void removePoppedViewAndClear() {

        // remove view
        if (mWindowManager != null && mWholeView != null) {
            mWindowManager.removeView(mWholeView);
        }

        if (mViewDismissHandler != null) {
            mViewDismissHandler.onViewDismiss();
        }

        // remove listeners
        mContentView.setOnClickListener(null);
        mWholeView.setOnTouchListener(null);
        mWholeView.setKeyEventHandler(null);
    }

    /**
     * touch the outside of the content view, remove the popped view
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Rect rect = new Rect();
        mContentView.getGlobalVisibleRect(rect);
        if (!rect.contains(x, y)) {
            removePoppedViewAndClear();
        }
        return false;
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            removePoppedViewAndClear();
        }
    }

    public interface ViewDismissHandler {
        void onViewDismiss();
    }


}
