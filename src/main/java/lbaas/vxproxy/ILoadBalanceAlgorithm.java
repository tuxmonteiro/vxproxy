package lbaas.vxproxy;

public interface ILoadBalanceAlgorithm {

    Integer getSelected(Integer size);

}
