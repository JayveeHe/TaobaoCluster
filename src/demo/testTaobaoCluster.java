package demo;

import TaobaoCluster.TaobaoRatesCluster;
import Utils.IDFCaculator;
import org.json.JSONException;

import java.io.*;

/**
 * Created by Jayvee on 2014/9/24.
 */
public class testTaobaoCluster {
    public static void main(String[] a)  {
//        getRateByURL("http://detail.tmall.com/item.htm?spm=a220m.1000858.1000725.1.rOFOJB&id=15227453115&areaId=330100&cat_id=50024411&rn=5769ae79caf91fd6529ee62e0e24da35&user_id=720797461&is_b=1", 0);
        TaobaoRatesCluster tr = new TaobaoRatesCluster();
        String resultSTR = null;
        IDFCaculator idfCaculator = new IDFCaculator("IDF.txt");
        try {
            resultSTR = tr.processRates("http://detail.tmall.com/item.htm?spm=a230r.1.14.6.hYzyO6&id=21469588733&ad_id=&am_id=&cm_id=140105335569ed55e27b&pm_id=", 10, 400, 5, 15, 8, idfCaculator);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File("result.txt"));
            fos.write(resultSTR.getBytes("utf-8"));
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
