package TaobaoCluster;


import java.util.Comparator;

/**
 * Created by Jayvee on 2014/10/8.
 */
public class BasicUtils {
    /**
     * 计算两向量间的余弦距离
     *
     * @param vec1
     * @param vec2
     * @return
     */
    protected static double calCosDist(float[] vec1, float[] vec2) {
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

    /**
     * 计算两个向量间的欧式距离
     *
     * @param vec1
     * @param vec2
     * @return
     */
    protected static double calDist(float[] vec1, float[] vec2) {
        double dist = 0;
        int dimensionNum = vec1.length;
        for (int i = 0; i < dimensionNum; i++) {
            dist += Math.pow((vec1[i] - vec2[i]), 2);
        }
        return Math.sqrt(dist);
    }

    public static Comparator<IClusterCalculable> typeSorter = new Comparator<IClusterCalculable>() {
        @Override
        public int compare(IClusterCalculable o1, IClusterCalculable o2) {
            return o1.getTypeID() - o2.getTypeID();
        }
    };
}
