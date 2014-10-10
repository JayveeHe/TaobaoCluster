package TaobaoCluster;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created by Jayvee on 2014/9/18.
 */
public interface IClusterCalculable<D> {
    /**
     * 获取待计算的向量值
     *
     * @return
     */
    public float[] getVecValues();

    /**
     * 获取向量ID
     *
     * @return
     */
    public int getID();

    /**
     * 获取向量类别ID
     *
     * @return
     */
    public int getTypeID();

    /**
     * 设置向量类别ID
     *
     * @param typeID
     */
    public void setTypeID(int typeID);

    public void setVecValues(float[] vecValues);

    public void setID(int ID);

//    public void setObj(D obj);

    public D getObj();

}
