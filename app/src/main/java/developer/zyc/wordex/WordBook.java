package developer.zyc.wordex;

import org.litepal.crud.DataSupport;

/**
 * Created by Lenovo on 2017/10/11.
 */

public class WordBook extends DataSupport{
    private int id;
    private String word;
    private String trans;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }
}
