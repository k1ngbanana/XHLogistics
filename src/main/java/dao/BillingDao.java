package dao;

import org.apache.ibatis.session.SqlSession;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pojo.Billing;
import pojo.mapper.BillingMapper;

import java.util.HashMap;
import java.util.List;

@Repository
public class BillingDao implements BillingMapper {

	@Autowired
	SqlSessionFactory ssf;
	/**
	 * 批量插入航线
	 *
	 * @param billing
	 *            需要插入的billing
	 */
	public void insertBilling(Billing billing) {

		// 打开session
		SqlSession session = ssf.openSession();
		BillingMapper mapper = session.getMapper(BillingMapper.class);
		mapper.insertBilling(billing);
		session.commit();
		session.close();
	}

	@Override
	public Billing selectBilling(Billing billing) {
		// 打开session
		SqlSession session = ssf.openSession();
		BillingMapper mapper = session.getMapper(BillingMapper.class);
		billing = mapper.selectBilling(billing);
		System.out.println(billing);
		session.close();
		return billing;
	}

	@Override
	public void updateBilling(Billing billing) {
		// 打开session
		SqlSession session = ssf.openSession();
		BillingMapper mapper = session.getMapper(BillingMapper.class);
		mapper.updateBilling(billing);
		session.commit();
		session.close();
	}

	@Override
	public List<Billing> selectBillingBetweenDateAndPayer(HashMap<Object, Object> hashMap) {
		// 打开session
		SqlSession session = ssf.openSession();
		BillingMapper mapper = session.getMapper(BillingMapper.class);
		List<Billing> billingList = mapper.selectBillingBetweenDateAndPayer(hashMap);
		session.close();
		return billingList;
	}
}
