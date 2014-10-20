package TaobaoCluster;

import Spider.RateSpider;
import Utils.IDFCaculator;
import Utils.TrieTree;
import Utils.WordNode;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm;
import org.ansj.domain.Term;
import org.ansj.recognition.NatureRecognition;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


/**
 * Created by ITTC-Jayvee on 2014/9/16.
 */
public class TaobaoRatesCluster {
    public static final int APCLUSTER = 1;
    public static final int KMEANS = 0;
    protected JSONArray rateList = null;//原始数据的存储
    protected IClusterCalculable[][] clusterResult = null;
    protected String[][] keyWords = null;

    public JSONArray getRateList() {
        return rateList;
    }

    public IClusterCalculable[][] getClusterResult() {
        return clusterResult;
    }

    public String[][] getKeyWords() {
        return keyWords;
    }

    /**
     * 根据淘宝商品URL进行评价聚类分析
     *
     * @param URL        链接
     * @param maxPage    需要爬取的最大页数，如真实最大页数小于此值，则使用真实最大页数
     * @param DNum       特征向量的维数设置
     * @param ClusterNum 聚类簇数
     * @param iterNum    聚类最大迭代次数
     * @param KeywordNum 最后显示的每一类的关键词个数
     */
    public String processRates(String URL, int clusterType, int maxPage, int DNum, int ClusterNum, int iterNum, int KeywordNum, IDFCaculator idfCaculator) throws JSONException {
//        int TNum = 400;//特征向量的维数
        //获取商品评价列表（JSON形式）
        JSONObject root = null;
        RateSpider spider = new RateSpider();
        root = spider.getRateByURL(URL, maxPage);
        //计算各评价文本的TFIDF排行
        TrieTree AllWordTree = new TrieTree();//用于存储该商品评价列表中出现的所有词汇
        JSONArray rateList = root.optJSONArray("rateList");
        if (null != rateList) {
            if (rateList.length() < 10) {
                JSONObject errJson = new JSONObject();
                errJson.put("errMsg", "评价数太少！");
                return errJson.toString();
            }
            this.rateList = rateList;//赋值给类属性
            ArrayList<DocData> docList = new ArrayList<DocData>();
            for (int i = 0; i < rateList.length(); i++) {
                JSONObject rate = null;
                try {
                    rate = (JSONObject) rateList.get(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String content = null;
//                if (null != rate) {
                content = rate.optString("content");
//                }
                //对每个content的内容进行分词统计
                TrieTree wordsTree = new TrieTree();
                List<Term> terms = NlpAnalysis.parse(content);
                new NatureRecognition(terms).recognition();
                for (Term term : terms) {
                    String natureStr = term.getNatureStr();
                    if ("n".equals(natureStr) || "a".equals(natureStr) || "b".equals(natureStr) || "v".equals(natureStr)) {//排除词性为null的bug词
                        String word = term.getName();
                        if (word.length() > 1) {
                            AllWordTree.addWord(word, natureStr);//添加到全局字典树中，用于选取特征值
                            wordsTree.addWord(word, natureStr);//添加到当前文档的字典树中，用于后续的TF-IDF向量值计算
                        }
                    }
                }
                DocData docData = new DocData(content, idfCaculator.CalTFIDF_ByTT(wordsTree));
                docList.add(i, docData);
            }
            //使用已经分好词的字典树进行TF-IDF计算，降序排行后选取为特征词
            TrieTree tfidfTree = idfCaculator.CalTFIDF_ByTT(AllWordTree);
            ArrayList<WordNode> TFIDFlist = tfidfTree.word_list;
            Collections.sort(TFIDFlist, TrieTree.TFIDF_dowmSortor);
            TrieTree metaVec = new TrieTree();//用于存储特征向量的字典树
            if (DNum > TFIDFlist.size()) {
                DNum = TFIDFlist.size();
            }
            for (int j = 0; j < DNum; j++) {
                WordNode wordNode = TFIDFlist.get(j);
//                System.out.println(wordNode.getWord() + "\tTF-IDF=" + wordNode.tfidf + "\t词性：" + wordNode.getNature());
                metaVec.addWord(wordNode.getWord(), 1);
            }
            //为K-means计算做准备
//            ArrayList<vectorData<String>> kmeansDatas = new ArrayList<vectorData<String>>();
            vectorData[] kmeansDatas = new vectorData[docList.size()];
            //获取每个评价所对应的TF-IDF向量值
            for (int p = 0; p < docList.size(); p++) {
                DocData dd = docList.get(p);
                //TF-IDF向量值的填充
                float[] coordValues = new float[DNum];
                ArrayList<WordNode> metaVecList = metaVec.word_list;
                for (int k = 0; k < metaVecList.size(); k++) {
                    WordNode wordNode = metaVecList.get(k);
                    //检查该评价是否包含特征词
                    WordNode temp = dd.wordTree.getWordNode(wordNode.getWord());
                    if (null != temp) {
                        coordValues[k] = (float) temp.tfidf;
                    } else {
                        coordValues[k] = 0;
                    }
                }
                vectorData<String> vd = new vectorData<String>(p, 0, coordValues, dd.text);
                kmeansDatas[p] = vd;
            }
            IClusterCalculable[][] clusterResult = new IClusterCalculable[0][];
            switch (clusterType) {
                case APCLUSTER: {//进行AP聚类
                    APCluster apCluster = new APCluster(kmeansDatas, 30, 0.6f, 100);
                    clusterResult = apCluster.startCluster();
                }
                break;
                case KMEANS: {
                    //进行k-means聚类
                    KMeansCluster kmc = new KMeansCluster(ClusterNum, iterNum);
                    clusterResult = kmc.kmeans(kmeansDatas);
                }
                break;
                default:
                    break;
            }
//            //进行k-means聚类
//            KMeansCluster kmc = new KMeansCluster(ClusterNum, iterNum);
//            IClusterCalculable[][] clusterResult = kmc.kmeans(kmeansDatas);

//            String[][] keyWords = new String[ClusterNum][KeywordNum];

            //进行孤立点的处理，将孤立点合并到一类
            ArrayList<IClusterCalculable> isolationList = new ArrayList<IClusterCalculable>();
            ArrayList<IClusterCalculable[]> resultList = new ArrayList<IClusterCalculable[]>();
            for (IClusterCalculable[] typeResult : clusterResult) {
                if (typeResult.length == 1) {
                    isolationList.add(typeResult[0]);
                } else {
                    resultList.add(typeResult);
                }
            }
            IClusterCalculable[][] reResult = new IClusterCalculable[resultList.size() + 1][];//正常类+孤立点类
            for (int i = 0; i < resultList.size(); i++) {
                reResult[i] = resultList.get(i);
            }
            reResult[reResult.length - 1] = new IClusterCalculable[isolationList.size()];
            for (int j = 0; j < isolationList.size(); j++) {
                reResult[reResult.length - 1][j] = isolationList.get(j);
            }
//            ClusterNum = clusterResult.length;
//            this.clusterResult = clusterResult;//赋值给类属性
            ClusterNum = reResult.length;
            this.clusterResult = reResult;//赋值给类属性
            String[][] keyWords = new String[ClusterNum][KeywordNum];
            //进行结果的JSON打包
            JSONObject resultRoot = new JSONObject();
            JSONArray clusterList = new JSONArray();

            //进行每个类别的关键词提取
            for (int kk = 0; kk < ClusterNum; kk++) {
//                System.out.println("第" + kk + "类：\t" + clusterResult[kk][0].getObj());
//                TrieTree wordTree = new TrieTree();
                IClusterCalculable[] IClusterCalculables = reResult[kk];

                //TODO 此处注释的是关键词非权重排序
//                for (int ii = 0; ii < IClusterCalculables.length; ii++) {
//                    IClusterCalculable kmd = IClusterCalculables[ii];
//                    List<Term> terms = NlpAnalysis.parse((String) kmd.getObj());
//                    new NatureRecognition(terms).recognition();
//                    for (Term term : terms) {
//                        wordTree.addWord(term.getName(), term.getNatureStr());
//                    }
//                }
//                keyWords[kk] = pullKeyWord(idfCaculator, wordTree, KeywordNum);

                //进行关键词权重排序
                keyWords[kk] = pullKeyWordBySentence(idfCaculator, IClusterCalculables, KeywordNum);

                //打印结果
//                System.out.println("第" + kk + "类关键词:");
                JSONArray clusterKeywords = new JSONArray();
                for (String keyword : keyWords[kk]) {
//                    System.out.println(keyword);
                    clusterKeywords.put(keyword);
                }
//                System.out.println("该类共有" + clusterResult[kk].length + "个评价\n例句：" + clusterResult[kk][0].getObj() + "\n========================");
                //json打包
                JSONArray rates = new JSONArray();
                for (IClusterCalculable ClusterRate : reResult[kk]) {
                    JSONObject rate = new JSONObject();
                    rate.put("content", ClusterRate.getObj());
                    rate.put("rateID", ClusterRate.getID());
                    rate.put("typeID", ClusterRate.getTypeID());
                    rates.put(rate);
                }
                JSONObject cluster = new JSONObject();
                cluster.put("keywords", clusterKeywords);
                cluster.put("rates", rates);
                clusterList.put(cluster);
            }
            resultRoot.put("taskTime", new Date(System.currentTimeMillis()).toString());
            resultRoot.put("clusterList", clusterList);
            resultRoot.put("itemName", root.getString("itemName"));
            resultRoot.put("itemID", root.getString("itemID"));
            resultRoot.put("sellerID", root.getString("sellerID"));
            resultRoot.put("URL", URL);
            this.keyWords = keyWords;//赋值给类属性
//            System.out.println("done!");
            return resultRoot.toString();
        } else {
            String errMsg = root.optString("errMsg");
//            System.out.println(errMsg);
            return root.toString();
        }
    }

    /**
     * 根据已经分好词的字典树进行基于TF-IDF的关键词计算
     *
     * @param idfCaculator
     * @param wordTree
     * @param keywordNum
     * @return
     */
    protected String[] pullKeyWord(IDFCaculator idfCaculator, TrieTree wordTree, int keywordNum) {
        TrieTree tfidfTree = idfCaculator.CalTFIDF_ByTT(wordTree);
        ArrayList<WordNode> word_list = tfidfTree.word_list;
        Collections.sort(word_list, TrieTree.TFIDF_dowmSortor);
        String[] keywords = new String[keywordNum];
        int i = 0;
        int j = 0;
        //去除词性为null的无用词
        while (i < keywordNum && j < word_list.size()) {
            WordNode wordNode = word_list.get(j);
            String natureStr = wordNode.getNature();
            if ("n".equals(natureStr) || "a".equals(natureStr) || "b".equals(natureStr) || "vn".equals(natureStr)) {
                if (wordNode.getWord().length() > 1 && wordNode.getWord().length() < 4) {
                    keywords[i] = wordNode.getWord();
                    i++;
                }
            }
            j++;
        }

        return keywords;

    }

    //TODO 关键词抽取权重改进

    /**
     * 根据已经分好词的字典树进行基于TF-IDF的关键词计算
     *
     * @param idfCaculator
     * @param IClusterCalculables
     * @param keywordNum
     * @return
     */
    protected String[] pullKeyWordBySentence(IDFCaculator idfCaculator, IClusterCalculable[] IClusterCalculables, int keywordNum) {
        TrieTree typeTree = new TrieTree();
        for (int ii = 0; ii < IClusterCalculables.length; ii++) {
            //对该类中的每一句评价进行关键词抽取，最后权重排序
            IClusterCalculable kmd = IClusterCalculables[ii];
            List<Term> terms = NlpAnalysis.parse((String) kmd.getObj());
            new NatureRecognition(terms).recognition();
            TrieTree sentenceTree = new TrieTree();
            for (Term term : terms) {
                sentenceTree.addWord(term.getName(), term.getNatureStr());
            }
            //对每一句进行TFIDF排序
            TrieTree tfidfByTT = idfCaculator.CalTFIDF_ByTT(sentenceTree);
            ArrayList<WordNode> word_list = tfidfByTT.word_list;
            Collections.sort(word_list, TrieTree.TFIDF_dowmSortor);
            for (int index = 0; index < word_list.size(); index++) {
                WordNode wn = word_list.get(index);
                wn.tfidf = wn.tfidf * Math.pow(0.5f, index);//进行权重值计算
                WordNode tempWord = typeTree.getWordNode(wn.getWord());
                if (tempWord == null) {
                    //第一次录入该词
                    typeTree.addWord(wn.getWord(), wn.getNature());
                } else {
                    tempWord.tfidf += wn.tfidf;
                }
            }
//            word_list = typeTree.word_list;//转换引用，对加权求和后的该类各句关键词进行tfidf排行
        }
        ArrayList<WordNode> typeWordList = typeTree.word_list;
        Collections.sort(typeWordList, TrieTree.TFIDF_dowmSortor);
        //关键词个数自适应调整
        keywordNum = keywordNum < typeWordList.size() ? keywordNum : typeWordList.size();
        String[] keywords = new String[keywordNum];
        int i = 0;
        int j = 0;
        //去除词性为null的无用词
        while (i < keywordNum && j < typeWordList.size()) {
            WordNode wordNode = typeWordList.get(j);
            String natureStr = wordNode.getNature();
            if ("n".equals(natureStr) || "a".equals(natureStr) || "b".equals(natureStr) || "vn".equals(natureStr)) {
                if (wordNode.getWord().length() > 1 && wordNode.getWord().length() < 4) {
                    keywords[i] = wordNode.getWord();
                    i++;
                }
            }
            j++;
        }

        return keywords;

    }


    //用于K-Means计算用的向量类
    protected class vectorData<T> implements IClusterCalculable {
        int vecID;//该向量的ID
        int typeID;//该向量所属类别的ID（SVM预留）
        float[] coordValues;//该向量的各坐标值
        public T obj;//预留的额外存储对象

        /**
         * @param vecID       该向量的ID
         * @param typeID      该向量所属类别的ID（SVM预留）
         * @param coordValues 该向量的各坐标值
         * @param obj         预留的额外存储对象
         */
        public vectorData(int vecID, int typeID, float[] coordValues, T obj) {
            this.vecID = vecID;
            this.typeID = typeID;
            this.coordValues = coordValues;
            this.obj = obj;
        }

        /**
         * 获取待计算的向量值
         *
         * @return
         */
        @Override
        public float[] getVecValues() {
            return coordValues;
        }

        /**
         * 获取向量ID
         *
         * @return
         */
        @Override
        public int getID() {
            return vecID;
        }

        /**
         * 获取向量类别ID
         *
         * @return
         */
        @Override
        public int getTypeID() {
            return typeID;
        }

        /**
         * 设置向量类别ID
         *
         * @param typeID
         */
        @Override
        public void setTypeID(int typeID) {
            this.typeID = typeID;
        }

        @Override
        public void setVecValues(float[] vecValues) {
            this.coordValues = vecValues;

        }

        @Override
        public void setID(int ID) {
            this.vecID = ID;

        }


        @Override
        public T getObj() {
            return (T) this.obj;
        }
    }

    //文档数据类（一个文档有可能是一段话）
    protected class DocData {
        String text;
        TrieTree wordTree;

        public DocData(String text, TrieTree wordTree) {
            this.text = text;
            this.wordTree = wordTree;
        }
    }

}
