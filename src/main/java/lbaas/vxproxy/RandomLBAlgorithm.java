package lbaas.vxproxy;

/*
 * Random Load Balance Algorithm
 */
public class RandomLBAlgorithm implements ILoadBalanceAlgorithm {

    public Integer getSelected(Integer size) {
        return (int) (Math.random() * (size - Float.MIN_VALUE));

    }

}
