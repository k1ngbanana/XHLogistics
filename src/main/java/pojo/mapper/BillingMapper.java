package pojo.mapper;

import pojo.Billing;

import java.util.HashMap;
import java.util.List;

public interface BillingMapper {
	void insertBilling(Billing billing);

	/**
	 * 通过billing的单号和人名进行选择
	 * @param billing
	 * @return
	 */
	Billing selectBilling(Billing billing);

	/**
	 * 更新单个billing
	 * @param billing
	 */
	void updateBilling(Billing billing);

	/**
	 * 获取时间段内对应付款人的账单
	 * @return
	 */
	List<Billing> selectBillingBetweenDateAndPayer(HashMap<Object,Object> hashMap);

}
