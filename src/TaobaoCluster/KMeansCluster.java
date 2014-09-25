package TaobaoCluster;

import com.sun.xml.internal.ws.resources.DispatchMessages;

import java.util.*;

/**
 * Created by Jayvee on 2014/9/18.
 */
public class KMeansCluster {
    int clusterNum;
    int iterNum;
    Map<Integer, ArrayList<IKMeansCalculable>> clusterMap;//用于保存每个簇的点的列表数组
    //    public static void main(String[] a) {
////        int[] aaa = new int[10];
//
//
//    }

    /**
     * K-Means聚类器的构造函数
     *
     * @param clusterNum 聚类簇的个数
     * @param iterNum    迭代次数
     */
    public KMeansCluster(int clusterNum, int iterNum) {
        this.clusterNum = clusterNum;
        this.iterNum = iterNum;
        clusterMap = new HashMap<Integer, ArrayList<IKMeansCalculable>>(clusterNum);
        for (int i = 0; i < clusterNum; i++) {
            clusterMap.put(i, new ArrayList<IKMeansCalculable>());//初始化类簇Map
        }
    }

    public IKMeansCalculable[][] kmeans(IKMeansCalculable[] kmeansDatas) {
//        int iterNum = 50;//迭代次数
        //使用K-Means++算法进行种子点的选择
        Random random = new Random(System.currentTimeMillis());
        int dimensionNum = kmeansDatas[0].getVecValues().length;
        //首先随机选取一个点作为种子点
        Map<Integer, float[]> CenterVecMap = new HashMap<Integer, float[]>();//聚类中心点的map
        IKMeansCalculable initTemp = kmeansDatas[(random.nextInt(kmeansDatas.length))];
//        initTemp.setTypeID(0);
        CenterVecMap.put(0, initTemp.getVecValues());
        System.out.println("初始化种子点");
        for (int i = 1; i < clusterNum; i++) {//寻找最优的clusterNum个种子点
            double distSum = 0;
            double[] minDists = new double[kmeansDatas.length];//对应每一个点，存储它与最近的种子点的距离的数组
            for (int j = 0; j < kmeansDatas.length; j++) {//对于每个点，求出它与最近的种子点的距离D(X)
                IKMeansCalculable vecData = kmeansDatas[j];
//                if (!CenterVecMap.contains(vecData)) {
                minDists[j] = calMinDist(vecData, CenterVecMap);
//                }
                distSum += minDists[j];
            }
            //取一个随机值，用权重的方式来取计算下一个种子点
            double randomStepSum = random.nextDouble() * distSum;
            for (int k = 0; k < kmeansDatas.length; k++) {
                randomStepSum -= minDists[k];
                if (randomStepSum < 0) {
                    IKMeansCalculable initSeed = kmeansDatas[k];
                    initSeed.setTypeID(i);//设置类别标签
                    CenterVecMap.put(i, initSeed.getVecValues());
                    break;
                }
            }
        }
        //进行普通的K-Means聚类
        for (int iter = 0; iter < iterNum; iter++) {
            System.out.println("第" + iter + "次迭代");
            //首先进行clusterMap进行清零
            for (int t = 0; t < clusterMap.size(); t++) {
                clusterMap.get(t).clear();
            }
            for (IKMeansCalculable vec : kmeansDatas) {
                calTypeID(vec, CenterVecMap);//依次计算每个点的类簇归属
            }
            //重新计算各类簇的中心点坐标
            double changeSum = reCalCenters(CenterVecMap);
            //TODO 进行收敛条件的约束设置
            if (changeSum == 0) {
                break;
            }
        }
        //得出结果
        IKMeansCalculable[][] result = new IKMeansCalculable[clusterNum][];
        for (int typeIndex = 0; typeIndex < clusterNum; typeIndex++) {
            ArrayList<IKMeansCalculable> arrayList = clusterMap.get(typeIndex);
            IKMeansCalculable[] array = new IKMeansCalculable[arrayList.size()];
            for (int kIndex = 0; kIndex < arrayList.size(); kIndex++) {
                array[kIndex] = arrayList.get(kIndex);
            }
            result[typeIndex] = array;
        }
        return result;
    }


