package TaobaoCluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AP聚类算法实现
 * Created by Jayvee on 2014/10/8.
 */
public class APCluster {
    private int dNum = 0;//矩阵维数
    private IClusterCalculable[] oriDatas;
    private double[][] Smatrix;
    private double[][] Amatrix;
    private double[][] Rmatrix;
    private int[] Tmatrix;
    private double pk;
    private float lam;
    private int iterNum;
    private float coePK;

    public static void main(String[] a) {
//        ArrayList<testData> list = new ArrayList<testData>();
        float[] tt = new float[]{
                (float) 1.2, (float) 2.0
        };

//        testData t = new testData(tt, 1, 0);
    }


    public APCluster(IClusterCalculable[] oriDatas, int iterNum, float lam, float coePK) {
        this.oriDatas = oriDatas;
        this.dNum = oriDatas.length;
        this.Amatrix = new double[dNum][dNum];//all zero
        this.Rmatrix = new double[dNum][dNum];
        this.Tmatrix = new int[dNum];
        this.iterNum = iterNum;
        this.lam = lam;
        this.coePK = coePK;
    }

    public IClusterCalculable[][] startCluster() {
        Tmatrix = clusterIter(iterNum);
        return getResult();
    }

    private double[][] getSimilarMatrix(IClusterCalculable[] oriData) {
        double[][] S = new double[dNum][dNum];//相似矩阵
        ArrayList<Double> list_S = new ArrayList<Double>();
//        double sum = 0;
        for (int i = 0; i < dNum; i++) {
            IClusterCalculable data_i = oriData[i];
            for (int j = 0; j < dNum; j++) {
                if (i != j) {
                    IClusterCalculable data_j = oriData[j];
                    //使用欧氏距离还是余弦距离，此处有待斟酌。注意，欧氏距离时，此处应该为负值
                    S[i][j] = -BasicUtils.calDist(data_i.getVecValues(), data_j.getVecValues());
                    list_S.add(S[i][j]);
//                    sum += S[i][j];
                }
//                else {
                //P(k)值的选取与聚类数目有关，有待校准
//                    S[i][i] =
//                }
            }
        }
        //pk值的确定，取该点所有对偶点的相似度中值
        Collections.sort(list_S);
        pk = list_S.get((int) (list_S.size() / 2));
////        pk = sum / list_S.size();
        for (int k = 0; k < dNum; k++) {
            S[k][k] = pk * coePK;//preference值的设置
        }
//        pk = 1f;
        return S;
    }


    private double[][] calResponsibility(double[][] smatrix, double[][] amatrix) {
        double[][] rmatrix = new double[dNum][dNum];
        for (int i = 0; i < dNum; i++) {
            for (int k = 0; k < dNum; k++) {
                //计算 max{A(i,j)+S(i,j)}
                double maxAS = -Double.MAX_VALUE;
                double temp;
                for (int j = 0; j < dNum; j++) {
                    if (j != k) {
                        temp = amatrix[i][j] + smatrix[i][j];
                        if (temp > maxAS) {
                            maxAS = temp;
                        }
                    }
                }
                //计算R(i,k)
                if (i != k) {
                    rmatrix[i][k] = smatrix[i][k] - maxAS;
                } else {
                    rmatrix[i][k] = pk - maxAS;
                }
                //TODO  阻尼处理的位置是否适应了第一次迭代时，A矩阵全零的情况？
                //TODO 进行收敛阻尼处理
//                rmatrix[i][k] = (1 - lam) * rmatrix[i][k] + lam * Rmatrix[i][k];
            }
        }
        return rmatrix;
    }

    private double[][] calAvailability(double[][] rmatrix) {
        double[][] amatrix = new double[dNum][dNum];
        for (int i = 0; i < dNum; i++) {
            for (int k = 0; k < dNum; k++) {
                //首先计算max(0,R(j,k))对于j的求和，其中j!=i且j!=k
                double maxSum = 0;
                for (int j = 0; j < dNum; j++) {
                    if (j != i && j != k) {
                        maxSum += 0 > rmatrix[j][k] ? 0 : rmatrix[j][k];
                    }
                }
                double temp = rmatrix[k][k] + maxSum;
                if (i != k) {
                    amatrix[i][k] = 0 < temp ? 0 : temp;
                } else {
                    amatrix[i][k] = maxSum;
                }
                //TODO 进行收敛阻尼处理
//                amatrix[i][k] = (1 - lam) * amatrix[i][k] + lam * Amatrix[i][k];
            }
        }
        return amatrix;
    }

