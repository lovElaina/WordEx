package developer.zyc.wordex;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import static developer.zyc.wordex.R.id.masked;
import static developer.zyc.wordex.R.id.transition_current_scene;
import static developer.zyc.wordex.R.id.visible;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ClearEditText edit_word;
    private TextView tv_word;
    private TextView tv_phonetic;
    private TextView tv_trans;
    public ProgressDialog waitingDialog;
    private ClearEditText ed;
    private long exitTime = 0;
    public SharedPreferences.Editor YYY; //SAVE
    public SharedPreferences XXX;
    public ImageView record;
    public int i;
    public String findword;

    private final static String KEY_CONTENT = "content";

    public static void startForContent(Context context, String content) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(KEY_CONTENT, content);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        XXX=PreferenceManager.getDefaultSharedPreferences(this);
        YYY=XXX.edit();

        int panduan=XXX.getInt("queren",0);

        if(panduan==0) {

            AlertDialog.Builder bu = new AlertDialog.Builder(this);
            bu.setTitle("提示");
            bu.setMessage("此应用目前处于测试阶段，不代表最终品质。\n\n翻译需要网络连接，使用前请确认网络可用\n\n划词翻译：返回至桌面（不要关闭软件）打开任意文本，选择需要翻译的单词，复制即可\n\n由于对有道翻译的格式进行了优化，推荐使用有道翻译\n\n添加最低兼容至Android4.1版本");
            bu.setPositiveButton("下次不再提示", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    YYY.putInt("queren", 1);
                    YYY.apply();
                }
            });
            bu.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            bu.show();
        }
        ed=(ClearEditText) findViewById(masked);
        ed.setHint(" 输入英文单词或中文");
        waitingDialog=new ProgressDialog(MainActivity.this);
        XXX=PreferenceManager.getDefaultSharedPreferences(this);

        YYY=XXX.edit();


        edit_word = (ClearEditText) findViewById(R.id.masked);
        tv_word =   (TextView) findViewById(R.id.danci);
        tv_phonetic = (TextView) findViewById(R.id.yinbiao);
        tv_trans = (TextView) findViewById(R.id.fanyi);
        record = (ImageView) findViewById(R.id.record);

        int opt=XXX.getInt("number",2);

        if(opt==1){
            edit_word.setHint("输入英文单词");
            edit_word.setKeyListener(new DigitsKeyListener(){
                @Override
                public int getInputType() {
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }

                @Override
                protected char[] getAcceptedChars() {
                    char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                    return data;
                }
                public String getStringData(int id) {
                    return getResources().getString(id);
                }
            });

        }else if(opt==2){
            edit_word.setHint("输入英文单词或中文");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);
        }else if(opt==3){
            edit_word.setHint("输入英文单词");
            edit_word.setKeyListener(new DigitsKeyListener(){
                @Override
                public int getInputType() {
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }

                @Override
                protected char[] getAcceptedChars() {
                    char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                    return data;
                }
                public String getStringData(int id) {
                    return getResources().getString(id);
                }
            });
        }else if(opt==4){
            edit_word.setHint("输入英文单词或中文");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);

        }else if(opt==5){
            edit_word.setHint("输入英文短语");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);

        }


        Intent intent = getIntent();
        Utils.printIntent("MainActivity::onCreate()", intent);

        tryToShowContent(intent);
        ListenClipboardService.start(this);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(edit_word.getText().toString())){
                    Toast.makeText(MainActivity.this,"请填写要查询的单词",Toast.LENGTH_SHORT).show();
                    return;
                }
                InputMethodManager zyc = (InputMethodManager)
                        getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
                zyc.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                WordTask task = new WordTask();
                task.execute();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "如遇到问题，请联系qq：723204821", Snackbar.LENGTH_LONG)
                        .setAction("带我去", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String url = "mqqwpa://im/chat?chat_type=wpa&uin=723204821";
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            }
                        }).setActionTextColor(getResources().getColor(R.color.colorAccent)).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Utils.printIntent("MainActivity::onNewIntent()", intent);

        tryToShowContent(intent);
    }

    private void tryToShowContent(Intent intent) {
        String content = intent.getStringExtra(KEY_CONTENT);
        if (!TextUtils.isEmpty(content)) {
            edit_word.setText(content);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "暂未实装", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_update) {
            Toast.makeText(this, "暂未实装", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_baidu) {
            YYY.putInt("number",1);
            YYY.apply();
            edit_word.setHint("输入英文单词");
            edit_word.setKeyListener(new DigitsKeyListener(){
                @Override
                public int getInputType() {
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }

                @Override
                protected char[] getAcceptedChars() {
                    char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                    return data;
                }
                public String getStringData(int id) {
                    return getResources().getString(id);
                }
            });


        } else if (id == R.id.nav_youdao) {
            YYY.putInt("number",2);
            YYY.apply();
            edit_word.setHint("输入英文单词或中文");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);

        } else if (id == R.id.nav_bing) {
            YYY.putInt("number",3);
            YYY.apply();
            edit_word.setHint("输入英文单词");
            edit_word.setKeyListener(new DigitsKeyListener(){
                @Override
                public int getInputType() {
                    return InputType.TYPE_TEXT_VARIATION_PASSWORD;
                }

                @Override
                protected char[] getAcceptedChars() {
                    char[] data = getStringData(R.string.login_only_can_input).toCharArray();
                    return data;
                }
                public String getStringData(int id) {
                    return getResources().getString(id);
                }
            });

        } else if (id == R.id.nav_jinshan) {
            YYY.putInt("number",4);
            YYY.apply();
            edit_word.setHint("输入英文单词或中文");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);

        } else if (id == R.id.nav_phrase){
            YYY.putInt("number",5);
            YYY.apply();
            edit_word.setHint("输入英文短语");
            edit_word.setInputType(InputType.TYPE_CLASS_TEXT);

        } else if (id == R.id.nav_biji) {
            Intent intent = new Intent(this,WordListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("关于");
            builder.setIcon(R.mipmap.pill);
            builder.setMessage("\n做这样一个软件，最初是为了方便自己查英文文档，做成后考虑到它的适用性较广，于是分享给大家，如果你喜欢用手机看英文小说或浏览一些英文网站，它会为你节省至少一半的查单词时间，或者你也可以仅仅把它当作一个集成各大词典的翻译软件，如何使用取决于个人的需求啦\n\n在划词翻译功能的实现上，此应用借鉴了GitHub上的优秀开源项目UCToast\n\n项目地址：https://github.com/liaohuqiu/android-UCToast\n\n在此表示衷心感谢！\n\n开发者简介：东方厨/INTJ/只专注于自己感兴趣的东西\n\n意见反馈：723204821@qq.com");
            builder.setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void findusage(){
        findword = edit_word.getText().toString();
        List<WordBook> wb = DataSupport.select("word").where("word = ?",findword).find(WordBook.class);
        if(wb!=null&&!wb.isEmpty()){
            i=1;
            record.setImageResource(R.drawable.ic_3d_dict2);
        }else{
            i=-1;
            record.setImageResource(R.drawable.ic_3d_dic1);
        }

    }

    public void imageclick(View view){


        if(i==-1){
            record.setImageResource(R.drawable.ic_3d_dict2);
            WordBook wordBook = new WordBook();
            wordBook.setWord(edit_word.getText().toString());
            wordBook.setTrans(tv_trans.getText().toString().replaceAll("\\n",""));
            if (wordBook.save()) {
                Toast.makeText(this, "成功添加至单词本", Toast.LENGTH_SHORT).show();
            }

        }else if(i==1){
            record.setImageResource(R.drawable.ic_3d_dic1);
            DataSupport.deleteAll(WordBook.class,"word = ?",findword);
            Toast.makeText(this, "已从单词本中移除", Toast.LENGTH_SHORT).show();
        }
        i = -i;


    }














    class WordTask extends AsyncTask {
        private String url ;
        private Document doc;
        private String word;
        public String phonetic;
        public String trans;
        public String result;

        public WordTask() {
            int num=XXX.getInt("number",2);
            word = edit_word.getText().toString();
            if(num == 1) {url = "http://dict.cn/";}
            else if (num == 2){url = "http://dict.youdao.com/search?q=";}
            else if (num == 3){url = "http://cn.bing.com/dict/search?q=";}
            else if (num == 4){url = "http://www.iciba.com/";}
            else if (num == 5){url = "http://dict.baidu.com/s?wd=";}
            url = url + word;

        }

        @Override
        public Object doInBackground(Object[] params) {
            int num=XXX.getInt("number",2);
            try {
                doc = Jsoup.connect(url).get();
                if(num == 1){
                    //tab-content
                    phonetic = doc.select(".phonetic").first().text();
                    int kl = phonetic.indexOf("]");
                    int rh = phonetic.lastIndexOf("美");
                    StringBuilder ph = new StringBuilder(phonetic);
                    ph.replace(kl+1,rh,"  ");
                    phonetic = ph.toString();
                    trans = doc.select(".dict-basic-ul").text();
                    result = trans;

                }
                if(num == 2) {
                    phonetic = doc.select(".phonetic")
                            .first().text();
                    trans = doc.select(".trans-container")
                            .first().text();
                    result = trans.replaceAll("n\\.", "\n n.").replaceAll("adj\\.", "\n adj.").replaceAll("adv\\.", "\n adv.").replaceAll("vi\\.", "\n vi.").replaceAll("prep\\.", "\n prep.").replaceAll("vt\\.", "\n vt.").replaceAll("\\[", "\n [").replaceAll("在例句中比较","").replaceAll("论文要发表？","").replaceAll("专家帮你译！","");
                }
                if (num == 3) {
                    phonetic = doc.select(".hd_prUS").first().text();
                    int wl = phonetic.indexOf("美");
                    phonetic = phonetic.substring(wl+2);
                    trans = doc.select(".def").text();
                    result=trans;
                }
                if (num == 4) {
                    phonetic = doc.select(".base-speak").first().text();
                    trans = doc.select(".clearfix").text();
                    int ch = trans.lastIndexOf("/");
                    int fn = trans.indexOf("双语例句");
                    result = trans.substring(ch+3,fn);

                }
                if (num == 5) {
                    phonetic = "no phonetic";
                    trans = doc.select(".tab-content").first().text();
                    result = trans.replaceAll(word,"");
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException s) {

                s.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPreExecute() {


            waitingDialog.setTitle("查询中");
            waitingDialog.setMessage("稍候");
            waitingDialog.setIndeterminate(true);
            waitingDialog.setCancelable(false);
            waitingDialog.setCanceledOnTouchOutside(false);
            waitingDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            tv_word.setText(" " + word);
            record.setVisibility(View.VISIBLE);
            tv_phonetic.setText(" " + phonetic);
            tv_trans.setText(result);
            findusage();
            waitingDialog.dismiss();
            super.onPostExecute(o);
        }
    }

}