    /**
     * 计算两个向量间的距离
     *
     * @param d1
     * @param d2
     * @return
     */
    protected double calDist(IKMeansCalculable d1, IKMeansCalculable d2) {
        double dist = 0;
        float[] vec1 = d1.getVecValues();
        float[] vec2 = d2.getVecValues();
        int dimensionNum = vec1.length;
        for (int i = 0; i < dimensionNum; i++) {
            dist += Math.pow((vec1[i] - vec2[i]), 2);
        }
        return Math.sqrt(dist);
    }

    /**
     * 计算两个向量间的距离
     *
     * @param vec1
     * @param vec2
     * @return
     */
    protected double calDist(float[] vec1, float[] vec2) {
        double dist = 0;
        int dimensionNum = vec1.length;
        for (int i = 0; i < dimensionNum; i++) {
            dist += Math.pow((vec1[i] - vec2[i]), 2);
        }
        return Math.sqrt(dist);
    }

    /**
     * 计算两向量间的余弦距离
     *
     * @param vec1
     * @param vec2
     * @return
     */
    protected double calCosDist(float[] vec1, float[] vec2) {
        double fenzi = 0;
        double fenmu1 = 0;
        double fenmu2 = 0;
        int dSum = vec1.length;
        for (int i = 0; i < dSum; i++) {
            fenzi += vec1[i] * vec2[i];
            fenmu1 += vec1[i] * vec1[i];
            fenmu2 += vec2[i] * vec2[i];
        }
        double cosDist = fenzi / (Math.sqrt(fenmu1) * Math.sqrt(fenmu2));
        return cosDist;
    }


    protected double calMinDist(IKMeansCalculable sourceData, Map<Integer, float[]> targetDatas) {
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < targetDatas.size(); i++) {
            float[] targetValues = targetDatas.get(i);
            double temp = calDist(sourceData.getVecValues(), targetValues);
            if (temp < minDist) {
                minDist = temp;
            }
        }
        return minDist;
    }

    protected double calMaxCosDist(IKMeansCalculable sourceData, Map<Integer, float[]> targetDatas) {
        double maxCosDist = 0;
        for (int i = 0; i < targetDatas.size(); i++) {
            float[] targetValues = targetDatas.get(i);
            double temp = calCosDist(sourceData.getVecValues(), targetValues);
            if (temp > maxCosDist) {
                maxCosDist = temp;
            }
        }
        return maxCosDist;
    }

    //计算某个点属于哪个类簇
    protected void calTypeID(IKMeansCalculable source, Map<Integer, float[]> centerMap) {
//        double minDist = Double.MAX_VALUE;
        double maxDist = 0;
        for (int i = 0; i < centerMap.size(); i++) {
            float[] cVec = centerMap.get(i);
//            double temp = calDist(source.getVecValues(), cVec);
//            if (minDist > temp) {
//                minDist = temp;
//                source.setTypeID(i);
//            }
            double temp = calCosDist(source.getVecValues(), cVec);
            if (maxDist < temp) {
                maxDist = temp;
                source.setTypeID(i);
            }
        }
        //TODO 在每次聚类迭代之前记得对clusterMap进行清零
        clusterMap.get(source.getTypeID()).add(source);
    }

    /**
     * 重新计算各类簇的中心点坐标
     *
     * @param centerMap 中心点坐标的map
     */
    protected double reCalCenters(Map<Integer, float[]> centerMap) {
        int dimensionNum = centerMap.get(0).length;//获取维数
//        centerList.clear();
        double changeSum = 0;//统计各中心位移的总和
        for (int i = 0; i < clusterNum; i++) {
            //按簇类遍历
            ArrayList<IKMeansCalculable> calculableArrayList = clusterMap.get(i);
            float[] newCenter = new float[dimensionNum];
            for (IKMeansCalculable vec : calculableArrayList) {
                for (int j = 0; j < dimensionNum; j++) {
                    newCenter[j] += vec.getVecValues()[j];
                }
            }
            float[] centerClusterVec = centerMap.get(i);
            double dist = 0;//单个簇类中心的距离变化量的平方
            for (int k = 0; k < dimensionNum; k++) {
                newCenter[k] = newCenter[k] / calculableArrayList.size();
                dist += Math.pow((centerClusterVec[k] - newCenter[k]), 2);
            }
            centerMap.put(i, newCenter);
            changeSum += dist;
        }
        return changeSum;
    }


}