    /**
     * 进行阻尼处理，目标收敛
     *
     * @param lam 阻尼系数，0.5到1之间
     */
    private void calDumping(float lam, double[][] rmatrix, double[][] amatrix) {
        for (int i = 0; i < dNum; i++) {
            for (int k = 0; k < dNum; k++) {
                rmatrix[i][k] = (1 - lam) * rmatrix[i][k] + lam * Rmatrix[i][k];
                amatrix[i][k] = (1 - lam) * amatrix[i][k] + lam * Amatrix[i][k];
            }
        }
    }

    /**
     * 进行簇类中心点和归属的计算
     *
     * @param rmatrix
     * @param amatrix
     * @return typeMatrix 包含类别的一维数组
     */
    private int[] identifyCluster(double[][] rmatrix, double[][] amatrix) {
        int[] typeMatrix = new int[dNum];
        for (int i = 0; i < dNum; i++) {
            double max = -Double.MAX_VALUE;
            for (int k = 0; k < dNum; k++) {
                double temp = amatrix[i][k] + rmatrix[i][k];
                if (temp > max) {
                    max = temp;
                    typeMatrix[i] = k;
                }
            }
        }
        return typeMatrix;
    }

    protected int[] clusterIter(int iterNum) {
        Smatrix = getSimilarMatrix(oriDatas);
        int diffCount = 0;//中心点不变化的次数
        for (int i = 0; i < iterNum; i++) {
            int diffNum = 0;//在一次迭代中中心点变化的个数
            System.out.println("正在进行第" + i + "次迭代");
            //注意，先更新R再更新A
            //TODO 更新时是否需要使用最新的数据，待斟酌
            double[][] rmatrix = calResponsibility(Smatrix, Amatrix);
            double[][] amatrix = calAvailability(Rmatrix);
            //收敛处理
            calDumping(lam, rmatrix, amatrix);
            Rmatrix = rmatrix;
            Amatrix = amatrix;
            int[] types = identifyCluster(Rmatrix, Amatrix);
            //检测簇类数是否有变化
            for (int j = 0; j < dNum; j++) {
                if (Tmatrix[j] != types[j]) {
                    diffNum++;
                }
            }
            Tmatrix = types;
            if (diffNum == 0) {
                diffCount++;
                if (diffCount > 5) {
                    break;
                }
            }
            System.out.println("第" + i + "次迭代完毕！");
        }
        return Tmatrix;
    }

    public IClusterCalculable[][] getResult() {
        //根据Tmatrix来输出结果
        HashMap<Integer, ArrayList<IClusterCalculable>> clusterMap = new HashMap<Integer, ArrayList<IClusterCalculable>>();
        for (int i = 0; i < dNum; i++) {
            IClusterCalculable data = oriDatas[i];
            data.setTypeID(Tmatrix[i]);
            if (clusterMap.containsKey(Tmatrix[i])) {
                ArrayList<IClusterCalculable> list = clusterMap.get(Tmatrix[i]);
                list.add(data);

            } else {
                ArrayList<IClusterCalculable> newlist = new ArrayList<IClusterCalculable>();
                newlist.add(data);
                clusterMap.put(Tmatrix[i], newlist);
            }
        }
//        Collections.sort(oriDatas, BasicUtils.typeSorter);
        IClusterCalculable[][] result = new IClusterCalculable[clusterMap.size()][];
        int count = 0;
        for (Map.Entry<Integer, ArrayList<IClusterCalculable>> entry : clusterMap.entrySet()) {
            ArrayList<IClusterCalculable> arrayList = entry.getValue();
            IClusterCalculable[] array = new IClusterCalculable[arrayList.size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = arrayList.get(i);
            }
            result[count] = array;
            count++;
        }
        return result;
    }

}
